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

package com.hyperaware.conference.android.eventmobi.fetcher.http;

import android.app.Application;
import android.content.res.Resources;

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.eventmobi.EventmobiConfig;

import java.util.Locale;

/**
 * Configuration for Eventmobi interaction that loads values from Android
 * resources.
 */

public class HttpEventmobiConfig implements EventmobiConfig {

    private final Resources res;
    private final String apiKey;
    private final String eventName;
    private final String eventApiUrl;

    public HttpEventmobiConfig(Application context) {
        res = context.getResources();
        apiKey = res.getString(R.string.eventmobi_api_key);
        eventName = res.getString(R.string.eventmobi_event_name);
        String prefix = res.getString(R.string.eventmobi_api_url_prefix);
        eventApiUrl = prefix + '/' + eventName;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public String getEventApiUrl() {
        return eventApiUrl;
    }

    @Override
    public String getPersonImageUrl(String event_id, String image) {
        return res.getString(R.string.eventmobi_person_image_url_format, event_id, image);
    }

    @Override
    public String getMapImageUrl(String event_id, String image) {
        return res.getString(R.string.eventmobi_map_image_url_format, event_id, image);
    }

    @Override
    public String getCompanyImageUrl(String event_id, String image) {
        return res.getString(R.string.eventmobi_company_image_url_format, event_id, image);
    }

}
