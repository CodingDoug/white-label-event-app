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

import android.app.Application;

import com.hyperaware.conference.android.util.AdjustableClock;
import com.hyperaware.conference.android.util.Clock;

import de.halfbit.tinybus.Bus;

/**
 * This is the interface that each flavor of app needs to extend on their
 * dedicated Dagger component interface.  That interface then needs to have
 * a collection of modules assigned to it that satisfy all the types returned
 * by all the getters here.
 */

public interface AppComponent {

    Application getApplication();

    AdjustableClock getAdjustableClock();
    Clock getClock();

    /** The instance of the event bus used through the app */
    Bus getBus();

    AppExecutors getAppExecutors();

}
