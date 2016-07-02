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

import java.util.Calendar;

public class Times {

    /**
     * Sets the time parts of the given calendar without changing the date.
     *
     * @param cal the Calendar to modify
     * @param hourOfDay hour of day
     * @param minuteOfHour minute of hour
     * @param secondOfMinute second of minute
     * @param millisOfSecond millisecond of second
     */
    public static void setTime(
        Calendar cal,
        int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond) {
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minuteOfHour);
        cal.set(Calendar.SECOND, secondOfMinute);
        cal.set(Calendar.MILLISECOND, millisOfSecond);
    }

}
