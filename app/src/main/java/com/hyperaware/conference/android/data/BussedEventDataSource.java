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

package com.hyperaware.conference.android.data;

import com.hyperaware.conference.android.eventmobi.model.AllEventData;

import javax.inject.Inject;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Produce;

/**
 * An EventDataSource that broadcasts value changes via the event bus.
 */

public class BussedEventDataSource implements EventDataSource {

    private final Bus bus;

    private FetchState fetchState;
    private AllEventData allEventData;

    public BussedEventDataSource(final Bus bus) {
        this.bus = bus;
    }

    @Override
    public void setFetchState(FetchState state) {
        fetchState = state;
        bus.post(fetchState);
    }

    @Override
    @Produce
    public FetchState getFetchState() {
        return fetchState;
    }

    @Override
    public void setAllEventData(AllEventData data) {
        this.allEventData = data;
        bus.post(data);
    }

    @Override
    @Produce
    public AllEventData getAllEventData() {
        return allEventData;
    }

}
