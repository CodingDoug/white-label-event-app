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

package com.hyperaware.conference.eventmobi.parser;

import com.hyperaware.conference.eventmobi.model.EmItem;
import com.hyperaware.conference.eventmobi.model.EmSectionResponse;
import com.hyperaware.conference.mechanics.ParseException;

import java.io.InputStream;

public interface SectionResponseParser<T extends EmItem> extends ResponseParser<EmSectionResponse<T>> {

    @Override
    EmSectionResponse<T> parse(InputStream in) throws ParseException;

}
