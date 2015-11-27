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

package com.hyperaware.conference.android.eventmobi;

import com.hyperaware.conference.android.eventmobi.model.AgendaSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.AttendeesSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.CompaniesSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.EventResponse;
import com.hyperaware.conference.android.eventmobi.model.MapsSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.SpeakersSectionResponse;
import com.hyperaware.conference.android.mechanics.Fetcher;

/**
 * Interface that describes all fetching interactions with Eventmobi.  It is
 * required that the Fetcher that returns EventResponse be fully invoked
 * successfully prior to the other fetchers here because EventResponse
 * contains the URLs for those other sections.
 */

public interface EventmobiApi {

    Fetcher<EventResponse> getEventFetcher();
    Fetcher<AgendaSectionResponse> getAgendaSectionFetcher();
    Fetcher<SpeakersSectionResponse> getSpeakersSectionFetcher();
    Fetcher<AttendeesSectionResponse> getAttendeesSectionFetcher();
    Fetcher<MapsSectionResponse> getMapsSectionFetcher();
    Fetcher<CompaniesSectionResponse> getCompaniesSectionFetcher();

}
