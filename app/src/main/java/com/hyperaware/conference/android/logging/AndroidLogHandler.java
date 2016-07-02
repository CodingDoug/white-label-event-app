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

package com.hyperaware.conference.android.logging;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Java logging handler for sending log records to the Android log.
 * See this app's Application object for an example of how to get this
 * installed into your app at launch.
 *
 * This Handler works best when your code's Loggers all live underneath the
 * package name given in the constructor.
 */

public class AndroidLogHandler extends Handler {

    private final String packageRoot;

    public AndroidLogHandler(final String package_root) {
        super();
        packageRoot = package_root;
    }

    @Override
    public void publish(final LogRecord record) {
        final String tag = packageRoot;
        String name = record.getLoggerName();

        // Strip packageRoot from the beginning, if present, to shorten redundant info
        if (name.startsWith(packageRoot)) {
            final int trunc = name.length() > packageRoot.length() ? packageRoot.length() + 1 : name.length();
            name = name.substring(trunc);
        }
        else {
            // Only use trailing (class name) after the dot
            final int lastdot = name.lastIndexOf('.');
            if (lastdot >= 0 && lastdot < name.length()) {
                name = name.substring(lastdot + 1);
            }
        }

        final Throwable throwable = record.getThrown();
        final Level level = record.getLevel();
        final long seq = record.getSequenceNumber();

        final String orig_message = record.getMessage();
        if (orig_message != null) {
            try {
                // Default buffer size is required here to prevent recursive log messages
                final BufferedReader br = new BufferedReader(new StringReader(orig_message), 8192);
                String line;
                while ((line = br.readLine()) != null) {
                    line = name + " " + level + " " + seq + ": " + line;
                    if (level == Level.SEVERE) {
                        Log.e(tag, line);
                    }
                    else if (level == Level.WARNING) {
                        Log.w(tag, line);
                    }
                    else if (level == Level.CONFIG || level == Level.INFO) {
                        Log.i(tag, line);
                    }
                    else if (level == Level.FINE) {
                        Log.v(tag, line);
                    }
                    else {
                        Log.d(tag, line);
                    }
                }
            }
            catch (final IOException ignore) {
            }
        }

        if (throwable != null) {
            final String line = name + " " + level + " " + seq;
            if (level == Level.SEVERE) {
                Log.e(tag, line, throwable);
            }
            else if (level == Level.WARNING) {
                Log.w(tag, line, throwable);
            }
            else if (level == Level.CONFIG || level == Level.INFO) {
                Log.i(tag, line, throwable);
            }
            else if (level == Level.FINE) {
                Log.v(tag, line, throwable);
            }
            else {
                Log.d(tag, line, throwable);
            }
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

}
