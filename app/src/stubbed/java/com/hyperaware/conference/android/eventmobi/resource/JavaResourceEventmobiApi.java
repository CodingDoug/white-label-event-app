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

package com.hyperaware.conference.android.eventmobi.resource;

import com.hyperaware.conference.android.eventmobi.EventmobiApi;
import com.hyperaware.conference.android.eventmobi.fetcher.fetcher.StreamingFetcher;
import com.hyperaware.conference.android.eventmobi.model.AgendaSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.AttendeesSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.CompaniesSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.EventResponse;
import com.hyperaware.conference.android.eventmobi.model.MapsSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.SpeakersSectionResponse;
import com.hyperaware.conference.android.eventmobi.parser.gson.GsonParser;
import com.hyperaware.conference.android.eventmobi.parser.gson.GsonSectionResponseParser;
import com.hyperaware.conference.android.mechanics.Fetcher;

public class JavaResourceEventmobiApi implements EventmobiApi {

    @Override
    public Fetcher<EventResponse> getEventFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer("event.json"),
            new GsonParser<>(EventResponse.class));
    }

    @Override
    public Fetcher<AgendaSectionResponse> getAgendaSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer("section_agenda.json"),
            new GsonSectionResponseParser<>(AgendaSectionResponse.class));
    }

    @Override
    public Fetcher<SpeakersSectionResponse> getSpeakersSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer("section_speakers.json"),
            new GsonSectionResponseParser<>(SpeakersSectionResponse.class));
    }

    @Override
    public Fetcher<AttendeesSectionResponse> getAttendeesSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer("section_attendees.json"),
            new GsonSectionResponseParser<>(AttendeesSectionResponse.class));
    }

    @Override
    public Fetcher<MapsSectionResponse> getMapsSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer("section_maps.json"),
            new GsonSectionResponseParser<>(MapsSectionResponse.class));
    }

    @Override
    public Fetcher<CompaniesSectionResponse> getCompaniesSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer("section_companies.json"),
            new GsonSectionResponseParser<>(CompaniesSectionResponse.class));
    }

}
