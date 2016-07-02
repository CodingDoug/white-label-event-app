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

package com.hyperaware.conference.android.util;

import android.content.Context;

import com.hyperaware.conference.android.R;

public class TwitterConfig {

    private static TwitterConfig config;

    public static void init(Context context) {
        config = new TwitterConfig(
            context.getString(R.string.twitter_api_key),
            context.getString(R.string.twitter_api_secret),
            context.getString(R.string.event_hashtag)
        );
    }

    public static TwitterConfig getInstance() {
        return config;
    }


    private final String apiKey;
    private final String apiSecret;
    private final String eventHashtag;
    private final boolean isConfigured;

    private TwitterConfig(String apiKey, String apiSecret, String eventHashtag) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.eventHashtag = eventHashtag;
        this.isConfigured = !(
            Strings.isNullOrEmpty(apiKey) ||
            Strings.isNullOrEmpty(apiSecret) ||
            Strings.isNullOrEmpty(eventHashtag));
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getEventHashtag() {
        return eventHashtag;
    }

    public boolean isConfigured() {
        return isConfigured;
    }

}
