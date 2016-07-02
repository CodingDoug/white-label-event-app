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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hyperaware.conference.android.R;

public class TextFeedbackCardView extends FeedbackCardView {

    private EditText et;

    public TextFeedbackCardView(Context context) {
        super(context);
    }

    public TextFeedbackCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextFeedbackCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        et = (EditText) findViewById(R.id.et);
    }

    public void setText(CharSequence s) {
        et.setText(s);
    }

    public CharSequence getText() {
        return et.getText();
    }

}
