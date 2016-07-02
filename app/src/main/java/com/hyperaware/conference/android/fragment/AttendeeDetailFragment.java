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
import com.hyperaware.conference.android.data.FirebaseDatabaseHelpers;
import com.hyperaware.conference.android.util.Strings;
import com.hyperaware.conference.model.AttendeeItem;
import com.hyperaware.conference.model.Event;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttendeeDetailFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(AttendeeDetailFragment.class);

    private static final String ARG_ATTENDEE_ID = "attendee_id";

    private ContentHost host;

    private DatabaseReference eventRef;
    private DatabaseReference attendeeRef;
    private FirebaseMultiQuery firebaseMultiQuery;

    private TextView tvName;
    private TextView tvCompany;
    private TextView tvTitle;
    private ImageView ivPic;
    private ViewGroup vgDetailLinks;
    private TextView tvWebsite, tvTwitter, tvFacebook, tvLinkedin;
    private TextView tvAbout;

    private String eventId;
    private AttendeeItem attendeeItem;
    private Exception exception;

    @NonNull
    public static AttendeeDetailFragment instantiate(@NonNull String attendee_id) {
        if (Strings.isNullOrEmpty(attendee_id)) {
            throw new IllegalArgumentException(ARG_ATTENDEE_ID + " can't be null or empty");
        }

        final Bundle args = new Bundle();
        args.putString(ARG_ATTENDEE_ID, attendee_id);

        final AttendeeDetailFragment fragment = new AttendeeDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.fine("onCreate");

        final Bundle args = getArguments();
        final String attendeeId = args.getString(ARG_ATTENDEE_ID);
        if (Strings.isNullOrEmpty(attendeeId)) {
            throw new IllegalArgumentException(ARG_ATTENDEE_ID + " can't be null or empty");
        }

        final FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        eventRef = fdb.getReference("/event");
        attendeeRef = fdb.getReference("/sections/attendees/items/" + attendeeId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attendee_detail, container, false);
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
        tvAbout = (TextView) root.findViewById(R.id.tv_about);

        updateUi();
    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseMultiQuery = new FirebaseMultiQuery(eventRef, attendeeRef);
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
        if (attendeeItem != null) {
            return attendeeItem.getName();
        }
        else {
            return null;
        }
    }

    private class AllOnCompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
        @Override
        public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
            if (task.isSuccessful()) {
                final Map<DatabaseReference, DataSnapshot> result = task.getResult();
                final Event event = FirebaseDatabaseHelpers.toEvent(result.get(eventRef));
                if (event != null) {
                    eventId = event.getId();
                }
                attendeeItem = FirebaseDatabaseHelpers.toAttendeeItem(result.get(attendeeRef));
            }
            else {
                exception = task.getException();
                LOGGER.log(Level.SEVERE, "oops", exception);
            }
            updateUi();
        }
    }

    private void updateUi() {
        if (eventId != null && attendeeItem != null) {
            updateAttendeeDetail();
        }
        else if (exception != null) {
            // TODO worthwhile to handle error?
        }
        else {
            // No spinner on this page
        }
    }

    private void updateAttendeeDetail() {
        tvName.setText(attendeeItem.getName());
        host.setTitle(attendeeItem.getName());

        final String company = attendeeItem.getCompanyName();
        tvCompany.setVisibility(Strings.isNullOrEmpty(company) ? View.GONE : View.VISIBLE);
        tvCompany.setText(company);

        final String title = attendeeItem.getTitle();
        tvTitle.setVisibility(Strings.isNullOrEmpty(title) ? View.GONE : View.VISIBLE);
        tvTitle.setText(title);

        Glide
            .with(AttendeeDetailFragment.this)
            .load(attendeeItem.getImage100())
            .fitCenter()
            .placeholder(R.drawable.nopic)
            .into(ivPic);

        boolean links_visible = false;
        String website = attendeeItem.getWebsite();
        if (!Strings.isNullOrEmpty(website)) {
            links_visible = true;
            tvWebsite.setVisibility(View.VISIBLE);
            tvWebsite.setText(website);
        }
        String twitter = attendeeItem.getTwitter();
        if (!Strings.isNullOrEmpty(twitter)) {
            links_visible = true;
            tvTwitter.setVisibility(View.VISIBLE);
            tvTwitter.setText(twitter);
        }
        String facebook = attendeeItem.getFacebook();
        if (!Strings.isNullOrEmpty(facebook)) {
            links_visible = true;
            tvFacebook.setVisibility(View.VISIBLE);
            tvFacebook.setText(facebook);
        }
        String linkedin = attendeeItem.getLinkedin();
        if (!Strings.isNullOrEmpty(linkedin)) {
            links_visible = true;
            tvLinkedin.setVisibility(View.VISIBLE);
            tvLinkedin.setText(linkedin);
        }
        vgDetailLinks.setVisibility(links_visible ? View.VISIBLE : View.GONE);

        tvAbout.setText(attendeeItem.getAbout());
    }

}
