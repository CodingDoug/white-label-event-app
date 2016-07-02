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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.ui.favsession.FavSessionButtonManager;
import com.hyperaware.conference.android.util.AgendaItems;
import com.hyperaware.conference.android.util.Strings;
import com.hyperaware.conference.model.AgendaItem;
import com.hyperaware.conference.model.AgendaSection;
import com.hyperaware.conference.model.Event;
import com.hyperaware.conference.model.SpeakerItem;
import com.hyperaware.conference.model.SpeakersSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.TimeZone;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class SpeakerDetailFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(SpeakerDetailFragment.class);

    private static final String ARG_SPEAKER_ID = "speaker_id";

    private String speakerId;

    private FavSessionButtonManager favSessionButtonManager;
    private Bus bus;
    private ContentHost host;

    private TextView tvName;
    private TextView tvCompany;
    private TextView tvTitle;
    private ImageView ivPic;
    private ViewGroup vgDetailLinks;
    private ViewGroup vgSessions;
    private TextView tvWebsite, tvTwitter, tvFacebook, tvLinkedin;
    private TextView tvAbout;

    private Event event;
    private TimeZone tz;
    private SpeakerItem speakerItem;
    private ArrayList<AgendaItem> agendaItems;

    @NonNull
    public static SpeakerDetailFragment instantiate(@NonNull String speaker_id) {
        if (Strings.isNullOrEmpty(speaker_id)) {
            throw new IllegalArgumentException(ARG_SPEAKER_ID + " can't be null or empty");
        }

        final Bundle args = new Bundle();
        args.putString(ARG_SPEAKER_ID, speaker_id);

        final SpeakerDetailFragment fragment = new SpeakerDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.fine("onCreate");

        final Bundle args = getArguments();
        speakerId = args.getString(ARG_SPEAKER_ID);
        if (Strings.isNullOrEmpty(speakerId)) {
            throw new IllegalArgumentException(ARG_SPEAKER_ID + " can't be null or empty");
        }

        favSessionButtonManager = new FavSessionButtonManager(
            FirebaseDatabase.getInstance(), FirebaseAuth.getInstance(), new MyAuthRequiredListener());

        bus = Singletons.deps.getBus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speaker_detail, container, false);
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

        tvName = (TextView) root.findViewById(R.id.tv_name);
        tvCompany = (TextView) root.findViewById(R.id.tv_company);
        tvTitle = (TextView) root.findViewById(R.id.tv_title);
        ivPic = (ImageView) root.findViewById(R.id.iv_pic);
        vgDetailLinks = (ViewGroup) root.findViewById(R.id.vg_detail_links);
        vgDetailLinks.setVisibility(View.GONE);
        tvWebsite = (TextView) vgDetailLinks.findViewById(R.id.tv_website);
        tvWebsite.setVisibility(View.GONE);
        tvTwitter = (TextView) vgDetailLinks.findViewById(R.id.tv_twitter);
        tvTwitter.setVisibility(View.GONE);
        tvFacebook = (TextView) vgDetailLinks.findViewById(R.id.tv_facebook);
        tvFacebook.setVisibility(View.GONE);
        tvLinkedin = (TextView) vgDetailLinks.findViewById(R.id.tv_linkedin);
        tvLinkedin.setVisibility(View.GONE);
        vgSessions = (ViewGroup) root.findViewById(R.id.vg_sessions);
        vgSessions.setVisibility(View.GONE);
        vgSessions.removeAllViews();
        tvAbout = (TextView) root.findViewById(R.id.tv_about);

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

    @Nullable
    @Override
    public String getTitle() {
        if (speakerItem != null) {
            return speakerItem.getName();
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
        if (this.agendaItems == null && agenda != null) {
            agendaItems = new ArrayList<>();
            final Collection<AgendaItem> values = agenda.getItems().values();
            for (final AgendaItem item : values) {
                if (item.getSpeakerIds().contains(speakerId)) {
                    agendaItems.add(item);
                }
            }
            Collections.sort(agendaItems, AgendaItems.START_TIME_COMPARATOR);
            updateUi();
        }
    }

    @Subscribe
    public void onSpeakers(final SpeakersSection speakers) {
        if (this.speakerItem == null && speakers != null) {
            speakerItem = speakers.getItems().get(speakerId);
            updateUi();
        }
    }

    private void updateUi() {
        if (event != null && agendaItems != null && speakerItem != null) {
            updateSpeaker();
        }
        else {
        }
    }

    private void updateSpeaker() {
        tvName.setText(speakerItem.getName());
        host.setTitle(speakerItem.getName());

        final String company = speakerItem.getCompanyName();
        tvCompany.setVisibility(Strings.isNullOrEmpty(company) ? View.GONE : View.VISIBLE);
        tvCompany.setText(company);

        final String title = speakerItem.getTitle();
        tvTitle.setVisibility(Strings.isNullOrEmpty(title) ? View.GONE : View.VISIBLE);
        tvTitle.setText(title);

        Glide
            .with(SpeakerDetailFragment.this)
            .load(speakerItem.getImage100())
            .fitCenter()
            .placeholder(R.drawable.nopic)
            .into(ivPic);

        boolean links_visible = false;
        final String website = speakerItem.getWebsite();
        if (!Strings.isNullOrEmpty(website)) {
            links_visible = true;
            tvWebsite.setVisibility(View.VISIBLE);
            tvWebsite.setText(website);
        }
        final String twitter = speakerItem.getTwitter();
        if (!Strings.isNullOrEmpty(twitter)) {
            links_visible = true;
            tvTwitter.setVisibility(View.VISIBLE);
            tvTwitter.setText(twitter);
        }
        final String facebook = speakerItem.getFacebook();
        if (!Strings.isNullOrEmpty(facebook)) {
            links_visible = true;
            tvFacebook.setVisibility(View.VISIBLE);
            tvFacebook.setText(facebook);
        }
        final String linkedin = speakerItem.getLinkedin();
        if (!Strings.isNullOrEmpty(linkedin)) {
            links_visible = true;
            tvLinkedin.setVisibility(View.VISIBLE);
            tvLinkedin.setText(linkedin);
        }
        vgDetailLinks.setVisibility(links_visible ? View.VISIBLE : View.GONE);

        tvAbout.setText(speakerItem.getAbout());

        if (agendaItems.size() > 0) {
            vgSessions.setVisibility(View.VISIBLE);
            vgSessions.removeAllViews();
            final StringBuilder sb = new StringBuilder();
            final Formatter formatter = new Formatter(sb);
            final LayoutInflater inflater = getActivity().getLayoutInflater();
            for (final AgendaItem item : agendaItems) {
                final View view = inflater.inflate(R.layout.item_speaker_session, vgSessions, false);

                ((TextView) view.findViewById(R.id.tv_topic)).setText(item.getTopic());

                sb.setLength(0);
                DateUtils.formatDateRange(
                    getActivity(),
                    formatter,
                    item.getEpochStartTime() * 1000,
                    item.getEpochEndTime() * 1000,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY,
                    tz.getID()
                );
                ((TextView) view.findViewById(R.id.tv_date)).setText(formatter.toString());

                sb.setLength(0);
                DateUtils.formatDateRange(
                    getActivity(),
                    formatter,
                    item.getEpochStartTime() * 1000,
                    item.getEpochEndTime() * 1000,
                    DateUtils.FORMAT_SHOW_TIME,
                    tz.getID()
                );
                ((TextView) view.findViewById(R.id.tv_time)).setText(formatter.toString());

                final String session_id = item.getId();
                final ImageButton ib_favorite = (ImageButton) view.findViewById(R.id.button_favorite_session);
                favSessionButtonManager.attach(ib_favorite, session_id);

                if (host != null) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Fragment next = SessionDetailFragment.instantiate(item.getId());
                            host.pushFragment(next, "session_detail");
                        }
                    });
                }

                vgSessions.addView(view);
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
