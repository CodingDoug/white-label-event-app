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
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.eventmobi.model.AgendaItem;
import com.hyperaware.conference.android.eventmobi.model.AllEventData;
import com.hyperaware.conference.android.eventmobi.model.SpeakerItem;
import com.hyperaware.conference.android.ui.error.CommonContentController;
import com.hyperaware.conference.android.ui.favsession.FavSessionButtonController;
import com.hyperaware.conference.android.ui.model.DateHeader;
import com.hyperaware.conference.android.ui.model.TimeGroupHeader;
import com.hyperaware.conference.android.util.AgendaItems;
import com.hyperaware.conference.android.util.BundleSerializer;
import com.hyperaware.conference.android.view.MutexViewGroup;

import java.io.Serializable;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class AgendaFragment extends Fragment implements Titled {

    private static final String ARG_TITLE = "title";
    private static final String ARG_START_AT_TIME = "start_at_time";

    private String title;
    private long startAtTime;
    private Bus bus;
    private FavSessionButtonController favController;
    private ContentHost host;

    private MutexViewGroup vgMutex;
    private RecyclerView rv;
    private CommonContentController contentController;

    private TimeZone tz;
    private List<Object> items;
    private Map<String, SpeakerItem> speakerItemsById;

    private static class FragmentState implements Serializable {
        public boolean alreadyScrolledToTime;
    }

    private FragmentState state;

    @NonNull
    public static AgendaFragment instantiate(@NonNull String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        AgendaFragment fragment = new AgendaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    public static AgendaFragment instantiate(@NonNull String title, long start_at_time) {
        AgendaFragment fragment = instantiate(title);
        final Bundle args = fragment.getArguments();
        args.putLong(ARG_START_AT_TIME, start_at_time);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreState(savedInstanceState);

        Bundle args = getArguments();
        title = args.getString(ARG_TITLE);
        startAtTime = args.getLong(ARG_START_AT_TIME, 0);

        bus = Singletons.deps.getBus();
        favController = new FavSessionButtonController(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
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

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    private void restoreState(final Bundle bundle) {
        if (bundle != null) {
            BundleSerializer<FragmentState> serializer = new BundleSerializer<>();
            state = serializer.deserialize(bundle);
        }
        else {
            state = new FragmentState();
        }
    }

    private void saveState(final Bundle bundle) {
        BundleSerializer<FragmentState> serializer = new BundleSerializer<>();
        serializer.serialize(state, bundle);
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Subscribe
    public void onAllEventData(final AllEventData data) {
        tz = TimeZone.getTimeZone(data.event.getTimezoneName());
        items = AgendaItems.organize(data.agendaSection.getItems(), tz);
        speakerItemsById = data.speakerItemsById;
        updateUi();
    }

    private void updateUi() {
        rv.setAdapter(new ScheduleAdapter(items, tz));

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

    // Look for first, nearest position without passing startAtTime
    private int computeTimedScrollToPosition() {
        int scroll_to = 0;
        long scroll_to_time = 0;
        for (int i = 0; i < items.size(); i++) {
            final Object item = items.get(i);
            if (item instanceof TimeGroupHeader) {
                TimeGroupHeader tgh = (TimeGroupHeader) item;
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

        private final List<Object> items;
        private final TimeZone tz;

        public ScheduleAdapter(List<Object> items, TimeZone tz) {
            this.items = items;
            this.tz = tz;
        }

        @Override
        public int getItemViewType(int position) {
            Object item = items.get(position);
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
            AgendaItemViewHolder h = (AgendaItemViewHolder) holder;
            h.bindAgendaItem((AgendaItem) items.get(position));
        }

        private void bindDateHeader(RecyclerView.ViewHolder holder, int position) {
            DateHeaderViewHolder h = (DateHeaderViewHolder) holder;
            h.bindDateHeader((DateHeader) items.get(position));
        }

        private void bindTimeGroupHeader(RecyclerView.ViewHolder holder, int position) {
            TimeGroupHeaderViewHolder h = (TimeGroupHeaderViewHolder) holder;
            h.bindTimeGroupHeader((TimeGroupHeader) items.get(position));
        }

    }

    private static StringBuilder sb = new StringBuilder();
    private static Formatter formatter = new Formatter(sb);

    private class AgendaItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTopic;
        private final TextView tvLocAndSpeaker;
        private final ImageButton buttonFavorite;

        public AgendaItemViewHolder(View view) {
            super(view);
            tvTopic = (TextView) view.findViewById(R.id.tv_topic);
            tvLocAndSpeaker = (TextView) view.findViewById(R.id.tv_loc_and_speaker);
            buttonFavorite = (ImageButton) view.findViewById(R.id.button_favorite_session);
        }

        public void bindAgendaItem(final AgendaItem item) {
            tvTopic.setText(item.getTopic());
            tvLocAndSpeaker.setText(AgendaItems.formatLocationAndSpeaker(getContext(), item, speakerItemsById));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment next = SessionDetailFragment.instantiate(item.getId());
                    host.pushFragment(next, "session_detail");
                }
            });

            favController.init(buttonFavorite, item.getId());
            buttonFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    favController.toggle(buttonFavorite, item.getId());
                }
            });
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

}
