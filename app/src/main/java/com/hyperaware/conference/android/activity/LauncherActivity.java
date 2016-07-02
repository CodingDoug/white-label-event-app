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

package com.hyperaware.conference.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.data.TriggerTaskValueEventListener;
import com.hyperaware.conference.android.fdb.EmptyValueEventListener;
import com.hyperaware.conference.model.AgendaSection;
import com.hyperaware.conference.android.data.FirebaseCache;
import com.hyperaware.conference.model.FeedbackQuestion;
import com.hyperaware.conference.model.QuestionType;
import com.hyperaware.conference.model.SpeakersSection;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.model.AgendaItem;
import com.hyperaware.conference.model.Event;
import com.hyperaware.conference.model.Section;
import com.hyperaware.conference.model.SpeakerItem;

import java.util.ArrayList;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Subscribe;

public class LauncherActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logging.getLogger(LauncherActivity.class);

    private final Bus bus = Singletons.deps.getBus();

    private final FirebaseDatabase fdb = FirebaseDatabase.getInstance();
    private DatabaseReference favoritesRef;

    private TaskCompletionSource<Event> eventSource = new TaskCompletionSource<>();
    private TaskCompletionSource<Section<AgendaItem>> agendaSource = new TaskCompletionSource<>();
    private TaskCompletionSource<Section<SpeakerItem>> speakersSource = new TaskCompletionSource<>();
    private TaskCompletionSource<DataSnapshot> favoritesSource = new TaskCompletionSource<>();

    private TriggerTaskValueEventListener favoritesListener;

    private TaskCompletionSource<Void> minDelaySource = new TaskCompletionSource<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        LOGGER.fine("onCreate");

        // If main content is already in cache, don't show the splash
        //
        final FirebaseCache cache = FirebaseCache.getInstance();
        final Event event = cache.getCachedEvent();
        final AgendaSection agenda = cache.getCachedAgenda();
        final SpeakersSection speakers = cache.getCachedSpeakers();
        if (event != null && agenda != null && speakers != null) {
            continueToMain();
            return;
        }

        final ArrayList<Task<?>> allTasks = new ArrayList<>();
        allTasks.add(eventSource.getTask());
        allTasks.add(agendaSource.getTask());
        allTasks.add(speakersSource.getTask());

        // Also get the user's settings if currently logged in
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            favoritesRef = fdb.getReference("/users/" + user.getUid() + "/favorites");
            favoritesListener = new TriggerTaskValueEventListener(favoritesSource);
            favoritesRef.addListenerForSingleValueEvent(favoritesListener);
            allTasks.add(favoritesSource.getTask());
        }

        // Show the splash for at least one second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                minDelaySource.setResult(null);
            }
        }, 1000);
        allTasks.add(minDelaySource.getTask());

        Tasks.whenAll(allTasks).continueWith(new ContinueLaunch());

        bus.register(this);
    }

    @Override
    protected void onDestroy() {
        if (bus.hasRegistered(this)) {
            bus.unregister(this);
        }

        if (favoritesRef != null) {
            favoritesRef.removeEventListener(favoritesListener);
        }

        super.onDestroy();
    }

    @Subscribe
    public void onEvent(Event event) {
        if (! eventSource.getTask().isComplete()) {
            eventSource.setResult(event);
        }
    }

    @Subscribe
    public void onAgenda(AgendaSection agenda) {
        if (! agendaSource.getTask().isComplete()) {
            agendaSource.setResult(agenda);
        }
    }

    @Subscribe
    public void onSpeakers(SpeakersSection speakers) {
        if (! speakersSource.getTask().isComplete()) {
            speakersSource.setResult(speakers);
        }
    }

    private class ContinueLaunch implements Continuation<Void, Void> {
        @Override
        public Void then(@NonNull Task<Void> task) throws Exception {
            continueToMain();
            return null;
        }
    }

    private void continueToMain() {
        // Cache other conference content for later use
        fdb.getReference("/sections").addListenerForSingleValueEvent(EmptyValueEventListener.INSTANCE);
        fdb.getReference("/feedback/session_questions").addListenerForSingleValueEvent(EmptyValueEventListener.INSTANCE);

        startActivity(new Intent(LauncherActivity.this, MainActivity.class));
        finish();
    }

}
