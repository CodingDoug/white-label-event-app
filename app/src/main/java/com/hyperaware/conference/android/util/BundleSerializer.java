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

package com.hyperaware.conference.android.util;

import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Utility for serializing and deserializing a Java Serializable object
 * to and from a Bundle.
 *
 * @param <T> type of the Serializable object to work with
 */

public class BundleSerializer<T extends Serializable> {

    public static final String DEFAULT_BUNDLE_PROPERTY = "component.state";

    private final String bundleProperty;

    public BundleSerializer() {
        bundleProperty = DEFAULT_BUNDLE_PROPERTY;
    }

    public BundleSerializer(final String bundle_property) {
        this.bundleProperty = bundle_property;
    }

    @SuppressWarnings("unchecked")
    public T deserialize(Bundle bundle) {
        try {
            byte[] serialized = bundle.getByteArray(bundleProperty);
            ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
            ObjectInputStream ois = new ObjectInputStream(bais);
            final T t = (T) ois.readObject();
            ois.close();
            return t;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void serialize(T t, Bundle bundle) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(t);
            oos.close();
            bundle.putByteArray(bundleProperty, baos.toByteArray());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
