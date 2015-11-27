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
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.eventmobi.EventmobiConfig;
import com.hyperaware.conference.android.eventmobi.model.AgendaItem;
import com.hyperaware.conference.android.eventmobi.model.AllEventData;
import com.hyperaware.conference.android.eventmobi.model.Event;
import com.hyperaware.conference.android.eventmobi.model.SpeakerItem;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.plugin.SessionFeedbackUrlMap;
import com.hyperaware.conference.android.ui.favsession.FavSessionButtonController;
import com.hyperaware.conference.android.ui.favsession.FavSessionButtonOnClickListener;
import com.hyperaware.conference.android.ui.feedback.SessionFeedbackOnClickListener;
import com.hyperaware.conference.android.util.Strings;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class SessionDetailFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(SessionDetailFragment.class);

    private static final String ARG_SESSION_ID = "session_id";

    private String sessionId;

    private Bus bus;
    private FavSessionButtonController favController;
    private ContentHost host;
    private String feedbackUrl;

    private TextView tvTopic;
    private TextView tvDate, tvTime, tvLocation;
    private ViewGroup vgSpeakers;
    private TextView tvDescription;
    private ViewGroup vgActions;

    private Event event;
    private AgendaItem agendaItem;
    private ArrayList<SpeakerItem> speakerItems;
    private TimeZone tz;

    @NonNull
    public static SessionDetailFragment instantiate(@NonNull String session_id) {
        if (Strings.isNullOrEmpty(session_id)) {
            throw new IllegalArgumentException(ARG_SESSION_ID + " can't be null or empty");
        }

        final Bundle args = new Bundle();
        args.putString(ARG_SESSION_ID, session_id);

        SessionDetailFragment fragment = new SessionDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        sessionId = args.getString(ARG_SESSION_ID);
        if (Strings.isNullOrEmpty(sessionId)) {
            throw new IllegalArgumentException(ARG_SESSION_ID + " can't be null or empty");
        }

        bus = Singletons.deps.getBus();
        favController = new FavSessionButtonController(getContext());

        SessionFeedbackUrlMap urlMap = Singletons.deps.getSessionFeedbackUrlMap();
        feedbackUrl = urlMap.get(sessionId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            host = (ContentHost) activity;
            host.setTitle(null);
        }

        View root = getView();
        if (root == null) {
            throw new IllegalStateException();
        }

        tvTopic = (TextView) root.findViewById(R.id.tv_topic);
        tvDate = (TextView) root.findViewById(R.id.tv_date);
        tvTime = (TextView) root.findViewById(R.id.tv_time);
        tvLocation = (TextView) root.findViewById(R.id.tv_location);
        ImageButton ib_favorite = (ImageButton) root.findViewById(R.id.button_favorite_session);
        ib_favorite.setOnClickListener(
            new FavSessionButtonOnClickListener(favController, ib_favorite, sessionId));
        favController.init(ib_favorite, sessionId);
        vgSpeakers = (ViewGroup) root.findViewById(R.id.vg_speakers);
        vgSpeakers.setVisibility(View.GONE);
        vgSpeakers.removeAllViews();
        vgActions = (ViewGroup) root.findViewById(R.id.vg_actions);
        vgActions.setVisibility(View.GONE);
        tvDescription = (TextView) root.findViewById(R.id.tv_description);
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

    @Nullable
    @Override
    public String getTitle() {
        if (agendaItem != null) {
            return agendaItem.getTopic();
        }
        else {
            return null;
        }
    }

    @Subscribe
    public void onAllEventData(final AllEventData data) {
        event = data.event;
        agendaItem = data.agendaItemsById.get(sessionId);
        tz = TimeZone.getTimeZone(data.event.getTimezoneName());
        speakerItems = new ArrayList<>();
        for (String id : agendaItem.getSpeakerIds()) {
            final SpeakerItem item = data.speakerItemsById.get(id);
            if (item != null) {
                speakerItems.add(item);
            }
        }

        if (agendaItem != null) {
            updateUi();
        }
        else {
            // PARANOID TODO some UI
            LOGGER.warning("Session id " + sessionId + " not found");
        }
    }

    private void updateUi() {
        tvTopic.setText(agendaItem.getTopic());
        host.setTitle(agendaItem.getTopic());

        final StringBuilder sb = new StringBuilder();
        final Formatter formatter = new Formatter(sb);

        final long start_ms = TimeUnit.SECONDS.toMillis(agendaItem.getEpochStartTime());
        final long end_ms = TimeUnit.SECONDS.toMillis(agendaItem.getEpochEndTime());

        sb.setLength(0);
        DateUtils.formatDateRange(
            getActivity(),
            formatter,
            start_ms,
            end_ms,
            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY,
            tz.getID()
        );
        tvDate.setText(formatter.toString());

        sb.setLength(0);
        DateUtils.formatDateRange(
            getActivity(),
            formatter,
            start_ms,
            end_ms,
            DateUtils.FORMAT_SHOW_TIME,
            tz.getID()
        );
        tvTime.setText(formatter.toString());

        final String location = agendaItem.getLocation();
        if (!Strings.isNullOrEmpty(location)) {
            tvLocation.setText(agendaItem.getLocation());
        }
        else {
            tvLocation.setVisibility(View.GONE);
        }

        tvDescription.setText(agendaItem.getDescription());

        if (feedbackUrl != null) {
            final String feedback_title = getString(R.string.fmt_title_session_feedback, agendaItem.getTopic());
            vgActions.findViewById(R.id.tv_session_feedback)
                .setOnClickListener(new SessionFeedbackOnClickListener(host, feedbackUrl, feedback_title));
            vgActions.setVisibility(View.VISIBLE);
        }

        EventmobiConfig config = Singletons.deps.getEventmobiConfig();

        if (speakerItems.size() > 0) {
            vgSpeakers.setVisibility(View.VISIBLE);
            vgSpeakers.removeAllViews();
            final LayoutInflater inflater = getActivity().getLayoutInflater();

            for (final SpeakerItem item : speakerItems) {
                final View view = inflater.inflate(R.layout.item_session_speaker, vgSpeakers, false);

                ImageView iv = (ImageView) view.findViewById(R.id.iv_pic);
                String image100 = item.getImage100();
                Glide
                    .with(SessionDetailFragment.this)
                    .load(config.getPersonImageUrl(event.getId(), image100))
                    .placeholder(R.drawable.nopic)
                    .into(iv);

                ((TextView) view.findViewById(R.id.tv_name)).setText(item.getName());

                if (host != null) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Fragment next = SpeakerDetailFragment.instantiate(item.getId());
                            host.pushFragment(next, "speaker_detail");
                        }
                    });
                }

                vgSpeakers.addView(view);
            }
        }
    }

}
