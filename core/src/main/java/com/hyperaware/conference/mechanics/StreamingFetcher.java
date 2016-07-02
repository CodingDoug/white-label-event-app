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

package com.hyperaware.conference.mechanics;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * A fetcher that simply takes the stream from a Streamer, sends
 * it through a Parser, and returns the result from that.
 *
 * @param <T> the type to fetch
 */

public class StreamingFetcher<T> implements Fetcher<T> {

    private final Streamer streamer;
    private final Parser<T> parser;

    public StreamingFetcher(final Streamer streamer, final Parser<T> parser) {
        this.streamer = streamer;
        this.parser = parser;
    }

    @Override
    public T fetch() throws FetchException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(streamer.stream());
            return parser.parse(is);
        }
        catch (final Exception e) {
            throw new FetchException(e);
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

}
