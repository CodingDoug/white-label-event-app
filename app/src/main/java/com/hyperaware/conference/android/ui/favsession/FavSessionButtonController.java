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

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageButton;

import com.hyperaware.conference.android.R;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FavSessionButtonController {

    private static final String SHARED_PREFS_NAME = "favorite_sessions";
    private static final String FAVORITES_PREF_NAME = "favorite_sessions";

    private final SharedPreferences favoriteSessions;

    public FavSessionButtonController(final Context context) {
        favoriteSessions = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void init(final ImageButton button, final String session_id) {
        final Set<String> sessions =
            favoriteSessions.getStringSet(FAVORITES_PREF_NAME, Collections.<String>emptySet());
        int draw_res = sessions.contains(session_id) ?
            R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp;
        button.setImageResource(draw_res);
    }

    public void toggle(final ImageButton button, final String session_id) {
        final Set<String> favs = favoriteSessions.getStringSet(FAVORITES_PREF_NAME, Collections.<String>emptySet());
        final Set<String> updated_favs = new HashSet<>(favs);
        boolean fav = updated_favs.contains(session_id);
        int draw_res;
        if (fav) {
            updated_favs.remove(session_id);
            draw_res = R.drawable.ic_favorite_border_black_24dp;
        }
        else {
            updated_favs.add(session_id);
            draw_res = R.drawable.ic_favorite_black_24dp;
        }
        favoriteSessions.edit().putStringSet(FAVORITES_PREF_NAME, updated_favs).apply();
        button.setImageResource(draw_res);
    }

}
