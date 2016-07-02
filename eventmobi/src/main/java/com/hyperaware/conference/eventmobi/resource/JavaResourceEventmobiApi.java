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

package com.hyperaware.conference.eventmobi.resource;

import com.hyperaware.conference.mechanics.Fetcher;
import com.hyperaware.conference.eventmobi.EventmobiApi;
import com.hyperaware.conference.mechanics.StreamingFetcher;
import com.hyperaware.conference.eventmobi.model.EmAgendaSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmAttendeesSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmCompaniesSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmEventResponse;
import com.hyperaware.conference.eventmobi.model.EmMapsSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmSpeakersSectionResponse;
import com.hyperaware.conference.eventmobi.parser.gson.GsonParser;
import com.hyperaware.conference.eventmobi.parser.gson.GsonSectionResponseParser;

public class JavaResourceEventmobiApi implements EventmobiApi {

    private final String event;
    private final String sectionAgenda;
    private final String sectionSpeakers;
    private final String sectionAttendees;
    private final String sectionMaps;
    private final String sectionCompanies;

    public JavaResourceEventmobiApi(String event, String sectionAgenda, String sectionSpeakers, String sectionAttendees, String sectionMaps, String sectionCompanies) {
        this.event = event;
        this.sectionAgenda = sectionAgenda;
        this.sectionSpeakers = sectionSpeakers;
        this.sectionAttendees = sectionAttendees;
        this.sectionMaps = sectionMaps;
        this.sectionCompanies = sectionCompanies;
    }

    @Override
    public Fetcher<EmEventResponse> getEventFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer(event),
            new GsonParser<>(EmEventResponse.class));
    }

    @Override
    public Fetcher<EmAgendaSectionResponse> getAgendaSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer(sectionAgenda),
            new GsonSectionResponseParser<>(EmAgendaSectionResponse.class));
    }

    @Override
    public Fetcher<EmSpeakersSectionResponse> getSpeakersSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer(sectionSpeakers),
            new GsonSectionResponseParser<>(EmSpeakersSectionResponse.class));
    }

    @Override
    public Fetcher<EmAttendeesSectionResponse> getAttendeesSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer(sectionAttendees),
            new GsonSectionResponseParser<>(EmAttendeesSectionResponse.class));
    }

    @Override
    public Fetcher<EmMapsSectionResponse> getMapsSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer(sectionMaps),
            new GsonSectionResponseParser<>(EmMapsSectionResponse.class));
    }

    @Override
    public Fetcher<EmCompaniesSectionResponse> getCompaniesSectionFetcher() {
        return new StreamingFetcher<>(
            new JavaResourceStreamer(sectionCompanies),
            new GsonSectionResponseParser<>(EmCompaniesSectionResponse.class));
    }

}
