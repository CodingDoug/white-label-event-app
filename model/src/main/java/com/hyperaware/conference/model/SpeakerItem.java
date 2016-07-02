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

package com.hyperaware.conference.model;

public class SpeakerItem extends PersonItem {

    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    // TODO These should not have to exist for Firebase pojos

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public String getCompanyName() {
        return super.getCompanyName();
    }

    @Override
    public void setCompanyName(String companyName) {
        super.setCompanyName(companyName);
    }

    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public String getImage50() {
        return super.getImage50();
    }

    @Override
    public void setImage50(String image50) {
        super.setImage50(image50);
    }

    @Override
    public String getImage100() {
        return super.getImage100();
    }

    @Override
    public void setImage100(String image100) {
        super.setImage100(image100);
    }

    @Override
    public String getAbout() {
        return super.getAbout();
    }

    @Override
    public void setAbout(String about) {
        super.setAbout(about);
    }

    @Override
    public String getWebsite() {
        return super.getWebsite();
    }

    @Override
    public void setWebsite(String website) {
        super.setWebsite(website);
    }

    @Override
    public String getFacebook() {
        return super.getFacebook();
    }

    @Override
    public void setFacebook(String facebook) {
        super.setFacebook(facebook);
    }

    @Override
    public String getTwitter() {
        return super.getTwitter();
    }

    @Override
    public void setTwitter(String twitter) {
        super.setTwitter(twitter);
    }

    @Override
    public String getLinkedin() {
        return super.getLinkedin();
    }

    @Override
    public void setLinkedin(String linkedin) {
        super.setLinkedin(linkedin);
    }

}
