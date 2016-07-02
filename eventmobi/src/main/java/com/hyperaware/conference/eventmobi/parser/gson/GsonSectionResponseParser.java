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

package com.hyperaware.conference.eventmobi.parser.gson;

import com.hyperaware.conference.eventmobi.model.EmItem;
import com.hyperaware.conference.eventmobi.model.EmSectionResponse;

/**
 * Generic parser for all kinds of sections.  Since gson can't handle generics
 * for its input types, we need to provide them explicitly in the generics
 * for this class so that the return types can be appropriately specific.
 *
 * @param <T> the type of section items
 * @param <R> the type of response that is expected from the input stream
 */

public class GsonSectionResponseParser<T extends EmItem, R extends EmSectionResponse<T>> extends GsonParser<R> {

    public GsonSectionResponseParser(Class<R> clazz) {
        super(clazz);
    }

}
