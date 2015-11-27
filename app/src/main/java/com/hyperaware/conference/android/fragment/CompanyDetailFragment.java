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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.eventmobi.EventmobiConfig;
import com.hyperaware.conference.android.eventmobi.model.AllEventData;
import com.hyperaware.conference.android.eventmobi.model.CompanyItem;
import com.hyperaware.conference.android.util.Strings;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class CompanyDetailFragment extends Fragment implements Titled {

    private static final String ARG_COMPANY_ID = "company_id";

    private String companyId;

    private Bus bus;
    private ContentHost host;

    private TextView tvName;
    private ImageView ivLogo;
    private ViewGroup vgDetailLinks;
    private TextView tvLocation, tvWebsite, tvTwitter, tvFacebook, tvLinkedin;
    private TextView tvDescription;

    private String eventId;
    private CompanyItem companyItem;

    private EventmobiConfig config = Singletons.deps.getEventmobiConfig();

    @NonNull
    public static CompanyDetailFragment instantiate(@NonNull String company_id) {
        if (Strings.isNullOrEmpty(company_id)) {
            throw new IllegalArgumentException(ARG_COMPANY_ID + " can't be null or empty");
        }

        final Bundle args = new Bundle();
        args.putString(ARG_COMPANY_ID, company_id);

        CompanyDetailFragment fragment = new CompanyDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        companyId = args.getString(ARG_COMPANY_ID);
        if (Strings.isNullOrEmpty(companyId)) {
            throw new IllegalArgumentException(ARG_COMPANY_ID + " can't be null or empty");
        }

        bus = Singletons.deps.getBus();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_company_detail, container, false);
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
        ivLogo = (ImageView) root.findViewById(R.id.iv_logo);
        tvDescription = (TextView) root.findViewById(R.id.tv_description);
        vgDetailLinks = (ViewGroup) root.findViewById(R.id.vg_detail_links);
        vgDetailLinks.setVisibility(View.GONE);
        tvLocation = (TextView) vgDetailLinks.findViewById(R.id.tv_location);
        tvLocation.setVisibility(View.GONE);
        tvWebsite = (TextView) vgDetailLinks.findViewById(R.id.tv_website);
        tvWebsite.setVisibility(View.GONE);
        tvTwitter = (TextView) vgDetailLinks.findViewById(R.id.tv_twitter);
        tvTwitter.setVisibility(View.GONE);
        tvFacebook = (TextView) vgDetailLinks.findViewById(R.id.tv_facebook);
        tvFacebook.setVisibility(View.GONE);
        tvLinkedin = (TextView) vgDetailLinks.findViewById(R.id.tv_linkedin);
        tvLinkedin.setVisibility(View.GONE);
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
        if (companyItem != null) {
            return companyItem.getName();
        }
        else {
            return null;
        }
    }

    @Subscribe
    public void onAllEventData(final AllEventData data) {
        eventId = data.event.getId();
        companyItem = data.companyItemsById.get(companyId);
        updateUi();
    }

    private void updateUi() {
        tvName.setText(companyItem.getName());
        host.setTitle(companyItem.getName());

        tvDescription.setText(companyItem.getDescription());

        boolean links_visible = false;
        String location = companyItem.getLocation();
        if (!Strings.isNullOrEmpty(location)) {
            links_visible = true;
            tvLocation.setVisibility(View.VISIBLE);
            tvLocation.setText(location);
        }
        String website = companyItem.getWebsite();
        if (!Strings.isNullOrEmpty(website)) {
            links_visible = true;
            tvWebsite.setVisibility(View.VISIBLE);
            tvWebsite.setText(website);
        }
        String twitter = companyItem.getTwitter();
        if (!Strings.isNullOrEmpty(twitter)) {
            links_visible = true;
            tvTwitter.setVisibility(View.VISIBLE);
            tvTwitter.setText(twitter);
        }
        String facebook = companyItem.getFacebook();
        if (!Strings.isNullOrEmpty(facebook)) {
            links_visible = true;
            tvFacebook.setVisibility(View.VISIBLE);
            tvFacebook.setText(twitter);
        }
        String linkedin = companyItem.getLinkedin();
        if (!Strings.isNullOrEmpty(linkedin)) {
            links_visible = true;
            tvLinkedin.setVisibility(View.VISIBLE);
            tvLinkedin.setText(twitter);
        }
        vgDetailLinks.setVisibility(links_visible ? View.VISIBLE : View.GONE);

        Glide
            .with(getActivity())
            .load(config.getCompanyImageUrl(eventId, companyItem.getLogoLargeWide()))
            .fitCenter()
            .into(ivLogo);
    }

}
