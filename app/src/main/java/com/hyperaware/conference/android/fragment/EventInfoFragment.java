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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.hyperaware.conference.android.fdb.FirebaseMultiQuery;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.data.FirebaseDatabaseHelpers;
import com.hyperaware.conference.android.view.MutexViewGroup;
import com.hyperaware.conference.model.Event;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventInfoFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(EventInfoFragment.class);

    private String title;

    private DatabaseReference eventRef;
    private FirebaseMultiQuery firebaseMultiQuery;

    private MutexViewGroup vgMutex;
    private TextView tvEventName;
    private TextView tvWebsite;
    private TextView tvLocation;
    private TextView tvDescription;

    private Event event;
    private Exception exception;

    @NonNull
    public static EventInfoFragment instantiate() {
        return new EventInfoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.fine("onCreate");

        title = getContext().getString(R.string.section_title_event_info);

        final FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        eventRef = fdb.getReference("/event");
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_info, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            ((ContentHost) activity).setTitle(title);
        }

        View root = getView();
        if (root == null) {
            throw new IllegalStateException();
        }

        vgMutex = (MutexViewGroup) root.findViewById(R.id.vg_mutex);
        tvEventName = (TextView) root.findViewById(R.id.tv_event_name);
        tvWebsite = (TextView) root.findViewById(R.id.tv_website);
        tvLocation = (TextView) root.findViewById(R.id.tv_location);
        tvDescription = (TextView) root.findViewById(R.id.tv_description);
    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseMultiQuery = new FirebaseMultiQuery(eventRef);
        final Task<Map<DatabaseReference, DataSnapshot>> allLoad = firebaseMultiQuery.start();
        allLoad.addOnCompleteListener(getActivity(), new AllOnCompleteListener());
    }

    @Override
    public void onStop() {
        firebaseMultiQuery.stop();
        super.onStop();
    }

    private class AllOnCompleteListener implements OnCompleteListener<Map<DatabaseReference, DataSnapshot>> {
        @Override
        public void onComplete(@NonNull Task<Map<DatabaseReference, DataSnapshot>> task) {
            if (task.isSuccessful()) {
                event = FirebaseDatabaseHelpers.toEvent(task.getResult().get(eventRef));
            }
            else {
                exception = task.getException();
                LOGGER.log(Level.SEVERE, "oops", exception);
            }
            updateUi();
        }
    }

    private void updateUi() {
        if (event != null) {
            updateEventInfo();
        }
        else if (exception != null) {
            vgMutex.showViewId(R.id.vg_data_error);
        }
        else {
            vgMutex.showViewId(R.id.pb);
        }
    }

    private void updateEventInfo() {
        vgMutex.showViewId(R.id.v_content);
        tvEventName.setText(event.getFullName());
        tvWebsite.setText(event.getWebsite());
        final String location = getString(R.string.fmt_name_and_address, event.getLocationName(), event.getLocationAddress());
        tvLocation.setText(location);
        tvLocation.setOnClickListener(new LocationOnClickListener(event.getLocationAddress()));
        tvDescription.setText(event.getDescription());
    }


    private class LocationOnClickListener implements View.OnClickListener {
        private final String location;

        public LocationOnClickListener(String location) {
            this.location = location;
        }

        @Override
        public void onClick(View v) {
            try {
                // TODO check to see if this intent can even be handled on this device
                // before launching it.
                String uri = "geo:0,0?q=" + URLEncoder.encode(location, "UTF-8");
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }
            catch (UnsupportedEncodingException ignore) {
            }
        }
    }

}
