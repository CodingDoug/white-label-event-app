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

package com.hyperaware.conference.android.dagger;

import android.app.Application;

import com.hyperaware.conference.android.eventmobi.EventmobiApi;
import com.hyperaware.conference.android.eventmobi.EventmobiConfig;
import com.hyperaware.conference.android.eventmobi.fetcher.http.HttpEventmobiConfig;

import dagger.Module;
import dagger.Provides;

/**
 * Default implementation of EventmobiModuleInterface that deals with the
 * web service over HTTP.
 */

@Module
public class HttpEventmobiModule implements EventmobiModuleInterface {

    @Override
    @Provides
    public EventmobiConfig provideEventmobiConfig(Application context) {
        return new HttpEventmobiConfig(context);
    }

    @Override
    @Provides
    public EventmobiApi provideEventmobiApi(EventmobiConfig config) {
        return new HttpEventmobiApi(config);
    }

}
