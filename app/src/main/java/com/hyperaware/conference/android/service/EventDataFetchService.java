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

package com.hyperaware.conference.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.data.EventDataSource;
import com.hyperaware.conference.android.eventmobi.EventmobiApi;
import com.hyperaware.conference.android.eventmobi.model.AgendaItem;
import com.hyperaware.conference.android.eventmobi.model.AgendaSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.AllEventData;
import com.hyperaware.conference.android.eventmobi.model.AttendeeItem;
import com.hyperaware.conference.android.eventmobi.model.AttendeesSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.CompaniesSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.CompanyItem;
import com.hyperaware.conference.android.eventmobi.model.EventResponse;
import com.hyperaware.conference.android.eventmobi.model.MapItem;
import com.hyperaware.conference.android.eventmobi.model.MapsSectionResponse;
import com.hyperaware.conference.android.eventmobi.model.SectionResponse;
import com.hyperaware.conference.android.eventmobi.model.SpeakerItem;
import com.hyperaware.conference.android.eventmobi.model.SpeakersSectionResponse;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.mechanics.FetchException;
import com.hyperaware.conference.android.mechanics.Fetcher;
import com.hyperaware.conference.android.util.AttendeeItems;
import com.hyperaware.conference.android.util.SpeakerItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service that's responsible for fetching event data and notifying
 * UI components when complete.  (An event bus is hidden behind
 * EventDataSource.)
 */

public class EventDataFetchService extends IntentService {

    private static final Logger LOGGER = Logging.getLogger(EventDataFetchService.class);

    private static final String ME = EventDataFetchService.class.getName();
    public static final String ACTION_FETCH = ME + ".FETCH";

    private EventmobiApi api;

    private EventResponse eventResponse;
    private SectionResponse<AgendaItem> agendaSectionResponse;
    private SectionResponse<SpeakerItem> speakersSectionResponse;
    private SectionResponse<AttendeeItem> attendeesSectionResponse;
    private SectionResponse<MapItem> mapsSectionResponse;
    private SectionResponse<CompanyItem> companiesSectionResponse;

    public EventDataFetchService() {
        super(EventDataFetchService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (ACTION_FETCH.equals(action)) {
                    fetchCriticalData();
                }
            }
        }
    }

    private void fetchCriticalData() {
        EventDataSource eventDataSource = Singletons.deps.getEventDataSource();
        try {
            api = Singletons.deps.getEventmobiApi();

            eventDataSource.setFetchState(EventDataSource.FetchState.Start);

            // For simulating delays and errors
//            try {
//                Thread.sleep(2000);
//            }
//            catch (InterruptedException ignore) {
//            }
//            throw new FetchException("oopsie");

//            // TODO parallelize fetches
            fetchEvent();
            fetchAgenda();
            fetchSpeakers();
            fetchAttendees();
            fetchMaps();
            fetchSponsors();

            AllEventData data = composeAllEventData();
            eventDataSource.setAllEventData(data);
            eventDataSource.setFetchState(EventDataSource.FetchState.Done);
        }
        catch (FetchException e) {
            LOGGER.log(Level.SEVERE, "Error fetching event data", e);
            eventDataSource.setFetchState(EventDataSource.FetchState.Error);
        }
    }

    private void fetchEvent() throws FetchException {
        final Fetcher<EventResponse> fetcher = api.getEventFetcher();
        eventResponse = fetcher.fetch();
        LOGGER.fine("EventResponse: " + eventResponse.getStatus());
    }

    private void fetchAgenda() throws FetchException {
        final Fetcher<AgendaSectionResponse> fetcher = api.getAgendaSectionFetcher();
        agendaSectionResponse = fetcher.fetch();
        LOGGER.fine("AgendaSectionResponse: " + agendaSectionResponse.getStatus());
    }

    private void fetchSpeakers() throws FetchException {
        final Fetcher<SpeakersSectionResponse> fetcher = api.getSpeakersSectionFetcher();
        speakersSectionResponse = fetcher.fetch();
        LOGGER.fine("SpeakersSectionResponse: " + speakersSectionResponse.getStatus());
    }

    private void fetchAttendees() throws FetchException {
        final Fetcher<AttendeesSectionResponse> fetcher = api.getAttendeesSectionFetcher();
        attendeesSectionResponse = fetcher.fetch();
        LOGGER.fine("AttendeesSectionResponse: " + attendeesSectionResponse.getStatus());
    }

    private void fetchMaps() throws FetchException {
        final Fetcher<MapsSectionResponse> fetcher = api.getMapsSectionFetcher();
        mapsSectionResponse = fetcher.fetch();
        LOGGER.fine("MapsSectionResponse: " + mapsSectionResponse.getStatus());
    }

    private void fetchSponsors() throws FetchException {
        final Fetcher<CompaniesSectionResponse> fetcher = api.getCompaniesSectionFetcher();
        companiesSectionResponse = fetcher.fetch();
        LOGGER.fine("CompaniesSectionResponse: " + companiesSectionResponse.getStatus());
    }

    // Event data is cooked here for immediate use in the UI
    private AllEventData composeAllEventData() {
        final AllEventData data = new AllEventData();
        data.event = eventResponse.getResponse();
        data.agendaSection = agendaSectionResponse.getSection();
        data.speakersSection = speakersSectionResponse.getSection();
        data.attendeesSection = attendeesSectionResponse.getSection();
        data.mapsSection = mapsSectionResponse.getSection();
        data.companiesSection = companiesSectionResponse.getSection();

        final HashMap<String, AgendaItem> agendaMap = new HashMap<>();
        for (AgendaItem item : data.agendaSection.getItems()) {
            agendaMap.put(item.getId(), item);
        }
        data.agendaItemsById = agendaMap;

        final HashMap<String, SpeakerItem> speakerMap = new HashMap<>();
        for (SpeakerItem item : data.speakersSection.getItems()) {
            speakerMap.put(item.getId(), item);
        }
        data.speakerItemsById = speakerMap;

        final HashMap<String, AttendeeItem> attendeeMap = new HashMap<>();
        for (AttendeeItem item : data.attendeesSection.getItems()) {
            attendeeMap.put(item.getId(), item);
        }
        data.attendeeItemsById = attendeeMap;

        data.sortedSpeakers = new ArrayList<>(data.speakersSection.getItems());
        Collections.sort(data.sortedSpeakers, SpeakerItems.POSITION_COMPARATOR);

        data.sortedAttendees = new ArrayList<>(data.attendeesSection.getItems());
        Collections.sort(data.sortedAttendees, AttendeeItems.NAME_COMPARATOR);

        final HashMap<String, CompanyItem> companyMap = new HashMap<>();
        for (CompanyItem item : data.companiesSection.getItems()) {
            companyMap.put(item.getId(), item);
        }
        data.companyItemsById = companyMap;

        final HashMap<String, List<AgendaItem>> speakersItemsMap = new HashMap<>();
        for (AgendaItem item : data.agendaSection.getItems()) {
            for (String speaker_id : item.getSpeakerIds()) {
                List<AgendaItem> agendaItems = speakersItemsMap.get(speaker_id);
                if (agendaItems == null) {
                    agendaItems = new ArrayList<>();
                    speakersItemsMap.put(speaker_id, agendaItems);
                }
                agendaItems.add(item);
            }
        }
        data.speakersAgendaItems = speakersItemsMap;

        return data;
    }

    public static void start(Context context) {
        final Intent intent = new Intent(context, EventDataFetchService.class);
        intent.setAction(ACTION_FETCH);
        context.startService(intent);
    }

}
