package com.example.instapets.utilities;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * This class describes the times we receive and translates
 * them into the format of times needed for us.
 * (whether we want the times in minutes/hours that have passed
 * and whether we want a display of the current time and more...)
 */
public class DateFormatter {
    public static DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.ENGLISH);

    public static String getTimeDifference(String dateId) {
        try {
            Date date = format.parse(dateId);
            Date now = new Date();
            assert date != null;
            // calculating the time differences in all the types(milli seconds,seconds,hours and days)
            // between the actual time (now) with the given past time (date)
            long difference_In_Millis = now.getTime() - date.getTime();
            long difference_In_Seconds = difference_In_Millis / 1000;
            long difference_In_Minutes = difference_In_Seconds / 60;
            long difference_In_Hours = difference_In_Minutes / 60;
            long difference_In_Days = difference_In_Hours / 24;

            // creates the string that explains how long ago the its been
            // between the actual time (now) with the given past time (date).
            String timeText = "";
            if (difference_In_Minutes == 0) {
                timeText = difference_In_Seconds + " second";
                if (difference_In_Seconds > 1) timeText += "s";
            } else if (difference_In_Hours == 0) {
                timeText = difference_In_Minutes + " minute";
                if (difference_In_Minutes > 1) timeText += "s";
            } else if (difference_In_Days == 0) {
                timeText = difference_In_Hours + " hour";
                if (difference_In_Hours > 1) timeText += "s";
            } else {
                timeText = difference_In_Days + " day";
                if (difference_In_Days > 1) timeText += "s";
            }
            timeText += " ago";
            return timeText;
        } catch (Exception e) {
            return null;
        }
    }

    //this method returns the current time
    public static String getCurrentTime() {
        return format.format(new Date());
    }

}