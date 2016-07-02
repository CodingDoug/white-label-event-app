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

package com.hyperaware.conference.backend;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.hyperaware.conference.backend.config.ResourcesConfigStrategy;
import com.hyperaware.conference.eventmobi.EventmobiApi;
import com.hyperaware.conference.eventmobi.EventmobiConfig;
import com.hyperaware.conference.eventmobi.http.HttpEventmobiApi;
import com.hyperaware.conference.eventmobi.model.EmAgendaItem;
import com.hyperaware.conference.eventmobi.model.EmAttendeeItem;
import com.hyperaware.conference.eventmobi.model.EmCompanyItem;
import com.hyperaware.conference.eventmobi.model.EmEvent;
import com.hyperaware.conference.eventmobi.model.EmMapItem;
import com.hyperaware.conference.eventmobi.model.EmSection;
import com.hyperaware.conference.eventmobi.model.EmSpeakerItem;
import com.hyperaware.conference.model.AgendaItem;
import com.hyperaware.conference.model.AttendeeItem;
import com.hyperaware.conference.model.CompanyItem;
import com.hyperaware.conference.model.Event;
import com.hyperaware.conference.model.Item;
import com.hyperaware.conference.model.MapItem;
import com.hyperaware.conference.model.Section;
import com.hyperaware.conference.model.SpeakerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Populate {

    private final FirebaseDatabase fdb;
    private final EventmobiConfig eventmobiConfig;
    private final EventmobiApi api;
    private final ArrayList<Task<Void>> allWrites = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        final ResourcesConfigStrategy configStrategy = new ResourcesConfigStrategy();
        configStrategy.configure();

        final FirebaseDatabase fdb = configStrategy.getFirebaseDatabase();
        final EventmobiConfig eventmobiConfig = configStrategy.getEventmobiConfig();
        System.out.println("Firebase database: " + fdb.getReference());
        System.out.println("Eventmobi API key: " + eventmobiConfig.getApiKey());
        System.out.println("Eventmobi event name: " + eventmobiConfig.getEventName());

        final Populate populate = new Populate(fdb, eventmobiConfig);
        populate.run();

        // Necessary to force Firebase thread to quit
        System.exit(0);
    }

    public Populate(FirebaseDatabase fdb, EventmobiConfig eventmobiConfig) {
        this.fdb = fdb;
        this.eventmobiConfig = eventmobiConfig;
        this.api = new HttpEventmobiApi(eventmobiConfig);
    }

    public void run() throws Exception {
        populate();
        System.out.println("Waiting for writes to complete");
        Tasks.await(Tasks.whenAll(allWrites));
        System.out.println("DONE");
    }

    private void populate() throws Exception {
        populateEvent();
        populateAgenda();
        populateSpeakers();
        populateAttendees();
        populateMaps();
        populateCompanies();
    }

    private void populateEvent() throws Exception {
        System.out.println("Populating Event");
        final EmEvent em_event = api.getEventFetcher().fetch().getResponse();

        final Event event = new Event();
        event.setId(assertNotEmpty(em_event.getId()));
        event.setName(assertNotEmpty(em_event.getName()));
        event.setFullName(assertNotEmpty(em_event.getFullName()));
        event.setLocationName(nullIfEmpty(em_event.getLocationName()));
        event.setLocationAddress(nullIfEmpty(em_event.getLocationAddress()));
        event.setWebsite(nullIfEmpty(em_event.getWebsite()));
        event.setTimezoneName(assertNotEmpty(em_event.getTimezoneName()));
        event.setDescription(assertNotEmpty(em_event.getDescription()));

        final List<EmSection> em_sections = em_event.getSections();
        ArrayList<Section<Item>> sections = new ArrayList<>(em_sections.size());
        for (EmSection em_section : em_sections) {
            Section<Item> section = new Section<>();
            section.setId(assertNotEmpty(em_section.getId()));
            section.setName(assertNotEmpty(em_section.getName()));
            section.setType(assertNotEmpty(em_section.getType()));
            sections.add(section);
        }
        event.setSections(sections);

        final Task<Void> task = fdb.getReference("event").setValue(event);
        allWrites.add(task);
        System.out.println("Number of sections: " + sections.size());
    }

    private void populateAgenda() throws Exception {
        System.out.println("Populating Agenda");
        final EmSection<EmAgendaItem> em_agenda = api.getAgendaSectionFetcher().fetch().getSection();

        final Section<AgendaItem> agenda = new Section<>();
        copyBaseSection(em_agenda, agenda);

        final HashMap<String, AgendaItem> items = new HashMap<>(em_agenda.getItems().size());
        for (EmAgendaItem em_item : em_agenda.getItems()) {
            final AgendaItem item = new AgendaItem();
            item.setId(assertNotEmpty(em_item.getId()));
            item.setTopic(assertNotEmpty(em_item.getTopic()));
            item.setDescription(nullIfEmpty(em_item.getDescription()));
            item.setLocation(nullIfEmpty(em_item.getLocation()));
            item.setRawDate(em_item.getRawDate());
            item.setEpochStartTime(em_item.getEpochStartTime());
            item.setEpochEndTime(em_item.getEpochEndTime());
            item.setGroupIds(emptyIfNull(em_item.getGroupIds()));
            item.setSpeakerIds(emptyIfNull(em_item.getSpeakerIds()));
            items.put(item.getId(), item);
        }
        agenda.setItems(items);

        final Task<Void> task = fdb.getReference("sections/agenda").setValue(agenda);
        allWrites.add(task);
        System.out.println("Number of agenda items: " + items.size());
    }

    private void populateSpeakers() throws Exception {
        System.out.println("Populating Speakers");
        final EmSection<EmSpeakerItem> em_speakers = api.getSpeakersSectionFetcher().fetch().getSection();
        assertNotEmpty(em_speakers.getEventId());

        final Section<SpeakerItem> speakers = new Section<>();
        copyBaseSection(em_speakers, speakers);

        final HashMap<String, SpeakerItem> items = new HashMap<>(em_speakers.getItems().size());
        for (EmSpeakerItem em_item : em_speakers.getItems()) {
            final SpeakerItem item = new SpeakerItem();
            item.setId(assertNotEmpty(em_item.getId()));
            item.setPosition(em_item.getPosition());
            item.setName(assertNotEmpty(em_item.getName()));
            item.setCompanyName(nullIfEmpty(em_item.getCompanyName()));
            item.setTitle(nullIfEmpty(em_item.getTitle()));

            String image50 = nullIfEmpty(em_item.getImage50());
            if (image50 != null) {
                image50 = eventmobiConfig.getPersonImageUrl(em_speakers.getEventId(), image50);
            }
            item.setImage50(image50);

            String image100 = nullIfEmpty(em_item.getImage100());
            if (image100 != null) {
                image100 = eventmobiConfig.getPersonImageUrl(em_speakers.getEventId(), image100);
            }
            item.setImage100(image100);

            item.setAbout(nullIfEmpty(em_item.getAbout()));
            item.setWebsite(nullIfEmpty(em_item.getWebsite()));
            item.setFacebook(nullIfEmpty(em_item.getFacebook()));
            item.setTwitter(nullIfEmpty(em_item.getTwitter()));
            item.setLinkedin(nullIfEmpty(em_item.getLinkedin()));
            items.put(item.getId(), item);
        }
        speakers.setItems(items);

        final Task<Void> task = fdb.getReference("sections/speakers").setValue(speakers);
        allWrites.add(task);
        System.out.println("Number of speakers: " + items.size());
    }

    private void populateAttendees() throws Exception {
        System.out.println("Populating Attendees");
        final EmSection<EmAttendeeItem> em_attendees = api.getAttendeesSectionFetcher().fetch().getSection();
        assertNotEmpty(em_attendees.getEventId());

        final Section<AttendeeItem> attendees = new Section<>();
        copyBaseSection(em_attendees, attendees);

        final HashMap<String, AttendeeItem> items = new HashMap<>(em_attendees.getItems().size());
        for (EmAttendeeItem em_item : em_attendees.getItems()) {
            final AttendeeItem item = new AttendeeItem();
            item.setId(assertNotEmpty(em_item.getId()));
            item.setName(assertNotEmpty(em_item.getName()));
            item.setCompanyName(nullIfEmpty(em_item.getCompanyName()));
            item.setTitle(nullIfEmpty(em_item.getTitle()));

            String image50 = nullIfEmpty(em_item.getImage50());
            if (image50 != null) {
                image50 = eventmobiConfig.getPersonImageUrl(em_attendees.getEventId(), image50);
            }
            item.setImage50(image50);

            String image100 = nullIfEmpty(em_item.getImage100());
            if (image100 != null) {
                image100 = eventmobiConfig.getPersonImageUrl(em_attendees.getEventId(), image100);
            }
            item.setImage100(image100);

            item.setAbout(nullIfEmpty(em_item.getAbout()));
            item.setWebsite(nullIfEmpty(em_item.getWebsite()));
            item.setFacebook(nullIfEmpty(em_item.getFacebook()));
            item.setTwitter(nullIfEmpty(em_item.getTwitter()));
            item.setLinkedin(nullIfEmpty(em_item.getLinkedin()));
            items.put(item.getId(), item);
        }
        attendees.setItems(items);

        final Task<Void> task = fdb.getReference("sections/attendees").setValue(attendees);
        allWrites.add(task);
        System.out.println("Number of attendees: " + items.size());
    }

    private void populateMaps() throws Exception {
        System.out.println("Populating Maps");
        final EmSection<EmMapItem> em_maps = api.getMapsSectionFetcher().fetch().getSection();
        assertNotEmpty(em_maps.getEventId());

        final Section<MapItem> maps = new Section<>();
        copyBaseSection(em_maps, maps);

        final HashMap<String, MapItem> items = new HashMap<>(em_maps.getItems().size());
        for (EmMapItem em_item : em_maps.getItems()) {
            final MapItem item = new MapItem();
            item.setId(assertNotEmpty(em_item.getId()));
            item.setName(assertNotEmpty(em_item.getName()));

            String filename = nullIfEmpty(em_item.getFilename());
            if (filename != null) {
                filename = eventmobiConfig.getMapImageUrl(em_maps.getEventId(), filename);
            }
            item.setFilename(filename);

            item.setGoogleMap(em_item.isGoogleMap());
            item.setGoogleMapUrl(nullIfEmpty(em_item.getGoogleMapUrl()));
            items.put(item.getId(), item);
        }
        maps.setItems(items);

        final Task<Void> task = fdb.getReference("sections/maps").setValue(maps);
        allWrites.add(task);
        System.out.println("Number of maps: " + items.size());
    }

    private void populateCompanies() throws Exception {
        System.out.println("Populating Companies");
        final EmSection<EmCompanyItem> em_companies = api.getCompaniesSectionFetcher().fetch().getSection();

        final Section<CompanyItem> companies = new Section<>();
        companies.setId(em_companies.getId());
        companies.setName(em_companies.getName());
        companies.setType(em_companies.getType());

        final HashMap<String, CompanyItem> items = new HashMap<>(em_companies.getItems().size());
        for (EmCompanyItem em_item : em_companies.getItems()) {
            final CompanyItem item = new CompanyItem();
            item.setId(assertNotEmpty(em_item.getId()));
            item.setName(assertNotEmpty(em_item.getName()));
            item.setDescription(nullIfEmpty(em_item.getDescription()));
            item.setPosition(em_item.getPosition());
            item.setBooth(nullIfEmpty(em_item.getBooth()));
            item.setLocation(nullIfEmpty(em_item.getLocation()));

            {
                String logo = nullIfEmpty(em_item.getLogoLarge());
                if (logo != null) {
                    logo = eventmobiConfig.getCompanyImageUrl(em_companies.getEventId(), logo);
                }
                item.setLogoLarge(logo);
            }

            {
                String logo = nullIfEmpty(em_item.getLogoLargeWide());
                if (logo != null) {
                    logo = eventmobiConfig.getCompanyImageUrl(em_companies.getEventId(), logo);
                }
                item.setLogoLargeWide(logo);
            }

            {
                String logo = nullIfEmpty(em_item.getLogoSmall());
                if (logo != null) {
                    logo = eventmobiConfig.getCompanyImageUrl(em_companies.getEventId(), logo);
                }
                item.setLogoSmall(logo);
            }

            {
                String logo = nullIfEmpty(em_item.getLogoSmallWide());
                if (logo != null) {
                    logo = eventmobiConfig.getCompanyImageUrl(em_companies.getEventId(), logo);
                }
                item.setLogoSmallWide(logo);
            }

            item.setWebsite(nullIfEmpty(em_item.getWebsite()));
            item.setFacebook(nullIfEmpty(em_item.getFacebook()));
            item.setTwitter(nullIfEmpty(em_item.getTwitter()));
            item.setLinkedin(nullIfEmpty(em_item.getLinkedin()));
            items.put(item.getId(), item);
        }
        companies.setItems(items);

        final Task<Void> task = fdb.getReference("sections/companies").setValue(companies);
        allWrites.add(task);
        System.out.println("Number of companies: " + items.size());
    }

    private static void copyBaseSection(EmSection<?> em_section, Section<?> section) {
        section.setId(assertNotEmpty(em_section.getId()));
        section.setName(assertNotEmpty(em_section.getName()));
        section.setType(assertNotEmpty(em_section.getType()));
    }

    // Empty strings become null
    private static String nullIfEmpty(String s) {
        return s != null && s.length() > 0 ? s : null;
    }

    private static String assertNotEmpty(String s) {
        if (s == null || s.length() == 0) {
            throw new IllegalArgumentException();
        }
        else {
            return s;
        }
    }

    private List<String> emptyIfNull(List<String> groupIds) {
        return groupIds == null ? Collections.<String>emptyList() : groupIds;
    }

}
