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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.activity.MapActivity;
import com.hyperaware.conference.android.data.FirebaseDatabaseHelpers;
import com.hyperaware.conference.android.fdb.FirebaseMultiQuery;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.view.MutexViewGroup;
import com.hyperaware.conference.model.MapItem;
import com.hyperaware.conference.model.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapsFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(MapsFragment.class);

    private static final String ARG_TITLE = "title";

    private String title;

    private DatabaseReference mapsRef;
    private FirebaseMultiQuery firebaseMultiQuery;

    private MutexViewGroup vgMutex;
    private RecyclerView rv;
    private MapsAdapter adapter;

    private List<MapItem> mapItems;
    private Exception exception;

    @NonNull
    public static MapsFragment instantiate(@NonNull String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        final MapsFragment fragment = new MapsFragment();
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
        mapsRef = fdb.getReference("/sections/maps");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            ((ContentHost) activity).setTitle(title);
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

        firebaseMultiQuery = new FirebaseMultiQuery(mapsRef);
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
                final Section<MapItem> maps = FirebaseDatabaseHelpers.toMapsSection(task.getResult().get(mapsRef));
                mapItems = new ArrayList<>(maps.getItems().values());
            }
            else {
                exception = task.getException();
                LOGGER.log(Level.SEVERE, "oops", exception);
            }
            updateUi();
        }
    }

    private void updateUi() {
        if (mapItems != null) {
            if (mapItems.size() > 0) {
                if (adapter == null) {
                    adapter = new MapsAdapter(mapItems);
                    rv.setAdapter(adapter);
                }
                else {
                    adapter.updateItems(mapItems);
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

    private class MapsAdapter extends RecyclerView.Adapter {

        private static final int TYPE_MAP_ITEM = 0;

        private List<MapItem> items;

        public MapsAdapter(List<MapItem> items) {
            this.items = items;
        }

        public void updateItems(@NonNull final List<MapItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_MAP_ITEM;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
            case TYPE_MAP_ITEM:
                return new MapItemViewHolder(inflater.inflate(R.layout.item_map, parent, false));
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
            case TYPE_MAP_ITEM:
                final MapItemViewHolder mivh = (MapItemViewHolder) holder;
                mivh.bindAgendaItem(items.get(position));
                break;
            }
        }

    }

    private class MapItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;

        public MapItemViewHolder(View view) {
            super(view);
            tvName = (TextView) view.findViewById(R.id.tv_name);
        }

        public void bindAgendaItem(final MapItem item) {
            tvName.setText(item.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String map_url = item.getFilename();
                    final Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra(MapActivity.EXTRA_IMAGE_URL, map_url);
                    startActivity(intent);
                }
            });
        }
    }

}
