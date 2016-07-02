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

import com.hyperaware.conference.eventmobi.model.EmSection;
import com.hyperaware.conference.eventmobi.model.EmSectionResponse;
import com.hyperaware.conference.eventmobi.model.EmSpeakerItem;
import com.hyperaware.conference.eventmobi.model.EmSpeakersSectionResponse;
import com.hyperaware.conference.eventmobi.parser.gson.GsonSectionResponseParser;

import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestSpeakersGsonSectionResponseParser {

    @Test
    public void test() throws Exception {
        GsonSectionResponseParser<EmSpeakerItem, EmSpeakersSectionResponse> parser =
            new GsonSectionResponseParser<>(EmSpeakersSectionResponse.class);
        InputStream is = getClass().getResourceAsStream("/section_speakers.json");
        EmSectionResponse<EmSpeakerItem> response = parser.parse(is);
        is.close();

        assertNotNull(response);

        assertEquals("success", response.getStatus());

        EmSection<EmSpeakerItem> section = response.getSection();
        assertNotNull(section);
        assertEquals("122371", section.getId());
        assertEquals("9849", section.getEventId());
        assertEquals("Speakers", section.getName());
        assertEquals("speakers", section.getType());

        List<EmSpeakerItem> items = section.getItems();
        assertNotNull(items);
        assertEquals(2, items.size());

        EmSpeakerItem item0 = items.get(0);
        assertNotNull(item0);
        assertEquals("2753081", item0.getId());
        assertEquals("Me", item0.getName());
        assertEquals("Google", item0.getCompanyName());
        assertEquals("me50.jpg", item0.getImage50());
        assertEquals("me100.jpg", item0.getImage100());
        assertEquals("Something about me", item0.getAbout());
        assertEquals(0, item0.getPosition());

        // Lazy
        assertNotNull(items.get(1));
    }

}
