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

package com.hyperaware.conference.android.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.hyperaware.conference.android.R;
import com.hyperaware.conference.android.activity.ContentHost;

public class SigninRequiredDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private ContentHost host;

    public static SigninRequiredDialogFragment instantiate() {
        return new SigninRequiredDialogFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        if (activity instanceof ContentHost) {
            host = (ContentHost) activity;
        }
        else {
            throw new IllegalStateException("Host activity must implement ContentHost");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.title_sign_in_required)
            .setMessage(R.string.msg_sign_in_required)
            .setPositiveButton(R.string.btn_sign_in, this)
            .setNegativeButton(R.string.btn_not_now, this)
            .create();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
            host.signIn();
            break;
        case DialogInterface.BUTTON_NEGATIVE:
            dismiss();
            break;
        default:
            break;
        }
    }

}
