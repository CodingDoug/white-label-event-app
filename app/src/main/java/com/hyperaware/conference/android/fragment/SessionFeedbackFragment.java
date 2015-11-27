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
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;

import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.util.Strings;

public class SessionFeedbackFragment extends WebViewFragment implements Titled {

    private static final String ARG_URL = "url";
    private static final String ARG_TITLE = "title";

    private String url;
    private String title;

    @NonNull
    public static SessionFeedbackFragment instantiate(@NonNull String url, String title) {
        if (Strings.isNullOrEmpty(url)) {
            throw new IllegalArgumentException(ARG_URL + " can't be null or empty");
        }

        final Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_TITLE, title);

        final SessionFeedbackFragment fragment = new SessionFeedbackFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        url = args.getString(ARG_URL);
        if (Strings.isNullOrEmpty(url)) {
            throw new IllegalArgumentException(ARG_URL + " can't be null or empty");
        }
        title = args.getString(ARG_TITLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            ((ContentHost) activity).setTitle(title);
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    protected void onWebViewCreated() {
        final WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(false);
    }

    @Override
    protected void onInitialState() {
        webView.loadUrl(url);
    }

    @Override
    protected WebChromeClient createWebChromeClient() {
        return null;
    }

}
