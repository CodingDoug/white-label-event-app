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

package com.hyperaware.conference.android.ui.favsession;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.logging.Logging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class that manages the functionality of "favorites" buttons.
 *
 */

@MainThread
public class FavSessionButtonManager {

    private static final Logger LOGGER = Logging.getLogger(FavSessionButtonManager.class);

    private final FirebaseDatabase fdb;
    private final FirebaseAuth auth;
    private final AuthRequiredListener authRequiredListener;
    private final MyAuthStateListener authStateListener = new MyAuthStateListener();

    private MyUserFavoritesListener favoritesListener;
    private DatabaseReference userFavoriteSessionsRef;
    private FirebaseUser user;

    private final HashMap<String, HashSet<ImageButton>> sessionButtons = new HashMap<>();
    private final HashSet<String> favorites = new HashSet<>();

    public FavSessionButtonManager(@NonNull final FirebaseDatabase fdb, @NonNull final FirebaseAuth auth, @NonNull final AuthRequiredListener arl) {
        this.fdb = fdb;
        this.auth = auth;
        this.authRequiredListener = arl;
    }

    public void start() {
        auth.addAuthStateListener(authStateListener);
    }

    public void stop() {
        auth.removeAuthStateListener(authStateListener);
        stopListeningFavorites();
    }

    public void attach(@NonNull final ImageButton button, @NonNull final String session_id) {
        button.setOnClickListener(new MyFavSessionButtonOnClickListener(session_id));
        HashSet<ImageButton> buttons = sessionButtons.get(session_id);
        if (buttons == null) {
            buttons = new HashSet<>();
            sessionButtons.put(session_id, buttons);
        }
        buttons.add(button);
        updateButton(button, session_id);
    }

    public void detach(@NonNull final ImageButton button, @NonNull final String session_id) {
        button.setOnClickListener(null);
        final HashSet<ImageButton> buttons = sessionButtons.get(session_id);
        if (buttons != null) {
            buttons.remove(button);
        }
    }

    private void updateButton(final ImageButton button, final String session_id) {
        boolean is_fav = favorites.contains(session_id);

        final int draw_res = is_fav ?
            R.drawable.ic_favorite_black_24dp :
            R.drawable.ic_favorite_border_black_24dp;
        button.setImageResource(draw_res);

        button.setOnClickListener(new MyFavSessionButtonOnClickListener(session_id));
    }

    private void startListeningFavorites() {
        userFavoriteSessionsRef = fdb.getReference("/users/" + user.getUid() + "/favorites/sessions");
        favoritesListener = new MyUserFavoritesListener();
        userFavoriteSessionsRef.addChildEventListener(favoritesListener);
    }

    private void stopListeningFavorites() {
        if (userFavoriteSessionsRef != null) {
            userFavoriteSessionsRef.removeEventListener(favoritesListener);
            userFavoriteSessionsRef = null;
            favoritesListener = null;
            favorites.clear();

            for (final Map.Entry<String, HashSet<ImageButton>> entry : sessionButtons.entrySet()) {
                final String session_id = entry.getKey();
                final HashSet<ImageButton> buttons = entry.getValue();
                for (final ImageButton button : buttons) {
                    updateButton(button, session_id);
                }
            }
        }
    }

    private class MyAuthStateListener implements FirebaseAuth.AuthStateListener {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            user = firebaseAuth.getCurrentUser();
            if (user != null) {
                startListeningFavorites();
            }
            else {
                stopListeningFavorites();
            }
        }
    }


    private class MyFavSessionButtonOnClickListener implements View.OnClickListener {
        private final String sessionId;

        public MyFavSessionButtonOnClickListener(final String session_id) {
            this.sessionId = session_id;
        }

        @Override
        public void onClick(final View view) {
            // Require a currently signed in user in order to toggle favorites
            if (user != null) {
                toggleFavorite();
            }
            else {
                authRequiredListener.onAuthRequired((ImageButton) view, sessionId);
            }
        }

        private void toggleFavorite() {
            boolean is_fav = favorites.contains(sessionId);
            if (is_fav) {
                favorites.remove(sessionId);
                userFavoriteSessionsRef.child(sessionId).removeValue();
            }
            else {
                favorites.add(sessionId);
                userFavoriteSessionsRef.child(sessionId).setValue(true);
            }
        }
    }


    private class MyUserFavoritesListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot data, String previousChildName) {
            final String session_id = data.getKey();
            boolean is_fav = data.getValue(Boolean.class);
            if (is_fav) {
                favorites.add(session_id);
            }
            else {
                favorites.remove(session_id);
            }
            updateButtons(session_id);
        }

        @Override
        public void onChildChanged(DataSnapshot data, String previousChildName) {
            final String session_id = data.getKey();
            boolean is_fav = data.getValue(Boolean.class);
            if (is_fav) {
                favorites.add(session_id);
            }
            else {
                favorites.remove(session_id);
            }
            updateButtons(session_id);
        }

        @Override
        public void onChildRemoved(DataSnapshot data) {
            final String session_id = data.getKey();
            favorites.remove(session_id);
            updateButtons(session_id);
        }

        @Override
        public void onChildMoved(DataSnapshot data, String s) {
            // Don't care
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            LOGGER.log(Level.SEVERE, "onCancelled", databaseError.toException());
        }

        private void updateButtons(String session_id) {
            final HashSet<ImageButton> buttons = sessionButtons.get(session_id);
            if (buttons != null) {
                for (final ImageButton button : buttons) {
                    updateButton(button, session_id);
                }
            }
        }
    }

    public interface AuthRequiredListener {
        void onAuthRequired(ImageButton view, String sessionId);
    }

}
