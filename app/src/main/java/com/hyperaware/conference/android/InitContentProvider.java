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

package com.hyperaware.conference.android;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.FirebaseDatabase;
import com.hyperaware.conference.android.dagger.AppComponentFactory;
import com.hyperaware.conference.android.data.FirebaseCache;
import com.hyperaware.conference.android.logging.AndroidLogHandler;
import com.hyperaware.conference.android.logging.Logging;
import com.hyperaware.conference.android.util.TwitterConfig;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.fabric.sdk.android.Fabric;

public class InitContentProvider extends ContentProvider {

    private Application context;
    private Logger logger;

    @Override
    public boolean onCreate() {
        final Context c = getContext();
        if (c != null) {
            context = (Application) c.getApplicationContext();
            initLogging();
            initStrictMode();
            initDagger();
            initFirebase();
            initFabricLax();
        }
        return true;
    }

    private void initLogging() {
        final String pkg = "com.hyperaware.conference";
        final AndroidLogHandler alh = new AndroidLogHandler(pkg);
        final Logger l = Logger.getLogger(pkg);
        l.addHandler(alh);
        l.setUseParentHandlers(false);
        l.setLevel(Level.FINEST); // configure default log level here, perhaps via a resource?

        logger = Logging.getLogger(this.getClass());
        logger.info("Logging initialized with default level " + l.getLevel());
    }

    private void initStrictMode() {
        // Complain loudly of strict mode violations when in debug mode.
        if (BuildConfig.DEBUG) {
            final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyFlashScreen()
                .penaltyLog()
                .build();
            StrictMode.setThreadPolicy(policy);
            logger.info("Strict Mode in effect for the main thread");
        }
    }

    private void initDagger() {
        // The class named by app_component_factory must be an instance of
        // AppComponentFactory and return an instance of AppComponent, which
        // would normally be created by a Dagger-generated builder.
        final Resources res = context.getResources();
        final String app_component_factory_name = res.getString(R.string.app_component_factory);
        try {
            Class<?> factory_class = Class.forName(app_component_factory_name);
            final AppComponentFactory factory = (AppComponentFactory) factory_class.newInstance();
            Singletons.deps = factory.newInstance(context);
        }
        catch (Exception e) {
            throw new RuntimeException("Couldn't create app component factory class " + app_component_factory_name, e);
        }
    }

    // Fabric init does several file IO operations (naughty!) so we'll disable strict
    // mode briefly to prevent complaints.
    private void initFabricLax() {
        StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
        try {
            initFabric();
        }
        finally {
            StrictMode.setThreadPolicy(old);
        }
    }

    private void initFabric() {
        // Only initialize Fabric if Twitter integration is fully configured
        TwitterConfig.init(context);
        TwitterConfig config = TwitterConfig.getInstance();
        if (config.isConfigured()) {
            logger.info("Event hashtag is " + config.getEventHashtag() + ".  Initializing Fabric.");
            TwitterAuthConfig auth = new TwitterAuthConfig(config.getApiKey(), config.getApiSecret());
            Fabric.with(context, new Twitter(auth));
        }
        else {
            logger.info("Twitter integration is not configured; Fabric not initialized");
        }
    }

    private void initFirebase() {
        final FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        fdb.setPersistenceEnabled(true);
        FirebaseCache.getInstance();
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

}
