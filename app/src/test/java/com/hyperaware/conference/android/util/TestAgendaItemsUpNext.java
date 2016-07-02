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

package com.hyperaware.conference.android.util;

import com.hyperaware.conference.model.AgendaItem;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class TestAgendaItemsUpNext {

    public static DateTimeZone DTZ = DateTimeZone.forID("America/Los_Angeles");

    @Test
    public void testWithNothing() {
        final List<AgendaItem> items = Collections.emptyList();
        final SortedMap<DateRange, List<AgendaItem>> map = AgendaItems.upNext(items, 0, 0, 0);
        assertEquals(0, map.size());
    }

    @Test
    public void testNoneInRange() {
        // 12 AM - 1 AM
        long start_time_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time_s = TimeUnit.MILLISECONDS.toSeconds(start_time_ms);
        long end_time_ms = start_time_ms + TimeUnit.HOURS.toMillis(1);
        long end_time_s = TimeUnit.MILLISECONDS.toSeconds(end_time_ms);
        AgendaItem item = new AgendaItem();
        item.setEpochStartTime(start_time_s);
        item.setEpochEndTime(end_time_s);

        long look_ahead = TimeUnit.DAYS.toMillis(1);
        long now = start_time_ms - look_ahead;  // just outside of range
        final List<AgendaItem> items_in = Collections.singletonList(item);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.upNext(items_in, now, look_ahead, 0);

        assertEquals(0, map.size());
    }

    @Test
    public void testOneInRange() {
        // 12 AM - 1 AM
        long start_time_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time_s = TimeUnit.MILLISECONDS.toSeconds(start_time_ms);
        long end_time_ms = start_time_ms + TimeUnit.HOURS.toMillis(1);
        long end_time_s = TimeUnit.MILLISECONDS.toSeconds(end_time_ms);
        AgendaItem item = new AgendaItem();
        item.setEpochStartTime(start_time_s);
        item.setEpochEndTime(end_time_s);

        long look_ahead = TimeUnit.DAYS.toMillis(1);
        long now = start_time_ms - look_ahead + 1;  // Just inside of range
        final List<AgendaItem> items_in = Collections.singletonList(item);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.upNext(items_in, now, TimeUnit.DAYS.toMillis(1), 0);

        assertEquals(1, map.size());
        final DateRange dr = map.firstKey();
        assertNotNull(dr);
        assertEquals(start_time_ms, dr.start);
        assertEquals(end_time_ms, dr.end);
        final List<AgendaItem> items_out = map.get(dr);
        assertEquals(1, items_out.size());
        assertSame(item, items_out.get(0));
    }

    @Test
    public void testOneGroupOfTwoInRange() {
        // 12 AM - 1 AM
        long start_time0_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time0_s = TimeUnit.MILLISECONDS.toSeconds(start_time0_ms);
        long end_time0_ms = start_time0_ms + TimeUnit.HOURS.toMillis(1);
        long end_time0_s = TimeUnit.MILLISECONDS.toSeconds(end_time0_ms);
        AgendaItem item0 = new AgendaItem();
        item0.setEpochStartTime(start_time0_s);
        item0.setEpochEndTime(end_time0_s);

        // 12 AM - 1 AM
        AgendaItem item1 = new AgendaItem();
        item1.setEpochStartTime(start_time0_s);
        item1.setEpochEndTime(end_time0_s);

        long look_ahead = TimeUnit.DAYS.toMillis(1);
        long now = start_time0_ms - look_ahead + 1;  // Just inside of range
        final List<AgendaItem> items_in = Arrays.asList(item0, item1);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.upNext(items_in, now, TimeUnit.DAYS.toMillis(1), 0);

        assertEquals(1, map.size());
        final DateRange dr = map.firstKey();
        assertNotNull(dr);
        assertEquals(start_time0_ms, dr.start);
        assertEquals(end_time0_ms, dr.end);
        final List<AgendaItem> items_out = map.get(dr);
        assertEquals(2, items_out.size());
        assertSame(item0, items_out.get(0));
        assertSame(item1, items_out.get(1));
    }

    @Test
    public void testIgnoreGroupBeyond() {
        // 12 AM - 1 AM
        long start_time0_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time0_s = TimeUnit.MILLISECONDS.toSeconds(start_time0_ms);
        long end_time0_ms = start_time0_ms + TimeUnit.HOURS.toMillis(1);
        long end_time0_s = TimeUnit.MILLISECONDS.toSeconds(end_time0_ms);
        AgendaItem item0 = new AgendaItem();
        item0.setEpochStartTime(start_time0_s);
        item0.setEpochEndTime(end_time0_s);

        // 1 AM - 2 AM
        long start_time1_ms = start_time0_ms + TimeUnit.HOURS.toMillis(1);
        long start_time1_s = TimeUnit.MILLISECONDS.toSeconds(start_time1_ms);
        long end_time1_ms = start_time1_ms + TimeUnit.HOURS.toMillis(1);
        long end_time1_s = TimeUnit.MILLISECONDS.toSeconds(end_time1_ms);
        AgendaItem item1 = new AgendaItem();
        item1.setEpochStartTime(start_time1_s);
        item1.setEpochEndTime(end_time1_s);

        long look_ahead = TimeUnit.DAYS.toMillis(1);
        long now = start_time0_ms - 1;
        long beyond = 0;
        final List<AgendaItem> items_in = Arrays.asList(item0, item1);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.upNext(items_in, now, look_ahead, beyond);

        assertEquals(1, map.size());

        Map.Entry<DateRange, List<AgendaItem>> entry;
        final ArrayList<Map.Entry<DateRange, List<AgendaItem>>> entries = new ArrayList<>(map.entrySet());

        entry = entries.get(0);
        final DateRange dr0 = entry.getKey();
        assertNotNull(dr0);
        assertEquals(start_time0_ms, dr0.start);
        assertEquals(end_time0_ms, dr0.end);
    }

    @Test
    public void testIncludeGroupBeyond() {
        // 12 AM - 1 AM
        long start_time0_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time0_s = TimeUnit.MILLISECONDS.toSeconds(start_time0_ms);
        long end_time0_ms = start_time0_ms + TimeUnit.HOURS.toMillis(1);
        long end_time0_s = TimeUnit.MILLISECONDS.toSeconds(end_time0_ms);
        AgendaItem item0 = new AgendaItem();
        item0.setEpochStartTime(start_time0_s);
        item0.setEpochEndTime(end_time0_s);

        // 1 AM - 2 AM
        long start_time1_ms = start_time0_ms + TimeUnit.HOURS.toMillis(1);
        long start_time1_s = TimeUnit.MILLISECONDS.toSeconds(start_time1_ms);
        long end_time1_ms = start_time1_ms + TimeUnit.HOURS.toMillis(1);
        long end_time1_s = TimeUnit.MILLISECONDS.toSeconds(end_time1_ms);
        AgendaItem item1 = new AgendaItem();
        item1.setEpochStartTime(start_time1_s);
        item1.setEpochEndTime(end_time1_s);

        long look_ahead = TimeUnit.DAYS.toMillis(1);
        long now = start_time0_ms - 1;
        long beyond = TimeUnit.HOURS.toMillis(1); // Look beyond to see second group
        final List<AgendaItem> items_in = Arrays.asList(item0, item1);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.upNext(items_in, now, look_ahead, beyond);

        assertEquals(2, map.size());

        Map.Entry<DateRange, List<AgendaItem>> entry;
        final ArrayList<Map.Entry<DateRange, List<AgendaItem>>> entries = new ArrayList<>(map.entrySet());

        entry = entries.get(0);
        final DateRange dr0 = entry.getKey();
        assertNotNull(dr0);
        assertEquals(start_time0_ms, dr0.start);
        assertEquals(end_time0_ms, dr0.end);

        entry = entries.get(1);
        final DateRange dr1 = entry.getKey();
        assertNotNull(dr1);
        assertEquals(start_time1_ms, dr1.start);
        assertEquals(end_time1_ms, dr1.end);
    }

    // Further tests omitted because of duplication in TestAgendaItemsHappeningNow

}
