package com.tninteractive.socketcontroller.util;

import java.util.Date;

public class TimeUtil {

    public static long[] getElapsedTimeValues(Date start, Date end){
        long diff = end.getTime() - start.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = diff / daysInMilli;
        diff = diff % daysInMilli;

        long elapsedHours = diff / hoursInMilli;
        diff = diff % hoursInMilli;

        long elapsedMinutes = (int)Math.ceil((double)diff / (double)minutesInMilli);

        return new long[]{elapsedDays, elapsedHours, elapsedMinutes};
    }
}
