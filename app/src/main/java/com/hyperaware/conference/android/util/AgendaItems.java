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

import android.content.Context;
import android.support.annotation.NonNull;

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.ui.model.DateHeader;
import com.hyperaware.conference.android.ui.model.TimeGroupHeader;
import com.hyperaware.conference.model.AgendaItem;
import com.hyperaware.conference.model.SpeakerItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class AgendaItems {

    public static final Comparator<AgendaItem> START_TIME_COMPARATOR = new Comparator<AgendaItem>() {
        @Override
        public int compare(AgendaItem lhs, AgendaItem rhs) {
            return (int) (lhs.getEpochStartTime() - rhs.getEpochStartTime());
        }
    };

    @NonNull
    public static List<Object> organize(Collection<AgendaItem> items, TimeZone tz) {
        ArrayList<AgendaItem> sorted = new ArrayList<>(items);
        Collections.sort(sorted, START_TIME_COMPARATOR);

        final ArrayList<Object> organized = new ArrayList<>(sorted.size() * 2);
        long current_date = 0;
        long current_start = 0, current_end = 0;
        for (final AgendaItem item : sorted) {
            final Calendar cal = Calendar.getInstance(tz);
            cal.setTimeInMillis(TimeUnit.SECONDS.toMillis(item.getEpochStartTime()));
            Times.setTime(cal, 0, 0, 0, 0);
            long item_date = cal.getTimeInMillis();
            if (current_date == 0 || item_date != current_date) {
                current_date = item_date;
                DateHeader header = new DateHeader();
                header.date = current_date;
                organized.add(header);
            }

            long item_start = TimeUnit.SECONDS.toMillis(item.getEpochStartTime());
            long item_end = TimeUnit.SECONDS.toMillis(item.getEpochEndTime());
            boolean range_changed =
                current_start == 0 || item_start != current_start || item_end != current_end;
            if (range_changed) {
                current_start = item_start;
                current_end = item_end;
                TimeGroupHeader header = new TimeGroupHeader();
                header.start = item_start;
                header.end = item_end;
                organized.add(header);
            }
            organized.add(item);
        }

        return organized;
    }

    /**
     * Sorts first by start time descending, then by duration ascending.
     */
    private static final Comparator<DateRange> HAPPENING_NOW_COMPARATOR = new Comparator<DateRange>() {
        @Override
        public int compare(DateRange lhs, DateRange rhs) {
            long comp = rhs.start - lhs.start;
            if (comp == 0) {
                comp = (lhs.end - lhs.start) - (rhs.end - rhs.start);
            }
            return (int) comp;
        }
    };

    @NonNull
    public static SortedMap<DateRange, List<AgendaItem>> happeningNow(Collection<AgendaItem> items, long now) {
        final TreeMap<DateRange, List<AgendaItem>> map = new TreeMap<>(HAPPENING_NOW_COMPARATOR);

        for (final AgendaItem item : items) {
            final long start = TimeUnit.SECONDS.toMillis(item.getEpochStartTime());
            final long end = TimeUnit.SECONDS.toMillis(item.getEpochEndTime());
            if (now >= start && now < end) {
                addItemToGroupMap(item, map);
            }
        }

        return map;
    }


    /**
     * Sorts first by start time ascending, then by duration ascending.
     */
    private static final Comparator<DateRange> UP_NEXT_COMPARATOR = new Comparator<DateRange>() {
        @Override
        public int compare(DateRange lhs, DateRange rhs) {
            long comp = lhs.start - rhs.start;
            if (comp == 0) {
                comp = (lhs.end - lhs.start) - (rhs.end - rhs.start);
            }
            return (int) comp;
        }
    };

    @NonNull
    public static SortedMap<DateRange, List<AgendaItem>> upNext(
        Collection<AgendaItem> items, long now, long look_ahead, long look_beyond) {
        final TreeMap<DateRange, List<AgendaItem>> map = new TreeMap<>(UP_NEXT_COMPARATOR);

        for (final AgendaItem item : items) {
            final long start = TimeUnit.SECONDS.toMillis(item.getEpochStartTime());
            if (now < start && start < now + look_ahead) {
                addItemToGroupMap(item, map);
            }
        }

        DateRange first = null;
        final Iterator<Map.Entry<DateRange, List<AgendaItem>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            DateRange dr = it.next().getKey();
            if (first == null) {
                first = dr;
            }
            if (dr.start > first.start + look_beyond) {
                it.remove();
            }
        }

        return map;
    }

    private static void addItemToGroupMap(AgendaItem item, TreeMap<DateRange, List<AgendaItem>> map) {
        final long start = TimeUnit.SECONDS.toMillis(item.getEpochStartTime());
        final long end = TimeUnit.SECONDS.toMillis(item.getEpochEndTime());

        final DateRange range = new DateRange();
        range.start = start;
        range.end = end;

        List<AgendaItem> list;
        if (map.containsKey(range)) {
            list = map.get(range);
        }
        else {
            list = new ArrayList<>();
            map.put(range, list);
        }

        list.add(item);
    }

    public static Map<String, List<AgendaItem>> groupBySpeaker(Collection<AgendaItem> agendaItems) {
        final HashMap<String, List<AgendaItem>> speakersItemsMap = new HashMap<>();
        for (final AgendaItem item : agendaItems) {
            for (String speaker_id : item.getSpeakerIds()) {
                List<AgendaItem> speakerAgendaItems = speakersItemsMap.get(speaker_id);
                if (speakerAgendaItems == null) {
                    speakerAgendaItems = new ArrayList<>();
                    speakersItemsMap.put(speaker_id, speakerAgendaItems);
                }
                speakerAgendaItems.add(item);
            }
        }
        return speakersItemsMap;
    }

    public static String formatLocationAndSpeaker(
        Context context, AgendaItem item, Map<String, SpeakerItem> speakerItemsById) {
        final String location = item.getLocation();

        final List<String> speaker_ids = item.getSpeakerIds();
        int speaker_count = speaker_ids.size();
        final String speakers;
        switch (speaker_count) {
            case 0:
                speakers = null;
                break;
            case 1:
                final SpeakerItem si = speakerItemsById.get(speaker_ids.get(0));
                if (si != null) {
                    speakers = si.getName();
                }
                else {
                    speakers = null;
                }
                break;
            default:
                speakers = context.getResources()
                    .getQuantityString(R.plurals.speaker_count, speaker_count, speaker_count);
                break;
        }

        final String loc_and_speaker;
        if (speakers == null) {
            if (location == null) {
                loc_and_speaker = "";
            }
            else {
                loc_and_speaker = location;
            }
        }
        else if (location == null) {
            loc_and_speaker = speakers;
        }
        else {
            loc_and_speaker = context.getString(R.string.fmt_location_and_speaker, location, speakers);
        }

        return loc_and_speaker;
    }

}
