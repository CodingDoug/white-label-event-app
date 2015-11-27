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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.eventmobi.model.AllEventData;
import com.hyperaware.conference.android.eventmobi.model.Event;
import com.hyperaware.conference.android.ui.error.CommonContentController;
import com.hyperaware.conference.android.view.MutexViewGroup;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class EventInfoFragment extends Fragment implements Titled {

    private String title;
    private Bus bus;

    private MutexViewGroup vgMutex;
    private TextView tvEventName;
    private TextView tvWebsite;
    private TextView tvLocation;
    private TextView tvDescription;
    private CommonContentController contentController;

    private Event event;

    @NonNull
    public static EventInfoFragment instantiate() {
        return new EventInfoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title = getContext().getString(R.string.section_title_event_info);
        bus = Singletons.deps.getBus();
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

    @Subscribe
    public void onAllEventData(final AllEventData data) {
        event = data.event;
        updateUi();
    }

    private void updateUi() {
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
