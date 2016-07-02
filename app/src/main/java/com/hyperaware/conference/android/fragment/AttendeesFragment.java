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
import com.hyperaware.conference.android.util.AttendeeItems;
import com.hyperaware.conference.android.data.FirebaseDatabaseHelpers;
import com.hyperaware.conference.android.view.MutexViewGroup;
import com.hyperaware.conference.model.AttendeeItem;
import com.hyperaware.conference.model.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttendeesFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(AttendeesFragment.class);

    private static final String ARG_TITLE = "title";

    private String title;
    private ContentHost host;

    private DatabaseReference attendeesRef;
    private FirebaseMultiQuery firebaseMultiQuery;

    private MutexViewGroup vgMutex;
    private RecyclerView rv;
    private AttendeesAdapter adapter;

    private List<AttendeeItem> items;
    private Exception exception;

    @NonNull
    public static AttendeesFragment instantiate(String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        final AttendeesFragment fragment = new AttendeesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.fine("onCreate");

        final Bundle args = getArguments();
        title = args.getString(ARG_TITLE);

        final FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        attendeesRef = fdb.getReference("/sections/attendees");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = null;
        return inflater.inflate(R.layout.fragment_attendees, container, false);
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

        firebaseMultiQuery = new FirebaseMultiQuery(attendeesRef);
        final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
        allLoad.addOnCompleteListener(getActivity(), new AllOnCompleteListener());
    }

    @Override
    public void onStop() {
        firebaseMultiQuery.stop();
        super.onStop();
    }

    @Override
    public String getTitle() {
        return title;
    }

    private class AllOnCompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
        @Override
        public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
            if (task.isSuccessful()) {
                final Section<AttendeeItem> attendeesSection = FirebaseDatabaseHelpers.toAttendeesSection(task.getResult().get(attendeesRef));
                items = new ArrayList<>(attendeesSection.getItems().values());
                Collections.sort(items, AttendeeItems.NAME_COMPARATOR);
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
                    adapter = new AttendeesAdapter(items);
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

    private class AttendeesAdapter extends RecyclerView.Adapter {

        private static final int TYPE_ATTENDEE_ITEM = 0;

        private List<AttendeeItem> items;

        public AttendeesAdapter(List<AttendeeItem> items) {
            this.items = items;
        }

        public void updateItems(List<AttendeeItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_ATTENDEE_ITEM;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
            case TYPE_ATTENDEE_ITEM:
                return new AttendeeItemViewHolder(inflater.inflate(R.layout.item_attendee, parent, false));
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
            case TYPE_ATTENDEE_ITEM:
                AttendeeItemViewHolder sivh = (AttendeeItemViewHolder) holder;
                sivh.bindAttendeeItem(items.get(position));
                break;
            }
        }
    }

    private class AttendeeItemViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivPic;
        public final TextView tvName;
        public final TextView tvSummary;

        public AttendeeItemViewHolder(View view) {
            super(view);
            ivPic = (ImageView) view.findViewById(R.id.iv_pic);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvSummary = (TextView) view.findViewById(R.id.tv_summary);
        }

        public void bindAttendeeItem(final AttendeeItem item) {
            Glide
                .with(itemView.getContext())
                .load(item.getImage100())
                .fitCenter()
                .placeholder(R.drawable.nopic)
                .into(ivPic);
            tvName.setText(item.getName());
            tvSummary.setText(item.getCompanyName());
            if (host != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment next = AttendeeDetailFragment.instantiate(item.getId());
                        host.pushFragment(next, "attendee_detail");
                    }
                });
            }
        }
    }

}
