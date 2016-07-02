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
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.util.AgendaItems;
import com.hyperaware.conference.android.util.SpeakerItems;
import com.hyperaware.conference.android.view.MutexViewGroup;
import com.hyperaware.conference.model.AgendaItem;
import com.hyperaware.conference.model.AgendaSection;
import com.hyperaware.conference.model.Section;
import com.hyperaware.conference.model.SpeakerItem;
import com.hyperaware.conference.model.SpeakersSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class SpeakersFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(SpeakersFragment.class);

    private static final String ARG_TITLE = "title";

    private String title;
    private Bus bus;
    private ContentHost host;

    private MutexViewGroup vgMutex;
    private RecyclerView rv;
    private SpeakersAdapter adapter;

    private Section<AgendaItem> agenda;
    private Section<SpeakerItem> speakers;
    private List<SpeakerItem> speakerItems;
    private Map<String, List<AgendaItem>> speakersAgendaItems;

    @NonNull
    public static SpeakersFragment instantiate(String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        final SpeakersFragment fragment = new SpeakersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.fine("onCreate");

        final Bundle args = getArguments();
        title = args.getString(ARG_TITLE);

        bus = Singletons.deps.getBus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = null;
        return inflater.inflate(R.layout.fragment_speakers, container, false);
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
        bus.register(this);
    }

    @Override
    public void onStop() {
        bus.unregister(this);
        super.onStop();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Subscribe
    public void onAgenda(AgendaSection agenda) {
        if (agenda != null && !agenda.equals(this.agenda)) {
            this.agenda = agenda;
            speakersAgendaItems = AgendaItems.groupBySpeaker(agenda.getItems().values());
            updateUi();
        }
    }

    @Subscribe
    public void onSpeakers(SpeakersSection speakers) {
        if (speakers != null && !speakers.equals(this.speakers)) {
            this.speakers = speakers;
            speakerItems = new ArrayList<>(speakers.getItems().values());
            Collections.sort(speakerItems, SpeakerItems.POSITION_COMPARATOR);
            updateUi();
        }
    }

    private void updateUi() {
        if (speakerItems != null && speakersAgendaItems != null) {
            if (speakerItems.size() > 0) {
                if (adapter == null) {
                    adapter = new SpeakersAdapter(speakerItems);
                    rv.setAdapter(adapter);
                }
                else {
                    adapter.updateItems(speakerItems);
                }
                vgMutex.showView(rv);
            }
            else {
                vgMutex.showViewId(R.id.vg_empty_section);
            }
        }
        else {
            vgMutex.showViewId(R.id.pb);
        }
    }

    //
    // RecyclerView stuff
    //

    private class SpeakersAdapter extends RecyclerView.Adapter {

        private static final int TYPE_SPEAKER_ITEM = 0;

        private List<SpeakerItem> items;

        public SpeakersAdapter(@NonNull final List<SpeakerItem> items) {
            this.items = items;
        }

        public void updateItems(@NonNull final List<SpeakerItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_SPEAKER_ITEM;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
            case TYPE_SPEAKER_ITEM:
                return new SpeakersItemViewHolder(inflater.inflate(R.layout.item_speaker, parent, false));
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
            case TYPE_SPEAKER_ITEM:
                SpeakersItemViewHolder sivh = (SpeakersItemViewHolder) holder;
                sivh.bindSpeakerItem(items.get(position));
                break;
            }
        }

    }

    private class SpeakersItemViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivPic;
        public final TextView tvName;
        public final TextView tvSummary;

        public SpeakersItemViewHolder(View view) {
            super(view);
            ivPic = (ImageView) view.findViewById(R.id.iv_pic);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvSummary = (TextView) view.findViewById(R.id.tv_summary);
        }

        public void bindSpeakerItem(final SpeakerItem item) {
            final List<AgendaItem> agendaItems = speakersAgendaItems.get(item.getId());
            int num_sessions = 0;
            if (agendaItems != null) {
                num_sessions = agendaItems.size();
            }

            Glide
                .with(itemView.getContext())
                .load(item.getImage100())
                .fitCenter()
                .placeholder(R.drawable.nopic)
                .into(ivPic);
            tvName.setText(item.getName());
            if (num_sessions > 0) {
                tvSummary.setText(getResources().getQuantityString(R.plurals.session_count, num_sessions, num_sessions));
            }
            if (host != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment next = SpeakerDetailFragment.instantiate(item.getId());
                        host.pushFragment(next, "speaker_detail");
                    }
                });
            }
        }
    }

}
