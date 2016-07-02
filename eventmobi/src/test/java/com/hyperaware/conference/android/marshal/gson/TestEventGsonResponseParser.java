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

import com.hyperaware.conference.eventmobi.model.EmEvent;
import com.hyperaware.conference.eventmobi.model.EmEventResponse;
import com.hyperaware.conference.eventmobi.model.EmSection;
import com.hyperaware.conference.eventmobi.parser.gson.GsonParser;

import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestEventGsonResponseParser {

    @Test
    public void test() throws Exception {
        GsonParser<EmEventResponse> parser = new GsonParser<>(EmEventResponse.class);
        InputStream is = getClass().getResourceAsStream("/event.json");
        EmEventResponse response = parser.parse(is);
        is.close();

        assertNotNull(response);

        assertEquals("success", response.getStatus());

        EmEvent event = response.getResponse();
        assertNotNull(event);
        assertEquals("9849", event.getId());
        assertEquals("STUBBED", event.getName());
        assertEquals("Stubbed Event Name", event.getFullName());
        assertEquals("Stubbed Location", event.getLocationName());
        assertEquals("Stubbed Location Address", event.getLocationAddress());
        assertEquals("Stubbed description." , event.getDescription());
        assertEquals("http://www.stubbed.com/", event.getWebsite());
        assertEquals("America/New_York", event.getTimezoneName());

        List<EmSection> sections = event.getSections();
        assertNotNull(sections);
        assertEquals(2, sections.size());

        EmSection section0 = sections.get(0);
        assertEquals("122369", section0.getId());
        assertEquals("Agenda and Sessions", section0.getName());
        assertEquals("agenda", section0.getType());
        assertEquals("http://stubbed.com/122369", section0.getUrl());

        EmSection section1 = sections.get(1);
        assertEquals("122371", section1.getId());
        assertEquals("Speakers", section1.getName());
        assertEquals("speakers", section1.getType());
        assertEquals("http://stubbed.com/122371", section1.getUrl());
    }

}
