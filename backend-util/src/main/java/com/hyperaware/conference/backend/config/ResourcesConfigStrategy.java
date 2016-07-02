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

package com.hyperaware.conference.backend.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.hyperaware.conference.eventmobi.EventmobiConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResourcesConfigStrategy implements ConfigStrategy {

    private static final String RESOURCE_DIR = "/firebase";

    private static final String FIREBASE_PROPERTIES_RESOURCE = RESOURCE_DIR + "/firebase.properties";
    private static final String FIREBASE_DATABASE_URL_PROP = "firebase.database.url";

    private static final String SERVICE_JSON_RESOURCE = RESOURCE_DIR + "/service.json";

    private static final String EVENTMOBI_PROPERTIES_RESOURCE = RESOURCE_DIR + "/eventmobi.properties";
    private static final String EVENTMOBI_API_KEY_PROP = "api_key";
    private static final String EVENTMOBI_EVENT_NAME_PROP = "event_name";

    private String databaseUrl;
    private EventmobiConfig eventmobiConfig;

    @Override
    public void configure() throws ConfigException {
        configureFirebaseProps();
        configureFirebase();
        configureEventmobi();
    }

    private void configureFirebaseProps() throws ConfigException {
        final InputStream is = getClass().getResourceAsStream(FIREBASE_PROPERTIES_RESOURCE);
        if (is == null) {
            throw new ConfigException("Can't find firebase properties " + FIREBASE_PROPERTIES_RESOURCE);
        }

        try {
            final Properties props = new Properties();
            props.load(is);
            is.close();

            Object value = props.get(FIREBASE_DATABASE_URL_PROP);
            if (value != null) {
                databaseUrl = value.toString();
            }
            else {
                throw new ConfigException("Property missing: " + FIREBASE_DATABASE_URL_PROP);
            }
        }
        catch (IOException e) {
            throw new ConfigException(e);
        }
    }

    private void configureFirebase() throws ConfigException {
        final InputStream is = getClass().getResourceAsStream(SERVICE_JSON_RESOURCE);
        if (is == null) {
            throw new ConfigException("Can't find service account resource " + SERVICE_JSON_RESOURCE);
        }

        final FirebaseOptions options = new FirebaseOptions.Builder()
            .setServiceAccount(is)
            .setDatabaseUrl(databaseUrl)
            .build();

        try {
            is.close();
        }
        catch (IOException e) {
            throw new ConfigException(e);
        }

        FirebaseApp.initializeApp(options);
    }

    private void configureEventmobi() throws ConfigException {
        final InputStream is = getClass().getResourceAsStream(EVENTMOBI_PROPERTIES_RESOURCE);
        if (is == null) {
            throw new ConfigException("Can't find Eventmobi properties " + EVENTMOBI_PROPERTIES_RESOURCE);
        }

        try {
            final Properties props = new Properties();
            props.load(is);
            is.close();

            Object value;
            String api_key, event_name;

            value = props.get(EVENTMOBI_API_KEY_PROP);
            if (value != null) {
                api_key = value.toString();
            }
            else {
                throw new ConfigException("Property missing: " + EVENTMOBI_API_KEY_PROP);
            }

            value = props.get(EVENTMOBI_EVENT_NAME_PROP);
            if (value != null) {
                event_name = value.toString();
            }
            else {
                throw new ConfigException("Property missing: " + EVENTMOBI_EVENT_NAME_PROP);
            }

            eventmobiConfig = new MyEventmobiConfig(api_key, event_name);
        }
        catch (IOException e) {
            throw new ConfigException(e);
        }
    }

    @Override
    public FirebaseDatabase getFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    @Override
    public EventmobiConfig getEventmobiConfig() {
        return eventmobiConfig;
    }

}
