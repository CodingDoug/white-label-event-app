/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.hyperaware.conference.eventmobi.resource;

import com.hyperaware.conference.mechanics.Streamer;

import java.io.IOException;
import java.io.InputStream;

public class JavaResourceStreamer implements Streamer {

    private String resourcePath;

    public JavaResourceStreamer(String resource_path) {
        this.resourcePath = resource_path;
    }

    @Override
    public InputStream stream() throws IOException {
        return getClass().getResourceAsStream(resourcePath);
    }

}
