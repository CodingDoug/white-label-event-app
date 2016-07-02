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

package com.hyperaware.conference.eventmobi.model;

import java.util.List;
import java.util.Map;

public class EmAllEventData {

    //
    // Raw data
    //

    public EmEvent event;
    public EmSection<EmAgendaItem> agendaSection;
    public EmSection<EmSpeakerItem> speakersSection;
    public EmSection<EmMapItem> mapsSection;
    public EmSection<EmCompanyItem> companiesSection;
    public EmSection<EmAttendeeItem> attendeesSection;

    //
    // Cooked data
    //

    public Map<String, EmAgendaItem> agendaItemsById;
    public Map<String, EmSpeakerItem> speakerItemsById;
    public Map<String, EmAttendeeItem> attendeeItemsById;
    public Map<String, EmCompanyItem> companyItemsById;

    public List<EmSpeakerItem> sortedSpeakers;
    public List<EmAttendeeItem> sortedAttendees;

    public Map<String, List<EmAgendaItem>> speakersAgendaItems;

}
