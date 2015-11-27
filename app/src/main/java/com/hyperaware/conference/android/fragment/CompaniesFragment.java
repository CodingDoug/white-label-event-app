/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.eventmobi.EventmobiConfig;
import com.hyperaware.conference.android.eventmobi.model.AllEventData;
import com.hyperaware.conference.android.eventmobi.model.CompanyItem;
import com.hyperaware.conference.android.eventmobi.model.Section;
import com.hyperaware.conference.android.ui.error.CommonContentController;
import com.hyperaware.conference.android.util.CompanyItems;
import com.hyperaware.conference.android.view.MutexViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class CompaniesFragment extends Fragment implements Titled {

    private static final String ARG_TITLE = "title";

    private String title;
    private Bus bus;
    private EventmobiConfig config;
    private ContentHost host;

    private MutexViewGroup vgMutex;
    private RecyclerView rv;
    private CommonContentController contentController;

    private String eventId;

    @NonNull
    public static CompaniesFragment instantiate(@NonNull String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        CompaniesFragment fragment = new CompaniesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        title = args.getString(ARG_TITLE);

        bus = Singletons.deps.getBus();
        config = Singletons.deps.getEventmobiConfig();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_companies, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            host = (ContentHost) activity;
            host.setTitle(title);
        }

        View root = getView();
        if (root == null) {
            throw new IllegalStateException();
        }

        vgMutex = (MutexViewGroup) root.findViewById(R.id.vg_mutex);
        rv = (RecyclerView) vgMutex.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        contentController = new CommonContentController(activity, vgMutex);
    }

    @Override
    public void onStart() {
        super.onStart();
        bus.register(contentController);
        bus.register(this);
    }

    @Override
    public void onStop() {
        bus.unregister(this);
        bus.unregister(contentController);
        super.onStop();
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Subscribe
    public void onAllEventData(final AllEventData data) {
        final Section<CompanyItem> companiesSection = data.companiesSection;
        eventId = companiesSection.getEventId();
        final ArrayList<CompanyItem> items = new ArrayList<>(companiesSection.getItems());
        Collections.sort(items, CompanyItems.POSITION_COMPARATOR);
        rv.setAdapter(new CompaniesAdapter(items));
        vgMutex.showView(rv);
    }

    //
    // RecyclerView stuff
    //

    private class CompaniesAdapter extends RecyclerView.Adapter {

        private static final int TYPE_COMPANY_ITEM = 0;

        private final List<CompanyItem> items;

        public CompaniesAdapter(List<CompanyItem> items) {
            this.items = items;
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
                .load(config.getCompanyImageUrl(eventId, item.getLogoLargeWide()))
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
