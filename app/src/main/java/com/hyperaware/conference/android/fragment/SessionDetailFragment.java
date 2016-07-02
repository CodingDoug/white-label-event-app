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
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.ui.favsession.FavSessionButtonManager;
import com.hyperaware.conference.android.util.Strings;
import com.hyperaware.conference.model.AgendaItem;
import com.hyperaware.conference.model.AgendaSection;
import com.hyperaware.conference.model.Event;
import com.hyperaware.conference.model.SpeakerItem;
import com.hyperaware.conference.model.SpeakersSection;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class SessionDetailFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(SessionDetailFragment.class);

    private static final String ARG_SESSION_ID = "session_id";

    private String sessionId;

    private FavSessionButtonManager favSessionButtonManager;
    private Bus bus;
    private ContentHost host;

    private TextView tvTopic;
    private TextView tvDate, tvTime, tvLocation;
    private ImageButton ibFavorite;
    private ViewGroup vgSpeakers;
    private TextView tvDescription;
    private ViewGroup vgActions;

    private Event event;
    private AgendaItem agendaItem;
    private SpeakersSection speakers;
    private ArrayList<SpeakerItem> speakerItems;
    private TimeZone tz;

    @NonNull
    public static SessionDetailFragment instantiate(@NonNull String session_id) {
        if (Strings.isNullOrEmpty(session_id)) {
            throw new IllegalArgumentException(ARG_SESSION_ID + " can't be null or empty");
        }

        final Bundle args = new Bundle();
        args.putString(ARG_SESSION_ID, session_id);

        final SessionDetailFragment fragment = new SessionDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.fine("onCreate");

        final Bundle args = getArguments();
        sessionId = args.getString(ARG_SESSION_ID);
        if (Strings.isNullOrEmpty(sessionId)) {
            throw new IllegalArgumentException(ARG_SESSION_ID + " can't be null or empty");
        }

        favSessionButtonManager = new FavSessionButtonManager(
            FirebaseDatabase.getInstance(), FirebaseAuth.getInstance(), new MyAuthRequiredListener());

        bus = Singletons.deps.getBus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            host = (ContentHost) activity;
            host.setTitle(null);
        }

        final View root = getView();
        if (root == null) {
            throw new IllegalStateException();
        }

        tvTopic = (TextView) root.findViewById(R.id.tv_topic);
        tvDate = (TextView) root.findViewById(R.id.tv_date);
        tvTime = (TextView) root.findViewById(R.id.tv_time);
        tvLocation = (TextView) root.findViewById(R.id.tv_location);
        ibFavorite = (ImageButton) root.findViewById(R.id.button_favorite_session);
        vgSpeakers = (ViewGroup) root.findViewById(R.id.vg_speakers);
        vgSpeakers.setVisibility(View.GONE);
        vgSpeakers.removeAllViews();
        vgActions = (ViewGroup) root.findViewById(R.id.vg_actions);
        vgActions.setVisibility(View.GONE);
        tvDescription = (TextView) root.findViewById(R.id.tv_description);

        updateUi();
    }

    @Override
    public void onStart() {
        super.onStart();

        bus.register(this);

        favSessionButtonManager.start();
        favSessionButtonManager.attach(ibFavorite, sessionId);
    }

    @Override
    public void onStop() {
        favSessionButtonManager.detach(ibFavorite, sessionId);
        favSessionButtonManager.stop();

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
    public void onEvent(final Event event) {
        if (this.event == null && event != null) {
            this.event = event;
            tz = TimeZone.getTimeZone(event.getTimezoneName());
            updateUi();
        }
    }

    @Subscribe
    public void onAgenda(final AgendaSection agenda) {
        if (this.agendaItem == null && agenda != null) {
            // TODO rarely ever a session not found
            agendaItem = agenda.getItems().get(sessionId);
            updateUi();
        }
    }

    @Subscribe
    public void onSpeakers(final SpeakersSection speakers) {
        if (this.speakers == null && speakers != null) {
            this.speakers = speakers;
            updateUi();
        }
    }

    private void updateUi() {
        if (event != null && agendaItem != null && speakers != null) {
            final HashMap<String, SpeakerItem> speakerMap = new HashMap<>();
            for (final SpeakerItem item : speakers.getItems().values()) {
                speakerMap.put(item.getId(), item);
            }

            speakerItems = new ArrayList<>();
            // TODO sanitize null session speaker ids with empty collections.
            final List<String> speakerIds = agendaItem.getSpeakerIds();
            if (speakerIds != null) {
                for (final String id : speakerIds) {
                    final SpeakerItem item = speakerMap.get(id);
                    if (item != null) {
                        speakerItems.add(item);
                    }
                }
            }

            updateSessionDetail();
        }
    }

    private void updateSessionDetail() {
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

        // Only sessions with speakers can have feedback
        final View feedback = vgActions.findViewById(R.id.tv_session_feedback);
        if (speakerItems.size() > 0) {
            feedback.setOnClickListener(new SessionFeedbackOnClickListener());
            vgActions.setVisibility(View.VISIBLE);
        }
        else {
            vgActions.setVisibility(View.GONE);
        }

        if (speakerItems.size() > 0) {
            vgSpeakers.setVisibility(View.VISIBLE);
            vgSpeakers.removeAllViews();
            final LayoutInflater inflater = getActivity().getLayoutInflater();

            for (final SpeakerItem item : speakerItems) {
                final View view = inflater.inflate(R.layout.item_session_speaker, vgSpeakers, false);

                ImageView iv = (ImageView) view.findViewById(R.id.iv_pic);
                Glide
                    .with(SessionDetailFragment.this)
                    .load(item.getImage100())
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


    private class SessionFeedbackOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                final String title_session_feedback = getString(R.string.fmt_title_session_feedback, agendaItem.getTopic());
                host.pushFragment(SessionFeedbackFragment.instantiate(sessionId, title_session_feedback), null);
            }
            else {
                new SigninRequiredDialogFragment().show(getFragmentManager(), null);
            }
        }
    }

    private class MyAuthRequiredListener implements FavSessionButtonManager.AuthRequiredListener {
        @Override
        public void onAuthRequired(ImageButton view, String sessionId) {
            new SigninRequiredDialogFragment().show(getFragmentManager(), null);
        }
    }

}
