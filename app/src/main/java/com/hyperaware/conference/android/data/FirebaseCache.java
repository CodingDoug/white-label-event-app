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

package com.hyperaware.conference.android.data;

import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyperaware.conference.android.Singletons;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.model.AgendaSection;
import com.hyperaware.conference.model.Event;
import com.hyperaware.conference.model.SpeakersSection;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.halfbit.tinybus.Bus;
import de.halfbit.tinybus.Produce;

@MainThread
public class FirebaseCache {

    private static final Logger LOGGER = Logging.getLogger(FirebaseCache.class);

    private static FirebaseCache cache;

    private final FirebaseDatabase fdb = FirebaseDatabase.getInstance();
    private final Bus bus = Singletons.deps.getBus();
    private final Executor executor = Singletons.deps.getAppExecutors().getComputeExecutor();

    private Event event;
    private AgendaSection agenda;
    private SpeakersSection speakers;

    private void init() {
        bus.register(this);
        warmEvent();
        warmAgenda();
        warmSpeakers();
    }

    //
    // Event stuff
    //

    private void warmEvent() {
        final DatabaseReference eventRef = fdb.getReference("/event");
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot data) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.fine("New Event data");
                        event = FirebaseDatabaseHelpers.toEvent(data);
                        bus.post(event);
                    }
                });
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                LOGGER.log(Level.SEVERE, "Error reading event", databaseError.toException());
            }
        });
    }

    @Produce
    @Nullable
    public Event getCachedEvent() {
        return event;
    }

    //
    // Agenda stuff
    //

    private void warmAgenda() {
        final DatabaseReference agendaRef = fdb.getReference("/sections/agenda");
        agendaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot data) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.fine("New Agenda data");
                        agenda = FirebaseDatabaseHelpers.toAgendaSection(data);
                        bus.post(agenda);
                    }
                });
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                LOGGER.log(Level.SEVERE, "Error reading agenda", databaseError.toException());
            }
        });
    }

    @Produce
    @Nullable
    public AgendaSection getCachedAgenda() {
        return agenda;
    }

    //
    // Speakers stuff
    //

    private void warmSpeakers() {
        final DatabaseReference ref = fdb.getReference("/sections/speakers");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot data) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.fine("New Speaker data");
                        speakers = FirebaseDatabaseHelpers.toSpeakersSection(data);
                        bus.post(speakers);
                    }
                });
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                LOGGER.log(Level.SEVERE, "Error reading speakers", databaseError.toException());
            }
        });
    }

    @Produce
    @Nullable
    public SpeakersSection getCachedSpeakers() {
        return speakers;
    }

    //
    // Singleton
    //

    public static FirebaseCache getInstance() {
        synchronized (FirebaseCache.class) {
            if (cache == null) {
                cache = new FirebaseCache();
                cache.init();
            }
            return cache;
        }
    }

}
