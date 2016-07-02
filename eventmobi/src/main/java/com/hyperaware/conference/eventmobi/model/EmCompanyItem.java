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

public class EmCompanyItem extends EmItem {

    private String name;
    private String description;
    private int position;
    private String booth;
    private String location;
    @SerializedName("logo_large")
    private String logoLarge;
    @SerializedName("logo_large_wide")
    private String logoLargeWide;
    @SerializedName("logo_small")
    private String logoSmall;
    @SerializedName("logo_small_wide")
    private String logoSmallWide;
    private String website;
    private String facebook;
    private String twitter;
    private String linkedin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getBooth() {
        return booth;
    }

    public void setBooth(String booth) {
        this.booth = booth;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLogoLarge() {
        return logoLarge;
    }

    public void setLogoLarge(String logoLarge) {
        this.logoLarge = logoLarge;
    }

    public String getLogoLargeWide() {
        return logoLargeWide;
    }

    public void setLogoLargeWide(String logoLargeWide) {
        this.logoLargeWide = logoLargeWide;
    }

    public String getLogoSmall() {
        return logoSmall;
    }

    public void setLogoSmall(String logoSmall) {
        this.logoSmall = logoSmall;
    }

    public String getLogoSmallWide() {
        return logoSmallWide;
    }

    public void setLogoSmallWide(String logoSmallWide) {
        this.logoSmallWide = logoSmallWide;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }
}
