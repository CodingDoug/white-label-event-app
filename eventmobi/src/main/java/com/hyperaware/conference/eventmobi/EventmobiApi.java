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

package com.hyperaware.conference.eventmobi;

import com.hyperaware.conference.eventmobi.model.EmAgendaSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmAttendeesSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmCompaniesSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmEventResponse;
import com.hyperaware.conference.eventmobi.model.EmMapsSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmSpeakersSectionResponse;
import com.hyperaware.conference.mechanics.Fetcher;

/**
 * Interface that describes all fetching interactions with Eventmobi.  It is
 * required that the Fetcher that returns EmEventResponse be fully invoked
 * successfully prior to the other fetchers here because EmEventResponse
 * contains the URLs for those other sections.
 */

public interface EventmobiApi {

    Fetcher<EmEventResponse> getEventFetcher();
    Fetcher<EmAgendaSectionResponse> getAgendaSectionFetcher();
    Fetcher<EmSpeakersSectionResponse> getSpeakersSectionFetcher();
    Fetcher<EmAttendeesSectionResponse> getAttendeesSectionFetcher();
    Fetcher<EmMapsSectionResponse> getMapsSectionFetcher();
    Fetcher<EmCompaniesSectionResponse> getCompaniesSectionFetcher();

}
