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
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static final String ARG_HOUR_OF_DAY = "hour_of_day";
    private static final String ARG_MINUTE = "minute";

    private int hourOfDay;
    private int minute;

    public static TimePickerDialogFragment instantiate(int hour_of_day, int minute) {
        final Bundle args = new Bundle();
        args.putInt(ARG_HOUR_OF_DAY, hour_of_day);
        args.putInt(ARG_MINUTE, minute);

        final TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        hourOfDay = args.getInt(ARG_HOUR_OF_DAY);
        minute = args.getInt(ARG_MINUTE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(
            getActivity(),
            this,
            hourOfDay,
            minute,
            DateFormat.is24HourFormat(getActivity())
        );
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Activity activity = getActivity();
        if (activity instanceof TimePickerDialog.OnTimeSetListener) {
            ((TimePickerDialog.OnTimeSetListener) activity).onTimeSet(view, hourOfDay, minute);
        }

    }
}
