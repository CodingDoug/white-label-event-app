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
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.ui.favsession.FavSessionButtonManager;
import com.hyperaware.conference.android.ui.model.DateHeader;
import com.hyperaware.conference.android.ui.model.TimeGroupHeader;
import com.hyperaware.conference.android.util.AgendaItems;
import com.hyperaware.conference.android.util.BundleSerializer;
import com.hyperaware.conference.android.view.MutexViewGroup;
import com.hyperaware.conference.model.AgendaItem;
import com.hyperaware.conference.model.AgendaSection;
import com.hyperaware.conference.model.Event;
import com.hyperaware.conference.model.Section;
import com.hyperaware.conference.model.SpeakerItem;
import com.hyperaware.conference.model.SpeakersSection;

import java.io.Serializable;
import java.util.Formatter;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class AgendaFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(AgendaFragment.class);

    private static final String ARG_TITLE = "title";
    private static final String ARG_START_AT_TIME = "start_at_time";

    private String title;
    private long startAtTime;
    private Bus bus;
    private ContentHost host;
    private FavSessionButtonManager favSessionButtonManager;

    private MutexViewGroup vgMutex;
    private RecyclerView rv;
    private ScheduleAdapter adapter;

    private Event event;
    private Section<AgendaItem> agenda;
    private Section<SpeakerItem> speakers;
    private TimeZone tz;
    private List<Object> items;

    private static class FragmentState implements Serializable {
        public boolean alreadyScrolledToTime;
    }

    private FragmentState state;

    @NonNull
    public static AgendaFragment instantiate(@NonNull String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        final AgendaFragment fragment = new AgendaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    public static AgendaFragment instantiate(@NonNull String title, long start_at_time) {
        final AgendaFragment fragment = instantiate(title);
        final Bundle args = fragment.getArguments();
        args.putLong(ARG_START_AT_TIME, start_at_time);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.fine("onCreate");
        restoreState(savedInstanceState);

        final Bundle args = getArguments();
        title = args.getString(ARG_TITLE);
        startAtTime = args.getLong(ARG_START_AT_TIME, 0);

        favSessionButtonManager = new FavSessionButtonManager(
            FirebaseDatabase.getInstance(), FirebaseAuth.getInstance(), new MyAuthRequiredListener());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = null;
        return inflater.inflate(R.layout.fragment_agenda, container, false);
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

        bus = Singletons.deps.getBus();

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
        favSessionButtonManager.start();
    }

    @Override
    public void onStop() {
        favSessionButtonManager.stop();
        bus.unregister(this);

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    private void restoreState(final Bundle bundle) {
        if (bundle != null) {
            state = new BundleSerializer<FragmentState>().deserialize(bundle);
        }
        else {
            state = new FragmentState();
        }
    }

    private void saveState(final Bundle bundle) {
        new BundleSerializer<FragmentState>().serialize(state, bundle);
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Subscribe
    public void onEvent(final Event event) {
        if (event != null && !event.equals(this.event)) {
            this.event = event;
            tz = TimeZone.getTimeZone(event.getTimezoneName());
            updateUi();
        }
    }

    @Subscribe
    public void onAgenda(final AgendaSection agenda) {
        if (agenda != null && !agenda.equals(this.agenda)) {
            this.agenda = agenda;
            updateUi();
        }
    }

    @Subscribe
    public void onSpeakers(final SpeakersSection speakers) {
        if (speakers != null && !speakers.equals(this.speakers)) {
            this.speakers = speakers;
            updateUi();
        }
    }

    private void updateUi() {
        if (event != null && agenda != null && speakers != null) {
            if (agenda.getItems().size() > 0) {
                items = AgendaItems.organize(agenda.getItems().values(), tz);
                if (adapter == null) {
                    adapter = new ScheduleAdapter(items, tz);
                    rv.setAdapter(adapter);
                }
                else {
                    adapter.updateItems(items);
                }

                // We want the user scrolled position to be retained when they
                // use the back button to come back to this fragment.
                if (!state.alreadyScrolledToTime) {
                    if (startAtTime > 0) {
                        rv.scrollToPosition(computeTimedScrollToPosition());
                        state.alreadyScrolledToTime = true;
                    }
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

    // Look for first, nearest position without passing startAtTime
    private int computeTimedScrollToPosition() {
        int scroll_to = 0;
        long scroll_to_time = 0;
        for (int i = 0; i < items.size(); i++) {
            final Object item = items.get(i);
            if (item instanceof TimeGroupHeader) {
                final TimeGroupHeader tgh = (TimeGroupHeader) item;
                if (tgh.start <= startAtTime && tgh.start > scroll_to_time) {
                    scroll_to = i;
                    scroll_to_time = tgh.start;
                }
                else if (tgh.start > scroll_to_time) {
                    break;
                }
            }
        }
        return scroll_to;
    }

    //
    // RecyclerView stuff
    //

    private class ScheduleAdapter extends RecyclerView.Adapter {

        private static final int TYPE_AGENDA_ITEM = 0;
        private static final int TYPE_DATE_HEADER = 1;
        private static final int TYPE_TIME_GROUP_HEADER = 2;

        private List<Object> items;
        private final TimeZone tz;

        public ScheduleAdapter(@NonNull final List<Object> items, @NonNull final  TimeZone tz) {
            this.items = items;
            this.tz = tz;
        }

        public void updateItems(@NonNull final List<Object> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            final Object item = items.get(position);
            if (item instanceof AgendaItem) {
                return TYPE_AGENDA_ITEM;
            }
            else if (item instanceof DateHeader) {
                return TYPE_DATE_HEADER;
            }
            else if (item instanceof TimeGroupHeader) {
                return TYPE_TIME_GROUP_HEADER;
            }
            else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
            case TYPE_AGENDA_ITEM:
                return new AgendaItemViewHolder(inflater.inflate(R.layout.item_agenda, parent, false));
            case TYPE_DATE_HEADER:
                return new DateHeaderViewHolder(inflater.inflate(R.layout.item_date_header, parent, false), tz);
            case TYPE_TIME_GROUP_HEADER:
                return new TimeGroupHeaderViewHolder(inflater.inflate(R.layout.item_time_group_header, parent, false), tz);
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
            case TYPE_AGENDA_ITEM:
                bindAgendaItem(holder, position);
                break;
            case TYPE_DATE_HEADER:
                bindDateHeader(holder, position);
                break;
            case TYPE_TIME_GROUP_HEADER:
                bindTimeGroupHeader(holder, position);
                break;
            }
        }

        private void bindAgendaItem(RecyclerView.ViewHolder holder, int position) {
            final AgendaItemViewHolder h = (AgendaItemViewHolder) holder;
            h.bindAgendaItem((AgendaItem) items.get(position));
        }

        private void bindDateHeader(RecyclerView.ViewHolder holder, int position) {
            final DateHeaderViewHolder h = (DateHeaderViewHolder) holder;
            h.bindDateHeader((DateHeader) items.get(position));
        }

        private void bindTimeGroupHeader(RecyclerView.ViewHolder holder, int position) {
            final TimeGroupHeaderViewHolder h = (TimeGroupHeaderViewHolder) holder;
            h.bindTimeGroupHeader((TimeGroupHeader) items.get(position));
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            if (holder.getItemViewType() == TYPE_AGENDA_ITEM) {
                ((AgendaItemViewHolder) holder).unbind();
            }
        }

    }

    private static StringBuilder sb = new StringBuilder();
    private static Formatter formatter = new Formatter(sb);

    private class AgendaItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTopic;
        private final TextView tvLocAndSpeaker;
        private final ImageButton buttonFavorite;
        public AgendaItem agendaItem;

        public AgendaItemViewHolder(View view) {
            super(view);
            tvTopic = (TextView) view.findViewById(R.id.tv_topic);
            tvLocAndSpeaker = (TextView) view.findViewById(R.id.tv_loc_and_speaker);
            buttonFavorite = (ImageButton) view.findViewById(R.id.button_favorite_session);
        }

        public void bindAgendaItem(final AgendaItem item) {
            tvTopic.setText(item.getTopic());
            tvLocAndSpeaker.setText(
                AgendaItems.formatLocationAndSpeaker(getContext(), item, speakers.getItems()));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment next = SessionDetailFragment.instantiate(item.getId());
                    host.pushFragment(next, "session_detail");
                }
            });
            this.agendaItem = item;

            favSessionButtonManager.attach(buttonFavorite, item.getId());
        }

        public void unbind() {
            if (agendaItem != null) {
                favSessionButtonManager.detach(buttonFavorite, agendaItem.getId());
                agendaItem = null;
            }
        }
    }

    private static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private final View itemView;
        private final TimeZone tz;
        private final TextView tvDate;

        public DateHeaderViewHolder(View view, TimeZone tz) {
            super(view);
            itemView = view;
            this.tz = tz;
            tvDate = (TextView) view.findViewById(R.id.tv_date);
        }

        public void bindDateHeader(DateHeader item) {
            sb.setLength(0);
            DateUtils.formatDateRange(
                itemView.getContext(),
                formatter,
                item.date,
                item.date,
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR,
                tz.getID()
            );
            tvDate.setText(formatter.toString());
        }
    }

    private static class TimeGroupHeaderViewHolder extends RecyclerView.ViewHolder {
        private final View itemView;
        private final TimeZone tz;
        private final TextView tvTimeRange;

        public TimeGroupHeaderViewHolder(View view, TimeZone tz) {
            super(view);
            itemView = view;
            this.tz = tz;
            tvTimeRange = (TextView) view.findViewById(R.id.tv_time_range);
        }

        public void bindTimeGroupHeader(TimeGroupHeader item) {
            sb.setLength(0);
            DateUtils.formatDateRange(
                itemView.getContext(),
                formatter,
                item.start,
                item.end,
                DateUtils.FORMAT_SHOW_TIME,
                tz.getID()
            );
            tvTimeRange.setText(formatter.toString());
        }
    }

    private class MyAuthRequiredListener implements FavSessionButtonManager.AuthRequiredListener {
        @Override
        public void onAuthRequired(ImageButton view, String sessionId) {
            new SigninRequiredDialogFragment().show(getFragmentManager(), null);
        }
    }

}
