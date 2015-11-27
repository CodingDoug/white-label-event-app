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

package com.hyperaware.conference.android.ui.favsession;

import android.view.View;
import android.widget.ImageButton;

public class FavSessionButtonOnClickListener implements View.OnClickListener {

    private final FavSessionButtonController favController;
    private final ImageButton button;
    private final String sessionId;

    public FavSessionButtonOnClickListener(
        FavSessionButtonController favController, ImageButton button, String session_id) {
        this.favController = favController;
        this.button = button;
        this.sessionId = session_id;
    }

    @Override
    public void onClick(View v) {
        favController.toggle(button, sessionId);
    }

}
