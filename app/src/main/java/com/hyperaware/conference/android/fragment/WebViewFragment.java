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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.view.MutexViewGroup;

public abstract class WebViewFragment extends Fragment {

    private MutexViewGroup vgMutex;
    private ProgressBar pb;
    protected WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View root = getView();
        if (root == null) {
            throw new IllegalStateException();
        }

        vgMutex = (MutexViewGroup) root.findViewById(R.id.vg_mutex);
        vgMutex.showViewId(R.id.pb);

        pb = (ProgressBar) vgMutex.findViewById(R.id.pb);
        pb.setIndeterminate(false);
        pb.setMax(100);  // WebChromeClient progress

        webView = (WebView) vgMutex.findViewById(R.id.webview);
        onWebViewCreated();

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }
        else {
            initWebView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onPause() {
        webView.onPause();
        super.onPause();
    }

    protected abstract void onWebViewCreated();

    protected abstract WebChromeClient createWebChromeClient();

    protected abstract void onInitialState();


    private void initWebView() {
        webView.setWebChromeClient(new MyWebChromeClient(createWebChromeClient()));
        onInitialState();
    }

    private class MyWebChromeClient extends WebChromeClient {
        private final WebChromeClient wrapped;

        public MyWebChromeClient(WebChromeClient wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (wrapped != null) {
                wrapped.onProgressChanged(view, newProgress);
            }
            pb.setProgress(newProgress);
            if (newProgress >= 100) {
                vgMutex.showView(webView);
            }
        }
    }

}
