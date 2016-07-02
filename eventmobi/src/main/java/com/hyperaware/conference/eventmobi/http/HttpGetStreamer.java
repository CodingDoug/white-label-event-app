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

package com.hyperaware.conference.eventmobi.http;

import com.hyperaware.conference.eventmobi.EventmobiConfig;
import com.hyperaware.conference.mechanics.Streamer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A Streamer that knows how to make requests of Eventmobi for event data.
 * In uses a standard Java URLConnection and adds an API key to the HTTP
 * request.
 */

public class HttpGetStreamer implements Streamer {

    private final EventmobiConfig config;
    private final String url;

    public HttpGetStreamer(final EventmobiConfig config, final String url) {
        this.config = config;
        this.url = url;
    }

    @Override
    public InputStream stream() throws IOException {
        final URL u = new URL(url);
        final URLConnection conn = u.openConnection();
        conn.addRequestProperty("X-API-KEY", config.getApiKey());
        return conn.getInputStream();
    }

}
