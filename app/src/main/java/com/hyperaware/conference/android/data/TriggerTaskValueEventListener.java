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

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hyperaware.conference.android.activity.LauncherActivity;

public class TriggerTaskValueEventListener implements ValueEventListener {

    private final TaskCompletionSource<DataSnapshot> source;

    public TriggerTaskValueEventListener(@NonNull final TaskCompletionSource<DataSnapshot> source) {
        this.source = source;
    }

    @Override
    public void onDataChange(DataSnapshot data) {
        source.setResult(data);
    }

    @Override
    public void onCancelled(DatabaseError error) {
        source.setException(error.toException());
    }

}
