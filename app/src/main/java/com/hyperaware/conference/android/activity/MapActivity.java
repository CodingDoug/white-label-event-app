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

package com.hyperaware.conference.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.logging.Logging;

import java.util.logging.Logger;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MapActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logging.getLogger(MapActivity.class);

    public static final String EXTRA_IMAGE_URL = "url";

    private String mapUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        mapUrl = intent.getStringExtra(EXTRA_IMAGE_URL);

        initViews();
    }

    private void initViews() {
        setContentView(R.layout.fragment_map);
        ImageView iv = (ImageView) findViewById(R.id.iv_map);
        LOGGER.info("Loading map from " + mapUrl);
        // TODO use a loading indicator
        Glide.with(this).load(mapUrl).into(new MyTarget(iv));
    }

    private class MyTarget extends GlideDrawableImageViewTarget {
        public MyTarget(ImageView view) {
            super(view);
        }

        @Override
        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
            super.onResourceReady(resource, animation);
            PhotoViewAttacher attacher = new PhotoViewAttacher(getView());
            attacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

}
