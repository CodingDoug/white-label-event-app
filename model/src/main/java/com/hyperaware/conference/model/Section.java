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

import java.util.Map;

/**
 * Common data for all sections, except contained items.
 *
 * @param <T> the type of the individual items contained in this section
 */

public class Section<T extends Item> {

    private String id;
    private String name;
    private String type;
    private Map<String, T> items;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Map<String, T> getItems() {
        return items;
    }

    public void setItems(Map<String, T> items) {
        this.items = items;
    }

    public String toString() {
        return
            "{Section" +
            " id=" + id +
            " name=" + name +
            " type=" + type +
            " items=" + items +
            "}";
    }

}

