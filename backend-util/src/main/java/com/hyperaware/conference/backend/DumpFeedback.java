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

package com.hyperaware.conference.backend;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyperaware.conference.backend.config.ResourcesConfigStrategy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.CountDownLatch;

/**
 * Dumps a tab-separated file containing all the feedback for all sessions.
 */

public class DumpFeedback {

    private final FirebaseDatabase fdb;

    public static void main(String[] args) throws Exception {
        final ResourcesConfigStrategy configStrategy = new ResourcesConfigStrategy();
        configStrategy.configure();

        final FirebaseDatabase fdb = configStrategy.getFirebaseDatabase();
        System.err.println("Firebase database: " + fdb.getReference());

        final DumpFeedback dump = new DumpFeedback(fdb);
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
        dump.dumpFeedback(writer);
        writer.flush();

        // Necessary to force Firebase thread to quit
        System.exit(0);
    }

    public DumpFeedback(FirebaseDatabase fdb) {
        this.fdb = fdb;
    }

    public void dumpFeedback(final BufferedWriter writer) throws IOException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final DatabaseReference sessionsRef = fdb.getReference("feedback/sessions");
        sessionsRef.addListenerForSingleValueEvent(new FeedbackValueEventListener(writer, latch));

        latch.await();
    }

    private class FeedbackValueEventListener implements ValueEventListener {
        private final BufferedWriter writer;
        private final CountDownLatch latch;

        public FeedbackValueEventListener(BufferedWriter writer, CountDownLatch latch) {
            this.writer = writer;
            this.latch = latch;
        }

        @Override
        public void onDataChange(final DataSnapshot data) {
            try {
                writeFeedbackTsv(data);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            latch.countDown();
        }

        @Override
        public void onCancelled(final DatabaseError error) {
            //noinspection ThrowableResultOfMethodCallIgnored
            error.toException().printStackTrace();
            latch.countDown();
        }

        private void writeFeedbackTsv(final DataSnapshot data) throws IOException {
            writer.write("session_id\tuser_id\n");
            for (final DataSnapshot session : data.getChildren()) {
                writeSessionRow(session);
            }
        }

        private void writeSessionRow(final DataSnapshot session) throws IOException {
            final String session_id = session.getKey();
            for (final DataSnapshot session_user : session.getChildren()) {
                writer.write(session_id);
                final String user_id = session_user.getKey();
                writer.write('\t');
                writer.write(user_id);
                for (DataSnapshot rating : session_user.getChildren()) {
                    writer.write('\t');
                    final Object value = rating.getValue();
                    if (value != null) {
                        writer.write(value.toString());
                    }
                }
                writer.write('\n');
            }
        }
    }

}
