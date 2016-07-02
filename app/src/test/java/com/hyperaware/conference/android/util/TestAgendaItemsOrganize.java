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

import com.hyperaware.conference.android.ui.model.DateHeader;
import com.hyperaware.conference.android.ui.model.TimeGroupHeader;
import com.hyperaware.conference.model.AgendaItem;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TestAgendaItemsOrganize {

    private static final DateTimeZone DTZ = DateTimeZone.UTC;
    private static final TimeZone TZ = DTZ.toTimeZone();

    @Test
    public void testOrganizeNothing() {
        final List<AgendaItem> items = Collections.emptyList();
        final List<Object> organized = AgendaItems.organize(items, TZ);

        assertEquals(0, organized.size());
    }

    @Test
    public void testOrganizeOneItem() {
        long date_millis = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long date_seconds = TimeUnit.MILLISECONDS.toSeconds(date_millis);
        long start_time_seconds = date_seconds + TimeUnit.HOURS.toSeconds(7);
        long end_time_seconds = start_time_seconds + TimeUnit.HOURS.toSeconds(1);

        // 7 AM - 8 AM
        AgendaItem item0 = new AgendaItem();
        item0.setEpochStartTime(start_time_seconds);
        item0.setEpochEndTime(end_time_seconds);

        final List<AgendaItem> items = Collections.singletonList(item0);
        final List<Object> organized = AgendaItems.organize(items, DateTimeZone.UTC.toTimeZone());

        assertEquals(3, organized.size());
        assertEquals(date_millis, ((DateHeader) organized.get(0)).date);
        TimeGroupHeader trh = (TimeGroupHeader) organized.get(1);
        assertEquals(TimeUnit.SECONDS.toMillis(start_time_seconds), trh.start);
        assertEquals(TimeUnit.SECONDS.toMillis(end_time_seconds), trh.end);
        assertEquals(item0, organized.get(2));
    }

    @Test
    public void testOrganizeOneDayTwoItems() {
        long date_millis = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long date_seconds = TimeUnit.MILLISECONDS.toSeconds(date_millis);

        // 7 AM - 8 AM
        AgendaItem item0 = new AgendaItem();
        item0.setEpochStartTime(date_seconds + TimeUnit.HOURS.toSeconds(7));
        item0.setEpochEndTime(item0.getEpochStartTime() + TimeUnit.HOURS.toSeconds(1));

        // 8 AM - 9 AM
        AgendaItem item1 = new AgendaItem();
        item1.setEpochStartTime(date_seconds + TimeUnit.HOURS.toSeconds(8));
        item1.setEpochEndTime(item1.getEpochStartTime() + TimeUnit.HOURS.toSeconds(1));

        // Initially unsorted
        final List<AgendaItem> items = Arrays.asList(item1, item0);
        final List<Object> organized = AgendaItems.organize(items, TZ);

        // One date header + two time range headers + 2 agenda items
        assertEquals(5, organized.size());

        assertEquals(date_millis, ((DateHeader) organized.get(0)).date);

        TimeGroupHeader trh1 = (TimeGroupHeader) organized.get(1);
        assertEquals(TimeUnit.SECONDS.toMillis(item0.getEpochStartTime()), trh1.start);
        assertEquals(TimeUnit.SECONDS.toMillis(item0.getEpochEndTime()), trh1.end);

        assertEquals(item0, organized.get(2));

        TimeGroupHeader trh3 = (TimeGroupHeader) organized.get(3);
        assertEquals(TimeUnit.SECONDS.toMillis(item1.getEpochStartTime()), trh3.start);
        assertEquals(TimeUnit.SECONDS.toMillis(item1.getEpochEndTime()), trh3.end);

        assertEquals(item1, organized.get(4));
    }

    @Test
    public void testOrganizeTwoDayTwoItems() {
        long date0_millis = new DateTime(2000, 1, 1, 0, 0, 0, 0, DTZ).getMillis();
        long date0_seconds = TimeUnit.MILLISECONDS.toSeconds(date0_millis);

        // Day 1, 7 AM - 8 AM
        AgendaItem item0 = new AgendaItem();
        item0.setEpochStartTime(date0_seconds + TimeUnit.HOURS.toSeconds(7));
        item0.setEpochEndTime(item0.getEpochStartTime() + TimeUnit.HOURS.toSeconds(1));

        long date1_millis = new DateTime(2000, 1, 2, 0, 0, 0, 0, DTZ).getMillis();
        long date1_seconds = TimeUnit.MILLISECONDS.toSeconds(date1_millis);

        // Day 2, 8 AM - 9 AM
        AgendaItem item1 = new AgendaItem();
        item1.setEpochStartTime(date1_seconds + TimeUnit.HOURS.toSeconds(8));
        item1.setEpochEndTime(item1.getEpochStartTime() + TimeUnit.HOURS.toSeconds(1));

        // Initially unsorted
        final List<AgendaItem> items = Arrays.asList(item1, item0);
        final List<Object> organized = AgendaItems.organize(items, TZ);

        // Two date headers + two time range headers + 2 items
        assertEquals(6, organized.size());

        assertEquals(date0_millis, ((DateHeader) organized.get(0)).date);

        TimeGroupHeader trh1 = (TimeGroupHeader) organized.get(1);
        assertEquals(TimeUnit.SECONDS.toMillis(item0.getEpochStartTime()), trh1.start);
        assertEquals(TimeUnit.SECONDS.toMillis(item0.getEpochEndTime()), trh1.end);

        assertEquals(item0, organized.get(2));

        assertEquals(date1_millis, ((DateHeader) organized.get(3)).date);

        TimeGroupHeader trh3 = (TimeGroupHeader) organized.get(4);
        assertEquals(TimeUnit.SECONDS.toMillis(item1.getEpochStartTime()), trh3.start);
        assertEquals(TimeUnit.SECONDS.toMillis(item1.getEpochEndTime()), trh3.end);

        assertEquals(item1, organized.get(5));
    }

}
