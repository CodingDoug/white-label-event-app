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

package com.hyperaware.conference.android.dagger;

import android.os.Process;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provides executors for general use throughout the app.
 *
 * The compute executor here is implemented by a fixed size executor
 * where the threads are all set to background priority by
 * Process.setPriority(). The size of the pool is the number of processors
 * on the device minus one (minimum 1).
 */

@Module
public class AppExecutorsModule {

    private static final int COMPUTE_THREAD_PRIORITY = Process.THREAD_PRIORITY_BACKGROUND;

    @Provides
    @Singleton
    public AppExecutors provideAppExecutors() {
        final int fixed = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        final Executor executor = Executors.newFixedThreadPool(fixed, new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull final Runnable runnable) {
                return new PrioritizedThread(COMPUTE_THREAD_PRIORITY, runnable);
            }
        });
        return new AppExecutors(executor);
    }

    private static class PrioritizedThread extends Thread {
        private final int prio;
        private final Runnable runnable;

        public PrioritizedThread(int prio, Runnable runnable) {
            this.prio = prio;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            Process.setThreadPriority(prio);
            super.run();
            runnable.run();
        }
    }

}
