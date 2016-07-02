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

package com.hyperaware.conference.android.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * A ViewGroup that shows only one of its immediate children at a time,
 * adjusting their visibility between GONE and VISIBLE as needed.
 * Currently can only be used in an XML layout.  Should support dynamic
 * adding and removing of views in the future.
 */

public class MutexViewGroup extends FrameLayout {

    private final HashMap<Integer, View> children = new HashMap<>();

    public MutexViewGroup(Context context) {
        super(context);
    }

    public MutexViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MutexViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        indexChildren();
    }

    private void indexChildren() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int id = child.getId();
            if (id != NO_ID) {
                children.put(id, child);
            }
        }
    }

    public void showViewId(@IdRes int id) {
        for (Map.Entry<Integer, View> entry : children.entrySet()) {
            final Integer i = entry.getKey();
            final View view = entry.getValue();
            view.setVisibility(i == id ? VISIBLE : GONE);
        }
    }

    public void showView(View view) {
        for (View v : children.values()) {
            v.setVisibility(v == view ? VISIBLE : GONE);
        }
    }

}
