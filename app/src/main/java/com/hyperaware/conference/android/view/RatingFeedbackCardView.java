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
import android.renderscript.RSInvalidStateException;
import android.util.AttributeSet;
import android.widget.RatingBar;

import com.hyperaware.conference.android.R;

public class RatingFeedbackCardView extends FeedbackCardView {

    private RatingBar ratingBar;

    public RatingFeedbackCardView(Context context) {
        super(context);
    }

    public RatingFeedbackCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RatingFeedbackCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ratingBar = (RatingBar) findViewById(R.id.rating);
        if (ratingBar == null) {
            throw new RSInvalidStateException("RatingBar not found");
        }
    }

    public void setRating(float rating) {
        ratingBar.setRating(rating);
    }

    public float getRating() {
        return ratingBar.getRating();
    }

}
