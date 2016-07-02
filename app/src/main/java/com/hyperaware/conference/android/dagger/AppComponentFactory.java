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

/**
 * This is a factory interface whose implementations should generate singleton
 * instances of AppComponent to be used throughout the app.  Typically this
 * would be created by using a Dagger-generated component builder class.
 * The full class name of the extension interface then needs to be present in
 * the resource named app_component_factory for a flavor's build.
 */

public interface AppComponentFactory {

    AppComponent newInstance(Application context);

}
