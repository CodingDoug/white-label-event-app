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

package com.hyperaware.conference.android.util;

import de.halfbit.tinybus.Bus;

/**
 * A Clock whose time can be adjusted.  When the time is adjusted an event bus
 * will be notified.  Tmarches on from that new time at the same rate as the
 * master Clock given in the constructor.
 */

public class AdjustableClock implements Clock {

    private final Clock master;
    private final Bus bus;

    private long base;
    private long adjusted;

    public AdjustableClock(final Clock master, final Bus bus) {
        this.master = master;
        this.bus = bus;
    }

    @Override
    public long getCurrentTimeMillis() {
        long now = master.getCurrentTimeMillis();
        return base > 0 ? now - base + adjusted : now;
    }

    public void setCurrentTimeMillis(long millis) {
        base = master.getCurrentTimeMillis();
        adjusted = millis;
        bus.post(this);
    }

    public void reset() {
        adjusted = 0;
        base = 0;
    }

}
