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

package com.hyperaware.conference.android.marshal.gson;

import com.hyperaware.conference.eventmobi.model.EmAgendaItem;
import com.hyperaware.conference.eventmobi.model.EmAgendaSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmSection;
import com.hyperaware.conference.eventmobi.model.EmSectionResponse;
import com.hyperaware.conference.eventmobi.parser.gson.GsonSectionResponseParser;

import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestAgendaGsonSectionResponseParser {

    @Test
    public void test() throws Exception {
        GsonSectionResponseParser<EmAgendaItem, EmAgendaSectionResponse> parser =
            new GsonSectionResponseParser<>(EmAgendaSectionResponse.class);
        InputStream is = getClass().getResourceAsStream("/section_agenda.json");
        EmSectionResponse<EmAgendaItem> response = parser.parse(is);
        is.close();

        assertNotNull(response);

        assertEquals("success", response.getStatus());

        EmSection<EmAgendaItem> section = response.getSection();
        assertNotNull(section);
        assertEquals("122369", section.getId());
        assertEquals("9849", section.getEventId());
        assertEquals("Agenda and Sessions", section.getName());
        assertEquals("agenda", section.getType());

        List<EmAgendaItem> items = section.getItems();
        assertNotNull(items);
        assertEquals(2, items.size());

        EmAgendaItem item0 = items.get(0);
        assertNotNull(item0);
        assertEquals("630807", item0.getId());
        assertEquals("Session 630807", item0.getTopic());
        assertEquals("Session 630807 Description", item0.getDescription());
        assertEquals(1448946000, item0.getRawDate());
        assertEquals(1448971200, item0.getEpochStartTime());
        assertEquals(1449014400, item0.getEpochEndTime());
        assertNotNull(item0.getGroupIds());
        assertEquals(0, item0.getGroupIds().size());
        assertNotNull(item0.getSpeakerIds());
        assertEquals(0, item0.getSpeakerIds().size());

        EmAgendaItem item1 = items.get(1);
        assertNotNull(item1);
        assertNotNull(item1.getGroupIds());
        assertEquals(1, item1.getGroupIds().size());
        assertEquals("168609", item1.getGroupIds().get(0));
        assertNotNull(item1.getSpeakerIds());
        assertEquals(1, item1.getSpeakerIds().size());
        assertEquals("2753086", item1.getSpeakerIds().get(0));
    }

}
