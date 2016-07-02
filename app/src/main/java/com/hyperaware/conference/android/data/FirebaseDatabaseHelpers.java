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

package com.hyperaware.conference.android.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.hyperaware.conference.model.AgendaItem;
import com.hyperaware.conference.model.AgendaSection;
import com.hyperaware.conference.model.AttendeeItem;
import com.hyperaware.conference.model.AttendeesSection;
import com.hyperaware.conference.model.CompaniesSection;
import com.hyperaware.conference.model.CompanyItem;
import com.hyperaware.conference.model.Event;
import com.hyperaware.conference.model.FeedbackQuestion;
import com.hyperaware.conference.model.FeedbackQuestion2;
import com.hyperaware.conference.model.Item;
import com.hyperaware.conference.model.MapItem;
import com.hyperaware.conference.model.MapsSection;
import com.hyperaware.conference.model.QuestionType;
import com.hyperaware.conference.model.Section;
import com.hyperaware.conference.model.SpeakerItem;
import com.hyperaware.conference.model.SpeakersSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FirebaseDatabaseHelpers {

    public static Event toEvent(final DataSnapshot data) {
        return data.getValue(Event.class);
    }

    public static AgendaSection toAgendaSection(final DataSnapshot data) {
        final AgendaSection section = new AgendaSection();
        populateSection(data, section);
        Map<String, AgendaItem> items = data.child("items").getValue(new GenericTypeIndicator<Map<String, AgendaItem>>() {});
        if (items == null) {
            items = Collections.emptyMap();
        }

        // Force empty speaker list when null so the client doesn't have to
        // always check null.
        for (final Map.Entry<String, AgendaItem> entry : items.entrySet()) {
            if (entry.getValue().getSpeakerIds() == null) {
                entry.getValue().setSpeakerIds(Collections.<String>emptyList());
            }
        }

        section.setItems(items);
        return section;
    }

    public static SpeakersSection toSpeakersSection(final DataSnapshot data) {
        final SpeakersSection section = new SpeakersSection();
        populateSection(data, section);
        Map<String, SpeakerItem> items = data.child("items").getValue(new GenericTypeIndicator<Map<String, SpeakerItem>>() {});
        if (items == null) {
            items = Collections.emptyMap();
        }
        section.setItems(items);
        return section;
    }

    public static AttendeesSection toAttendeesSection(final DataSnapshot data) {
        final AttendeesSection section = new AttendeesSection();
        populateSection(data, section);
        Map<String, AttendeeItem> items = data.child("items").getValue(new GenericTypeIndicator<Map<String, AttendeeItem>>() {});
        if (items == null) {
            items = Collections.emptyMap();
        }
        section.setItems(items);
        return section;
    }

    public static CompaniesSection toCompaniesSection(final DataSnapshot data) {
        final CompaniesSection section = new CompaniesSection();
        populateSection(data, section);
        Map<String, CompanyItem> items = data.child("items").getValue(new GenericTypeIndicator<Map<String, CompanyItem>>() {});
        if (items == null) {
            items = Collections.emptyMap();
        }
        section.setItems(items);
        return section;
    }

    public static MapsSection toMapsSection(DataSnapshot data) {
        final MapsSection section = new MapsSection();
        populateSection(data, section);
        Map<String, MapItem> items = data.child("items").getValue(new GenericTypeIndicator<Map<String, MapItem>>() {});
        if (items == null) {
            items = Collections.emptyMap();
        }
        section.setItems(items);
        return section;
    }

    public static CompanyItem toCompanyItem(final DataSnapshot data) {
        return data.getValue(CompanyItem.class);
    }

    public static AttendeeItem toAttendeeItem(final DataSnapshot data) {
        return data.getValue(AttendeeItem.class);
    }

    public static SpeakerItem toSpeakerItem(final DataSnapshot data) {
        return data.getValue(SpeakerItem.class);
    }

    public static AgendaItem toAgendaItem(final DataSnapshot data) {
        return data.getValue(AgendaItem.class);
    }

    public static List<FeedbackQuestion> toFeedbackQuestions(final DataSnapshot data) {
        final List<FeedbackQuestion2> questions = data.getValue(new GenericTypeIndicator<List<FeedbackQuestion2>>() {});
        int size = questions != null ? questions.size() : 0;
        ArrayList<FeedbackQuestion> qs = new ArrayList<>(size);
        if (questions != null) {
            for (final FeedbackQuestion2 q : questions) {
                qs.add(new FeedbackQuestion(QuestionType.valueOf(q.getType()), q.getText()));
            }
        }
        return qs;

    }

    private static <T extends Item> void populateSection(final DataSnapshot data, Section<T> section) {
        section.setId(data.child("id").getValue(String.class));
        section.setName(data.child("name").getValue(String.class));
        section.setType(data.child("type").getValue(String.class));
    }

}
