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

public class TestAgendaItemsHappeningNow {

    public static DateTimeZone DTZ = DateTimeZone.forID("America/Los_Angeles");

    @Test
    public void testWithNothing() {
        final List<AgendaItem> items = Collections.emptyList();
        final SortedMap<DateRange, List<AgendaItem>> map = AgendaItems.happeningNow(items, 0);
        assertEquals(0, map.size());
    }

    // Test that we match an item at the very beginning of its duration.
    @Test
    public void testAtStartOfOne() {
        // 12 AM - 1 AM
        long start_time_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time_s = TimeUnit.MILLISECONDS.toSeconds(start_time_ms);
        long end_time_ms = start_time_ms + TimeUnit.HOURS.toMillis(1);
        long end_time_s = TimeUnit.MILLISECONDS.toSeconds(end_time_ms);
        AgendaItem item = new AgendaItem();
        item.setEpochStartTime(start_time_s);
        item.setEpochEndTime(end_time_s);

        final List<AgendaItem> items_in = Collections.singletonList(item);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.happeningNow(items_in, start_time_ms);

        assertEquals(1, map.size());
        final DateRange dr = map.firstKey();
        assertNotNull(dr);
        assertEquals(start_time_ms, dr.start);
        assertEquals(end_time_ms, dr.end);
        final List<AgendaItem> items_out = map.get(dr);
        assertEquals(1, items_out.size());
        assertSame(item, items_out.get(0));
    }

    // Test that we match an item just before the very end of its duration.
    @Test
    public void testBeforeEndOfOne() {
        // 12 AM - 1 AM
        long start_time_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time_s = TimeUnit.MILLISECONDS.toSeconds(start_time_ms);
        long end_time_ms = start_time_ms + TimeUnit.HOURS.toMillis(1);
        long end_time_s = TimeUnit.MILLISECONDS.toSeconds(end_time_ms);
        AgendaItem item = new AgendaItem();
        item.setEpochStartTime(start_time_s);
        item.setEpochEndTime(end_time_s);

        final List<AgendaItem> items_in = Collections.singletonList(item);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.happeningNow(items_in, start_time_ms + TimeUnit.HOURS.toMillis(1) - 1);

        assertEquals(1, map.size());
        final DateRange dr = map.firstKey();
        assertNotNull(dr);
        assertEquals(start_time_ms, dr.start);
        assertEquals(end_time_ms, dr.end);
        final List<AgendaItem> items_out = map.get(dr);
        assertEquals(1, items_out.size());
        assertSame(item, items_out.get(0));
    }

    // Test that we don't match any items at the very end of their duration.
    @Test
    public void testAtEndOfOne() {
        // 12 AM - 1 AM
        long start_time_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time_s = TimeUnit.MILLISECONDS.toSeconds(start_time_ms);
        long end_time_ms = start_time_ms + TimeUnit.HOURS.toMillis(1);
        long end_time_s = TimeUnit.MILLISECONDS.toSeconds(end_time_ms);
        AgendaItem item = new AgendaItem();
        item.setEpochStartTime(start_time_s);
        item.setEpochEndTime(end_time_s);

        final List<AgendaItem> items_in = Collections.singletonList(item);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.happeningNow(items_in, start_time_ms + TimeUnit.HOURS.toMillis(1));

        assertEquals(0, map.size());
    }

    // Test that we match two items grouped into the same range.
    @Test
    public void testAtStartOfTwoSameRange() {
        // 12 AM - 1 AM
        long start_time_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time_s = TimeUnit.MILLISECONDS.toSeconds(start_time_ms);
        long end_time_ms = start_time_ms + TimeUnit.HOURS.toMillis(1);
        long end_time_s = TimeUnit.MILLISECONDS.toSeconds(end_time_ms);
        AgendaItem item0 = new AgendaItem();
        item0.setEpochStartTime(start_time_s);
        item0.setEpochEndTime(end_time_s);
        AgendaItem item1 = new AgendaItem();
        item1.setEpochStartTime(start_time_s);
        item1.setEpochEndTime(end_time_s);

        final List<AgendaItem> items_in = Arrays.asList(item0, item1);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.happeningNow(items_in, start_time_ms);

        assertEquals(1, map.size());
        final DateRange dr = map.firstKey();
        assertNotNull(dr);
        assertEquals(start_time_ms, dr.start);
        assertEquals(end_time_ms, dr.end);
        final List<AgendaItem> items_out = map.get(dr);
        assertEquals(2, items_out.size());
        assertSame(item0, items_out.get(0));
        assertSame(item1, items_out.get(1));
    }

    // Test that we sort date ranges by reverse start time
    @Test
    public void testTwoDifferentRanges() {
        // 12 AM - 1 AM
        long start_time0_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time0_s = TimeUnit.MILLISECONDS.toSeconds(start_time0_ms);
        long end_time0_ms = start_time0_ms + TimeUnit.HOURS.toMillis(1);
        long end_time0_s = TimeUnit.MILLISECONDS.toSeconds(end_time0_ms);
        AgendaItem item0 = new AgendaItem();
        item0.setEpochStartTime(start_time0_s);
        item0.setEpochEndTime(end_time0_s);

        // 12:30 AM - 1:30 AM
        long start_time1_ms = new DateTime(2000, 1, 1, 0, 30, 0, 0, DTZ).getMillis();
        long start_time1_s = TimeUnit.MILLISECONDS.toSeconds(start_time1_ms);
        long end_time1_ms = start_time1_ms + TimeUnit.HOURS.toMillis(1);
        long end_time1_s = TimeUnit.MILLISECONDS.toSeconds(end_time1_ms);
        AgendaItem item1 = new AgendaItem();
        item1.setEpochStartTime(start_time1_s);
        item1.setEpochEndTime(end_time1_s);

        // Request items at happening at 12:30
        final List<AgendaItem> items_in = Arrays.asList(item0, item1);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.happeningNow(items_in, start_time1_ms);

        // 2 ranges each with one item
        assertEquals(2, map.size());

        List<AgendaItem> items_out;
        Map.Entry<DateRange, List<AgendaItem>> entry;
        final ArrayList<Map.Entry<DateRange, List<AgendaItem>>> entries = new ArrayList<>(map.entrySet());

        // First range, first item
        entry = entries.get(0);
        final DateRange dr0 = entry.getKey();
        assertNotNull(dr0);
        assertEquals(start_time1_ms, dr0.start);
        assertEquals(end_time1_ms, dr0.end);
        items_out = entry.getValue();
        assertNotNull(items_out);
        assertEquals(1, items_out.size());
        assertSame(item1, items_out.get(0));

        // Second range, first item
        entry = entries.get(1);
        final DateRange dr1 = entry.getKey();
        assertNotNull(dr1);
        assertEquals(start_time0_ms, dr1.start);
        assertEquals(end_time0_ms, dr1.end);
        items_out = entry.getValue();
        assertNotNull(items_out);
        assertEquals(1, items_out.size());
        assertSame(item0, items_out.get(0));
    }

    // Test that we sort date ranges by duration after start time
    @Test
    public void testTwoDifferentRangesSameStart() {
        // 12 AM - 1 AM
        long start_time0_ms = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long start_time0_s = TimeUnit.MILLISECONDS.toSeconds(start_time0_ms);
        long end_time0_ms = start_time0_ms + TimeUnit.HOURS.toMillis(1);
        long end_time0_s = TimeUnit.MILLISECONDS.toSeconds(end_time0_ms);

        AgendaItem item0 = new AgendaItem();
        item0.setEpochStartTime(start_time0_s);
        item0.setEpochEndTime(end_time0_s);

        // 12 AM - 2 AM
        @SuppressWarnings("UnnecessaryLocalVariable")
        long start_time1_ms = start_time0_ms;
        long start_time1_s = TimeUnit.MILLISECONDS.toSeconds(start_time1_ms);
        long end_time1_ms = start_time1_ms + TimeUnit.HOURS.toMillis(2);
        long end_time1_s = TimeUnit.MILLISECONDS.toSeconds(end_time1_ms);

        AgendaItem item1 = new AgendaItem();
        item1.setEpochStartTime(start_time1_s);
        item1.setEpochEndTime(end_time1_s);

        // Request items at happening at 12:30
        final List<AgendaItem> items_in = Arrays.asList(item1, item0);
        final SortedMap<DateRange, List<AgendaItem>> map =
            AgendaItems.happeningNow(items_in, start_time1_ms);

        // 2 ranges each with one item
        assertEquals(2, map.size());

        List<AgendaItem> items_out;
        Map.Entry<DateRange, List<AgendaItem>> entry;
        final ArrayList<Map.Entry<DateRange, List<AgendaItem>>> entries = new ArrayList<>(map.entrySet());

        // First range, first item
        entry = entries.get(0);
        final DateRange dr0 = entry.getKey();
        assertNotNull(dr0);
        assertEquals(start_time0_ms, dr0.start);
        assertEquals(end_time0_ms, dr0.end);
        items_out = entry.getValue();
        assertNotNull(items_out);
        assertEquals(1, items_out.size());
        assertSame(item0, items_out.get(0));

        // Second range, first item
        entry = entries.get(1);
        final DateRange dr1 = entry.getKey();
        assertNotNull(dr1);
        assertEquals(start_time1_ms, dr1.start);
        assertEquals(end_time1_ms, dr1.end);
        items_out = entry.getValue();
        assertNotNull(items_out);
        assertEquals(1, items_out.size());
        assertSame(item1, items_out.get(0));
    }

}
