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

package com.hyperaware.conference.android.dagger;

import com.hyperaware.conference.android.util.AdjustableClock;
import com.hyperaware.conference.android.util.AndroidSystemClock;
import com.hyperaware.conference.android.util.Clock;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.halfbit.tinybus.Bus;

@Module
public class AdjustableClockModule {

    @Provides
    @Singleton
    public Clock provideClock(AdjustableClock clock) {
        return clock;
    }

    @Provides
    @Singleton
    public AdjustableClock provideAdjustableClock(Bus bus) {
        return new AdjustableClock(new AndroidSystemClock(), bus);
    }

}
