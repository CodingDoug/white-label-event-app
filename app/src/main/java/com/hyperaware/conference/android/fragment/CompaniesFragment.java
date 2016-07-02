/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyperaware.conference.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.fdb.FirebaseMultiQuery;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.util.CompanyItems;
import com.hyperaware.conference.android.data.FirebaseDatabaseHelpers;
import com.hyperaware.conference.android.view.MutexViewGroup;
import com.hyperaware.conference.model.CompanyItem;
import com.hyperaware.conference.model.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompaniesFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(AgendaFragment.class);

    private static final String ARG_TITLE = "title";

    private String title;
    private ContentHost host;

    private DatabaseReference eventRef;
    private DatabaseReference companiesRef;
    private FirebaseMultiQuery firebaseMultiQuery;

    private MutexViewGroup vgMutex;
    private RecyclerView rv;
    private CompaniesAdapter adapter;

    private ArrayList<CompanyItem> items;
    private Exception exception;

    @NonNull
    public static CompaniesFragment instantiate(@NonNull String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        final CompaniesFragment fragment = new CompaniesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.fine("onCreate");

        final Bundle args = getArguments();
        title = args.getString(ARG_TITLE);

        final FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        eventRef = fdb.getReference("/event");
        companiesRef = fdb.getReference("/sections/companies");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = null;
        return inflater.inflate(R.layout.fragment_companies, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            host = (ContentHost) activity;
            host.setTitle(title);
        }

        final View root = getView();
        if (root == null) {
            throw new IllegalStateException();
        }

        vgMutex = (MutexViewGroup) root.findViewById(R.id.vg_mutex);
        rv = (RecyclerView) vgMutex.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        updateUi();
    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseMultiQuery = new FirebaseMultiQuery(eventRef, companiesRef);
        final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
        allLoad.addOnCompleteListener(getActivity(), new AllOnCompleteListener());
    }

    @Override
    public void onStop() {
        firebaseMultiQuery.stop();
        super.onStop();
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    private class AllOnCompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
        @Override
        public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
            if (task.isSuccessful()) {
                final Section<CompanyItem> companies = FirebaseDatabaseHelpers.toCompaniesSection(task.getResult().get(companiesRef));
                items = new ArrayList<>(companies.getItems().values());
                Collections.sort(items, CompanyItems.POSITION_COMPARATOR);
            }
            else {
                exception = task.getException();
                LOGGER.log(Level.SEVERE, "oops", exception);
            }
            updateUi();
        }
    }

    private void updateUi() {
        if (items != null) {
            if (items.size() > 0) {
                if (adapter == null) {
                    adapter = new CompaniesAdapter(items);
                    rv.setAdapter(adapter);
                }
                else {
                    adapter.updateItems(items);
                }
                vgMutex.showView(rv);
            }
            else {
                vgMutex.showViewId(R.id.vg_empty_section);
            }
        }
        else if (exception != null) {
            vgMutex.showViewId(R.id.vg_data_error);
        }
        else {
            vgMutex.showViewId(R.id.pb);
        }
    }

    //
    // RecyclerView stuff
    //

    private class CompaniesAdapter extends RecyclerView.Adapter {

        private static final int TYPE_COMPANY_ITEM = 0;

        private List<CompanyItem> items;

        public CompaniesAdapter(List<CompanyItem> items) {
            this.items = items;
        }

        public void updateItems(@NonNull final List<CompanyItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_COMPANY_ITEM;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
            case TYPE_COMPANY_ITEM:
                return new CompanyItemViewHolder(inflater.inflate(R.layout.card_company, parent, false));
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
            case TYPE_COMPANY_ITEM:
                CompanyItemViewHolder civh = (CompanyItemViewHolder) holder;
                civh.bindAgendaItem(items.get(position));
                break;
            }
        }
    }

    private class CompanyItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvLocation;
        private final TextView tvDescription;
        private final ImageView ivLogo;

        public CompanyItemViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvLocation = (TextView) view.findViewById(R.id.tv_location);
            tvDescription = (TextView) view.findViewById(R.id.tv_description);
            ivLogo = (ImageView) view.findViewById(R.id.iv_logo);
        }

        public void bindAgendaItem(final CompanyItem item) {
            tvName.setText(item.getName());
            tvLocation.setText(item.getLocation());
            tvDescription.setText(item.getDescription());
            Glide
                .with(itemView.getContext())
                .load(item.getLogoLargeWide())
                .fitCenter()
                .into(ivLogo);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment next = CompanyDetailFragment.instantiate(item.getId());
                    host.pushFragment(next, "company_detail");
                }
            });
        }
    }

}
