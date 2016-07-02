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

/**
 * The only reason why this class exists is because enum serialization in
 * Firebase is currently broken as of 9.2.0.  Compare this to
 * FeedbackQuestion and see that the type field is an enum.
 */

public class FeedbackQuestion2 {

    private String type;
    private String text;

    public FeedbackQuestion2(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public FeedbackQuestion2() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return "{FeedbackQuestion2" +
            " type=" + type +
            " text=" + text +
            "}";
    }

}
