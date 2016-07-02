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

package com.hyperaware.conference.backend.config;

import com.hyperaware.conference.eventmobi.EventmobiConfig;

public class MyEventmobiConfig implements EventmobiConfig {

    private final String apiKey;
    private final String eventName;

    public MyEventmobiConfig(String api_key, String event_name) {
        this.apiKey = api_key;
        this.eventName = event_name;
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
        return "http://api.eventmobi.com/en/api/v1/events/" + eventName;
    }

    @Override
    public String getPersonImageUrl(String event_id, String image) {
        return "http://s3.amazonaws.com/eventmobi-assets/eventsbyids/" + event_id + "/people/" + image;
    }

    @Override
    public String getMapImageUrl(String event_id, String image) {
        return "http://s3.amazonaws.com/eventmobi-assets/eventsbyids/" + event_id + "/maps/" + image;
    }

    @Override
    public String getCompanyImageUrl(String event_id, String image) {
        return "http://s3.amazonaws.com/eventmobi-assets/eventsbyids/" + event_id + "/exhibitors/" + image;
    }

}
