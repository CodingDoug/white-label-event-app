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
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.eventmobi.model.AgendaItem;
import com.hyperaware.conference.android.eventmobi.model.AllEventData;
import com.hyperaware.conference.android.ui.error.CommonContentController;
import com.hyperaware.conference.android.util.AdjustableClock;
import com.hyperaware.conference.android.util.AgendaItems;
import com.hyperaware.conference.android.util.Clock;
import com.hyperaware.conference.android.util.DateRange;
import com.hyperaware.conference.android.view.MutexViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class HomeFragment extends Fragment implements Titled {

    private static final String ARG_TITLE = "title";

    private String title;
    private Bus bus;
    private ContentHost host;

    private MutexViewGroup vgMutex;
    private View vBeforeEvent;
    private View vAfterEvent;
    private CardView cardHappeningNow;
    private CardView cardUpNext;
    private TextView tvDescription;
    private CommonContentController contentController;

    private AllEventData data;
    private long happeningNowStartTime;
    private long upNextStartTime;

    @NonNull
    public static HomeFragment instantiate(@NonNull String title) {
        final Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        title = args.getString(ARG_TITLE);

        bus = Singletons.deps.getBus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
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

        View content = root.findViewById(R.id.vg_content);
        vBeforeEvent = content.findViewById(R.id.v_before_event);
        vBeforeEvent.setVisibility(View.GONE);
        vAfterEvent = content.findViewById(R.id.v_after_event);
        vAfterEvent.setVisibility(View.GONE);
        cardHappeningNow = (CardView) content.findViewById(R.id.card_happening_now);
        cardHappeningNow.setVisibility(View.GONE);
        cardHappeningNow.setOnClickListener(new HappeningNowOnClickListener());
        cardUpNext = (CardView) content.findViewById(R.id.card_up_next);
        cardUpNext.setVisibility(View.GONE);
        cardUpNext.setOnClickListener(new UpNextOnClickListener());
        tvDescription = (TextView) content.findViewById(R.id.tv_description);

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
        this.data = data;
        updateUi();
    }

    @Subscribe
    public void onAdjustableClock(final AdjustableClock clock) {
        updateUi();
    }

    private long timeAtUpdate;

    private void updateUi() {
        if (data == null) {
            return;
        }

        final Clock clock = Singletons.deps.getClock();
        timeAtUpdate = clock.getCurrentTimeMillis();

        vgMutex.showViewId(R.id.vg_content);
        updateBeforeAndAfterEvent();
        updateHappeningNowCard();
        updateUpNextCard();
    }

    private void updateBeforeAndAfterEvent() {
        final TimeZone tz = TimeZone.getTimeZone(data.event.getTimezoneName());
        final Calendar now_day = startOfDay(timeAtUpdate, tz);

        vBeforeEvent.setVisibility(View.GONE);
        vAfterEvent.setVisibility(View.GONE);
        tvDescription.setVisibility(View.GONE);
        final ArrayList<AgendaItem> items = new ArrayList<>(data.agendaSection.getItems());
        if (items.size() > 0) {
            Collections.sort(items, AgendaItems.START_TIME_COMPARATOR);

            final AgendaItem first = items.get(0);
            Calendar first_day = startOfDay(TimeUnit.SECONDS.toMillis(first.getEpochStartTime()), tz);
            final AgendaItem last = items.get(items.size() - 1);
            long end_time = TimeUnit.SECONDS.toMillis(last.getEpochEndTime());

            if (now_day.getTimeInMillis() < first_day.getTimeInMillis()) {
                final TextView msg = (TextView) vBeforeEvent.findViewById(R.id.tv_message);
                final java.text.DateFormat dfmt = DateFormat.getLongDateFormat(getContext());
                dfmt.setTimeZone(tz);
                msg.setText(getString(R.string.fmt_message_before_event, dfmt.format(first_day.getTimeInMillis())));
                vBeforeEvent.setVisibility(View.VISIBLE);
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(data.event.getDescription());
            }
            else if (timeAtUpdate > end_time) {
                final TextView msg = (TextView) vAfterEvent.findViewById(R.id.tv_message);
                final java.text.DateFormat dfmt = DateFormat.getLongDateFormat(getContext());
                dfmt.setTimeZone(tz);
                msg.setText(getString(R.string.fmt_message_after_event, dfmt.format(end_time)));
                vAfterEvent.setVisibility(View.VISIBLE);
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(data.event.getDescription());
            }
        }
    }

    private void updateHappeningNowCard() {
        ViewGroup time_groups = (ViewGroup) cardHappeningNow.findViewById(R.id.vg_time_groups);
        time_groups.removeAllViews();

        final SortedMap<DateRange, List<AgendaItem>> happening =
            AgendaItems.happeningNow(data.agendaSection.getItems(), timeAtUpdate);

        if (happening.size() > 0) {
            populateTimeGroups(happening, time_groups);
            cardHappeningNow.setVisibility(View.VISIBLE);
            happeningNowStartTime = happening.firstKey().start;
        }
        else {
            cardHappeningNow.setVisibility(View.GONE);
        }
    }

    private void updateUpNextCard() {
        ViewGroup time_groups = (ViewGroup) cardUpNext.findViewById(R.id.vg_time_groups);
        time_groups.removeAllViews();

        final SortedMap<DateRange, List<AgendaItem>> up_next =
            AgendaItems.upNext(data.agendaSection.getItems(), timeAtUpdate,
                TimeUnit.DAYS.toMillis(1), TimeUnit.HOURS.toMillis(1));

        if (up_next.size() > 0) {
            populateTimeGroups(up_next, time_groups);
            cardUpNext.setVisibility(View.VISIBLE);
            upNextStartTime = up_next.firstKey().start;
        }
        else {
            cardUpNext.setVisibility(View.GONE);
        }
    }

    private final StringBuilder sb = new StringBuilder();
    private final Formatter formatter = new Formatter(sb);

    private void populateTimeGroups(SortedMap<DateRange, List<AgendaItem>> groups, ViewGroup time_groups) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        for (Map.Entry<DateRange, List<AgendaItem>> entry : groups.entrySet()) {
            final ViewGroup sessions_group =
                (ViewGroup) inflater.inflate(R.layout.item_time_group_sessions, time_groups, false);
            time_groups.addView(sessions_group);

            TextView tv_time = (TextView) sessions_group.findViewById(R.id.tv_time);
            final DateRange range = entry.getKey();
            sb.setLength(0);
            DateUtils.formatDateRange(
                tv_time.getContext(),
                formatter,
                range.start,
                range.end,
                DateUtils.FORMAT_SHOW_TIME,
                data.event.getTimezoneName()
            );
            tv_time.setText(formatter.toString());

            ViewGroup vg_sessions = (ViewGroup) sessions_group.findViewById(R.id.vg_sessions);
            vg_sessions.removeAllViews();
            for (AgendaItem item : entry.getValue()) {
                View session = inflater.inflate(R.layout.item_time_group_session, vg_sessions, false);
                vg_sessions.addView(session);
                TextView tv_topic = (TextView) session.findViewById(R.id.tv_topic);
                tv_topic.setText(item.getTopic());
            }
        }
    }

    private class HappeningNowOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            host.showAgendaAtTime(happeningNowStartTime);
        }
    }

    private class UpNextOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            host.showAgendaAtTime(upNextStartTime);
        }
    }


    private static Calendar startOfDay(long time, TimeZone tz) {
        Calendar cal = Calendar.getInstance(tz);
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

}
