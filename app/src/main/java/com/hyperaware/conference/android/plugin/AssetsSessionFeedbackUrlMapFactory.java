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

package com.hyperaware.conference.android.plugin;

import android.app.Application;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetsSessionFeedbackUrlMapFactory implements CheckedFactory<SessionFeedbackUrlMap, Exception> {

    private final Application context;
    private final String asset;

    public AssetsSessionFeedbackUrlMapFactory(Application context, String asset) {
        this.context = context;
        this.asset = asset;
    }

    @Override
    public SessionFeedbackUrlMap newInstance() throws Exception {
        InputStream is = null;
        final SessionFeedbackUrlMap map;
        try {
            is = context.getAssets().open(asset);
            final Gson gson = new Gson();
            map = gson.fromJson(new InputStreamReader(is), SessionFeedbackUrlMap.class);
        }
        finally {
            IOUtils.closeQuietly(is);
        }
        return map;
    }

}
