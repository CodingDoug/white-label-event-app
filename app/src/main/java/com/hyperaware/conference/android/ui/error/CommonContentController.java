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

package com.hyperaware.conference.android.ui.error;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.data.EventDataSource;
import com.hyperaware.conference.android.service.EventDataFetchService;
import com.hyperaware.conference.android.view.MutexViewGroup;

import de.halfbit.tinybus.Subscribe;

public class CommonContentController {

    private final MutexViewGroup vgMutex;
    private final ProgressBar pb;
    private final ViewGroup vgDataError;

    public CommonContentController(final Context context, final MutexViewGroup mvg) {
        this.vgMutex = mvg;

        mvg.showViewId(R.id.pb);

        pb = (ProgressBar) mvg.findViewById(R.id.pb);
        vgDataError = (ViewGroup) mvg.findViewById(R.id.vg_data_error);

        vgDataError.findViewById(R.id.btn_reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventDataFetchService.start(context);
            }
        });
    }

    @Subscribe
    public void onFetchState(final EventDataSource.FetchState state) {
        switch (state) {
        case Start:
            vgMutex.showView(pb);
            break;
        case Error:
            vgMutex.showView(vgDataError);
            break;
        }
    }

}
