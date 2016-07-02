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

package com.hyperaware.conference.android.fdb;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.hyperaware.conference.android.logging.Logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

@MainThread
public class FirebaseMultiQuery {

    private static final Logger LOGGER = Logging.getLogger(FirebaseMultiQuery.class);

    private final HashSet<DatabaseReference> refs = new HashSet<>();
    private final HashMap<DatabaseReference, DataSnapshot> snaps = new HashMap<>();
    private final HashMap<DatabaseReference, ValueEventListener> listeners = new HashMap<>();

    public FirebaseMultiQuery(final DatabaseReference... refs) {
        for (final DatabaseReference ref : refs) {
            add(ref);
        }
    }

    public void add(final DatabaseReference ref) {
        refs.add(ref);
    }

    public Task<Map<DatabaseReference, DataSnapshot>> start() {
        // Create a Task<DataSnapshot> to trigger in response to each database listener.
        //
        final ArrayList<Task<DataSnapshot>> tasks = new ArrayList<>(refs.size());
        for (final DatabaseReference ref : refs) {
            final TaskCompletionSource<DataSnapshot> source = new TaskCompletionSource<>();
            final ValueEventListener listener = new MyValueEventListener(ref, source);
            ref.addListenerForSingleValueEvent(listener);
            listeners.put(ref, listener);
            tasks.add(source.getTask());
        }

        // Return a single Task that triggers when all queries are complete.  It contains
        // a map of all original DatabaseReferences originally given here to their resulting
        // DataSnapshot.
        //
        return Tasks.whenAll(tasks).continueWith(new Continuation<Void, Map<DatabaseReference, DataSnapshot>>() {
            @Override
            public Map<DatabaseReference, DataSnapshot> then(@NonNull Task<Void> task) throws Exception {
                task.getResult();
                return new HashMap<>(snaps);
            }
        });
    }

    public void stop() {
        for (final Map.Entry<DatabaseReference, ValueEventListener> entry : listeners.entrySet()) {
            entry.getKey().removeEventListener(entry.getValue());
        }
        snaps.clear();
        listeners.clear();
    }

    private class MyValueEventListener implements ValueEventListener {
        private final DatabaseReference ref;
        private final TaskCompletionSource<DataSnapshot> taskSource;

        public MyValueEventListener(DatabaseReference ref, TaskCompletionSource<DataSnapshot> taskSource) {
            this.ref = ref;
            this.taskSource = taskSource;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            snaps.put(ref, dataSnapshot);
            taskSource.setResult(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            taskSource.setException(databaseError.toException());
        }
    }

}
