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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.activity.ContentHost;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.util.Strings;
import com.hyperaware.conference.android.view.MutexViewGroup;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterApiException;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.internal.TwitterApiConstants;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;

import java.util.logging.Level;
import java.util.logging.Logger;

import retrofit2.Call;

public class TweetsFragment extends Fragment implements Titled {

    private static final Logger LOGGER = Logging.getLogger(TweetsFragment.class);

    private static final String ARG_HASHTAG = "hashtag";

    private String hashtag;

    private MutexViewGroup vgMutex;
    private RecyclerView rv;

    private TwitterCore core;
    private TwitterSession session;
    private TwitterApiClient client;

    @NonNull
    public static TweetsFragment instantiate(@NonNull String hashtag) {
        if (Strings.isNullOrEmpty(hashtag)) {
            throw new IllegalArgumentException(ARG_HASHTAG + " can't be null or empty");
        }

        Bundle args = new Bundle();
        args.putString(ARG_HASHTAG, hashtag);

        TweetsFragment fragment = new TweetsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        hashtag = args.getString(ARG_HASHTAG);
        if (Strings.isNullOrEmpty(hashtag)) {
            throw new IllegalArgumentException(ARG_HASHTAG + " can't be null or empty");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tweets, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            ((ContentHost) activity).setTitle(hashtag);
        }

        View root = getView();
        if (root == null) {
            throw new IllegalStateException();
        }

        vgMutex = (MutexViewGroup) root.findViewById(R.id.vg_mutex);
        vgMutex.showViewId(R.id.pb);

        rv = (RecyclerView) root.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        initTwitterSearch();
    }

    @Nullable
    @Override
    public String getTitle() {
        return hashtag;
    }

    //
    // Twitter stuff
    //

    private void initTwitterSearch() {
        core = TwitterCore.getInstance();
        session = core.getSessionManager().getActiveSession();
        if (session != null) {
            client = core.getApiClient(session);
        }
        else {
            client = core.getGuestApiClient();
        }

        if (client != null) {
            doSearch();
        }
    }

    private void doSearch() {
        final Call<Search> search = client.getSearchService()
            .tweets(hashtag, null, null, null, null, 50, null, null, null, true);
        search.enqueue(new SearchCallback());
    }

    private class SearchCallback extends Callback<Search> {
        @Override
        public void success(Result<Search> result) {
            if (result.data.tweets.size() > 0) {
                rv.setAdapter(new TweetsAdapter(result.data));
                vgMutex.showView(rv);
            }
            else {
                TextView tv = (TextView) vgMutex.findViewById(R.id.tv_no_tweets);
                tv.setText(getString(R.string.fmt_message_no_tweets, hashtag));
                vgMutex.showView(tv);
            }
        }

        @Override
        public void failure(TwitterException e) {
            final TwitterApiException apiException = (TwitterApiException) e;
            final int errorCode = apiException.getErrorCode();
            LOGGER.log(Level.SEVERE, "error code " + errorCode, apiException);
            if (errorCode == TwitterApiConstants.Errors.APP_AUTH_ERROR_CODE || errorCode == TwitterApiConstants.Errors.GUEST_AUTH_ERROR_CODE) {
                // Session is bad, so clear existing sessions and retry
                // TODO max retries to prevent looping
                core.getSessionManager().clearActiveSession();
                initTwitterSearch();
            }
            else {
                vgMutex.showViewId(R.id.tv_twitter_data_error);
            }
        }
    }

    //
    // RecyclerView stuff
    //

    private class TweetsAdapter extends RecyclerView.Adapter {

        private static final int TYPE_TWEET = 0;

        private final Search search;

        public TweetsAdapter(Search search) {
            this.search = search;
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_TWEET;
        }

        @Override
        public int getItemCount() {
            return search.tweets.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
            case TYPE_TWEET:
                // View won't init properly without some tweet given at time of construction.
                // The binding will change its tweet later.
                CompactTweetView view = new CompactTweetView(parent.getContext(), search.tweets.get(0));
                return new TweetsItemViewHolder(view);
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
            case TYPE_TWEET:
                TweetsItemViewHolder tivh = (TweetsItemViewHolder) holder;
                tivh.bindTweet(search.tweets.get(position));
                break;
            }
        }
    }

    private class TweetsItemViewHolder extends RecyclerView.ViewHolder {
        private final CompactTweetView view;

        public TweetsItemViewHolder(CompactTweetView view) {
            super(view);
            this.view = view;
        }

        public void bindTweet(Tweet tweet) {
            view.setTweet(tweet);
        }
    }

}
