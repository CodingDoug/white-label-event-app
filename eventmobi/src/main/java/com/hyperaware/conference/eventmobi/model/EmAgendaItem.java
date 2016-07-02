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

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmAgendaItem extends EmItem {

    private String topic;
    private String description;
    private String location;
    @SerializedName("date_t")
    private long rawDate;
    @SerializedName("start_time_t")
    private long epochStartTime;
    @SerializedName("end_time_t")
    private long epochEndTime;
    @SerializedName("group_ids")
    private List<String> groupIds;
    @SerializedName("speaker_ids")
    private List<String> speakerIds;

    public String getTopic() {
        return topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public long getRawDate() {
        return rawDate;
    }

    public void setRawDate(final long rawDate) {
        this.rawDate = rawDate;
    }

    public long getEpochStartTime() {
        return epochStartTime;
    }

    public void setEpochStartTime(final long epochStartTime) {
        this.epochStartTime = epochStartTime;
    }

    public long getEpochEndTime() {
        return epochEndTime;
    }

    public void setEpochEndTime(final long epochEndTime) {
        this.epochEndTime = epochEndTime;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }

    public List<String> getSpeakerIds() {
        return speakerIds;
    }

    public void setSpeakerIds(List<String> speakerIds) {
        this.speakerIds = speakerIds;
    }

}
