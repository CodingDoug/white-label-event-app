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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hyperaware.conference.mechanics.ParseException;
import com.hyperaware.conference.mechanics.Parser;

import java.io.InputStream;
import java.io.InputStreamReader;

public class GsonParser<T> implements Parser<T> {

    private final Class<T> clazz;

    public GsonParser(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T parse(InputStream is) throws ParseException {
        try {
            final GsonBuilder gb = new GsonBuilder();
            // Eventmobi serializes booleans as integer strings!  Yuck!
            final BooleanDeserializer bs = new BooleanDeserializer();
            gb.registerTypeAdapter(boolean.class, bs);
            gb.registerTypeAdapter(Boolean.class, bs);
            final Gson gson = gb.create();
            final T response = gson.fromJson(
                new InputStreamReader(is, GsonParserConstants.CHARSET),
                clazz
            );
            if (response != null) {
                return response;
            }
            else {
                throw new ParseException("Reader at EOF");
            }
        }
        catch (Exception e) {
            throw new ParseException(e);
        }
    }

}
