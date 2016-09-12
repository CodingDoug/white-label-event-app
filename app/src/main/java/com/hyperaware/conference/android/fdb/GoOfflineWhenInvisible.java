package com.hyperaware.conference.android.fdb;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.database.FirebaseDatabase;
import com.hyperaware.conference.android.logging.Logging;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * When no activities are visible in this application, tell Firebase Realtime
 * Database to go offline.  This will help reduce the number of concurrent
 * connections to the database when the app is not being used.  Without this,
 * Firebase will retain a connection to the database when the app is invisible
 * and the process is still alive (Android will retain app processes as an
 * optimization until its resources are needed).  This may also save data
 * usage while the app is not being used if you use keepSynced() or long-lived
 * listeners at database locations (this app does).
 *
 * Here, we go offline when all Activities have been stopped for at least 30
 * seconds, then reconnect when the next Activity is started.
 */

public class GoOfflineWhenInvisible implements Application.ActivityLifecycleCallbacks {

    private static final Logger LOGGER = Logging.getLogger(GoOfflineWhenInvisible.class);
    private static final long OFFLINE_DELAY = TimeUnit.SECONDS.toMillis(30);

    private final FirebaseDatabase fdb;
    private final Handler handler = new Handler();
    private int numActivitiesStarted;

    private Runnable goOffline = new Runnable() {
        @Override
        public void run() {
            LOGGER.fine("Going offline now");
            fdb.goOffline();
        }
    };

    private Runnable goOnline = new Runnable() {
        @Override
        public void run() {
            fdb.goOnline();
        }
    };

    public GoOfflineWhenInvisible(FirebaseDatabase fdb) {
        this.fdb = fdb;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (numActivitiesStarted == 0) {
            LOGGER.fine("Only activity started, going online");
            handler.removeCallbacks(goOffline);
            fdb.goOnline();
        }
        numActivitiesStarted++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        numActivitiesStarted--;
        if (numActivitiesStarted == 0) {
            LOGGER.fine("Last activity stopped, going offline");
            handler.postDelayed(goOffline, OFFLINE_DELAY);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

}
