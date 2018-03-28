package com.acua.app.utils;

import android.content.Context;

import com.acua.app.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ghost on 2/9/2017.
 */

public class TimeUtil {

    public final static long ONE_MILLISECOND = 1;
    public final static long MILLISECONDS_IN_A_SECOND = 1000;

    public final static long ONE_SECOND = 1000;
    public final static long SECONDS_IN_A_MINUTE = 60;

    public final static long ONE_MINUTE = ONE_SECOND * 60;
    public final static long MINUTES_IN_AN_HOUR = 60;

    public final static long ONE_HOUR = ONE_MINUTE * 60;
    public final static long HOURS_IN_A_DAY = 24;
    public final static long ONE_DAY = ONE_HOUR * 24;
    public final static long DAYS_IN_A_YEAR = 365;

    public static String formatHMSM(Number n) {

        String res = "";
        if (n != null) {
            long duration = n.longValue();

            duration /= ONE_MILLISECOND;
            int milliseconds = (int) (duration % MILLISECONDS_IN_A_SECOND);
            duration /= ONE_SECOND;
            int seconds = (int) (duration % SECONDS_IN_A_MINUTE);
            duration /= SECONDS_IN_A_MINUTE;
            int minutes = (int) (duration % MINUTES_IN_AN_HOUR);
            duration /= MINUTES_IN_AN_HOUR;
            int hours = (int) (duration % HOURS_IN_A_DAY);
            duration /= HOURS_IN_A_DAY;
            int days = (int) (duration % DAYS_IN_A_YEAR);
            duration /= DAYS_IN_A_YEAR;
            int years = (int) (duration);
            if (days == 0) {
                res = String.format("%02d:%02d:%02d", hours, minutes, seconds);//String.format("%02dh%02dm%02ds", hours, minutes, seconds);
            } else if (years == 0) {
                res = String.format("%d days %02d:%02d:%02d", days, hours, minutes, seconds);//String.format("%ddays %02dh%02dm%02ds", days, hours, minutes, seconds);
            } else {
                res = String.format("%d yrs %d days %02d:%02d:%02d", years, days, hours, minutes, seconds);//String.format("%dyrs %ddays %02dh%02dm%02ds", years, days, hours, minutes, seconds);
            }
        }
        return res;

    }

    public static String getComplexTimeString(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(calendar.getTime());
        SimpleDateFormat week_day = new SimpleDateFormat("EEEE");
        String week_name = week_day.format(calendar.getTime());
        return week_name + " " + day + " " + month_name + " " + year;
    }

    public static String getComplexTimeString(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(calendar.getTime());
        SimpleDateFormat week_day = new SimpleDateFormat("EEEE");
        String week_name = week_day.format(calendar.getTime());
        return week_name + " " + day + " " + month_name + " " + year;
    }

    public static String getFullTimeString(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(calendar.getTime());
        return String.format("%02d", hour) + ":" + String.format("%02d", minute) + " on " + day + " " + month_name + " " + year;
    }

    public static String getSimpleTimeString(long millis) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(millis);
        return getSimpleTimeString(date.getTime());
    }

    public static String getSimpleTimeString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return format.format(date);
    }

    public static String getSimpleDateString(long millis) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(millis);
        return getSimpleDateString(date.getTime());
    }

    public static String getSimpleDateString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault());
        return format.format(date);
    }

    public static boolean checkAvailableTimeRange(long millis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (6 <= hour && hour < 18) {
            return true;
        }else{
            return false;
        }
    }

    public static String getDateString(long millis) {

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(millis);
        return getDateString(date.getTime());

    }

    public static String getDateString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return format.format(date);
    }

    public static String getUserTime(long datetime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datetime);
        return getUserTime(calendar);
    }

    public static String getUserTime(Calendar calendar) {
        return getUserTime(calendar.getTime());
    }

    public static String getUserTime(Date time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        return simpleDateFormat.format(time);
    }

    public static String getUserFriendlyDate(Context context, long millis) {

        millis = getMillisForDateOnly(millis);
        long currentMillis = getMillisForDateOnly(System.currentTimeMillis());

        long diff = currentMillis - millis;
        int days = (int) Math.floor(diff / (1000 * 60 * 60 * 24));
        if (days == 1)
            return context.getResources().getString(R.string.yesterday);
        else {
            return TimeUtil.getSimpleDateString(millis);
        }
    }


    private static long getMillisForDateOnly(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

}
