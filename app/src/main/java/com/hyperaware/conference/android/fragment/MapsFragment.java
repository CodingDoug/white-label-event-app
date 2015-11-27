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

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.activity.MapActivity;
import com.hyperaware.conference.android.eventmobi.EventmobiConfig;
import com.hyperaware.conference.android.eventmobi.model.AllEventData;
import com.hyperaware.conference.android.eventmobi.model.MapItem;
import com.hyperaware.conference.android.ui.error.CommonContentController;
import com.hyperaware.conference.android.view.MutexViewGroup;

import java.util.List;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class MapsFragment extends Fragment implements Titled {

    private static final String ARG_TITLE = "title";

    private String title;
    private Bus bus;

    private MutexViewGroup vgMutex;
    private RecyclerView rv;
    private CommonContentController contentController;

    private String eventId;
    private List<MapItem> mapItems;

    @NonNull
    public static MapsFragment instantiate(@NonNull String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        MapsFragment fragment = new MapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        title = args.getString(ARG_TITLE);

        bus = Singletons.deps.getBus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            ((ContentHost) activity).setTitle(title);
        }

        View root = getView();
        if (root == null) {
            throw new IllegalStateException();
        }

        vgMutex = (MutexViewGroup) root.findViewById(R.id.vg_mutex);
        vgMutex.showViewId(R.id.pb);

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
        eventId = data.event.getId();
        mapItems = data.mapsSection.getItems();
        updateUi();
    }

    private void updateUi() {
        rv.setAdapter(new MapsAdapter(mapItems));
        vgMutex.showView(rv);
    }

    //
    // RecyclerView stuff
    //

    private class MapsAdapter extends RecyclerView.Adapter {

        private static final int TYPE_MAP_ITEM = 0;

        private final List<MapItem> items;

        public MapsAdapter(List<MapItem> items) {
            this.items = items;
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
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

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
                MapItemViewHolder mivh = (MapItemViewHolder) holder;
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
                    final EventmobiConfig config = Singletons.deps.getEventmobiConfig();
                    final String map_url = config.getMapImageUrl(eventId, item.getFilename());
                    final Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra(MapActivity.EXTRA_IMAGE_URL, map_url);
                    startActivity(intent);
                }
            });
        }
    }

}
