/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    public static final String TAG = Utility.class.getSimpleName();

    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static void setPreferredLocation(String newLocation, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.pref_location_key), newLocation);
        editor.apply();
    }

    public static void updateWeather(Activity activity) {
        SunshineSyncAdapter.syncRightNow(activity);
    }

    public static void updateWeather(Context context) {
        SunshineSyncAdapter.syncRightNow(context);
    }

    public static void openLocationInMap(String location, Context context) {

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent geoIntent = new Intent(Intent.ACTION_VIEW);
        geoIntent.setData(geoLocation);
        if(geoIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(geoIntent);
        } else {
            String message = "Unable to show " + location + " on map. No map application installed on device";
            Log.d(TAG, message);
            //Also good idea to show toast for user
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     *
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if(julianDay == currentJulianDay) {
            //just "Today" is enough
            String todayString = context.getString(R.string.today);
            return todayString;
        } else if(julianDay < currentJulianDay + 7) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     *
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if(julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if(julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                     in Utility.DATE_FORMAT
     *
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        windFormat = R.string.format_wind_kmh;

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if(degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if(degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if(degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if(degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if(degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if(degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if(degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if(degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param weatherId from OpenWeatherMap API response
     *
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        //or here: http://openweathermap.org/weather-conditions
        if(weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if(weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if(weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if(weatherId == 511) {
            return R.drawable.ic_snow;
        } else if(weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if(weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if(weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if(weatherId == 762 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if(weatherId == 800) {
            return R.drawable.ic_clear;
        } else if(weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if(weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        } else {
            Log.w(TAG, "Weather Condition with code " + weatherId + " is not found");
            return -1;
        }

    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param weatherId from OpenWeatherMap API response
     *
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if(weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if(weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if(weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if(weatherId == 511) {
            return R.drawable.art_snow;
        } else if(weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if(weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_rain;
        } else if(weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if(weatherId == 762 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if(weatherId == 800) {
            return R.drawable.art_clear;
        } else if(weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if(weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        } else {
            Log.w(TAG, "Weather Condition with code " + weatherId + " is not found");
            return -1;
        }
    }


    /**
     * Provides fancy string for temperature value
     *
     * @param context     for i18n
     * @param temperature temperature to create formatted string with
     *
     * @return formatted temperature string
     */
    public static String formatTemperature(Context context, double temperature) {
        return context.getString(R.string.formatted_temperature, temperature);
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }
}