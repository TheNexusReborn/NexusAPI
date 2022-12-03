package com.thenexusreborn.api.helper;

import com.starmediadev.starlib.TimeUnit;
import com.thenexusreborn.api.util.*;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public final class TimeHelper {
    public static long parseTime(String rawTime) {
        Map.Entry<Long, String> years = extractRawTime(rawTime, TimeUnit.YEARS);
        Map.Entry<Long, String> months = extractRawTime(years.getValue(), TimeUnit.MONTHS);
        Map.Entry<Long, String> weeks = extractRawTime(months.getValue(), TimeUnit.WEEKS);
        Map.Entry<Long, String> days = extractRawTime(weeks.getValue(), TimeUnit.DAYS);
        Map.Entry<Long, String> hours = extractRawTime(days.getValue(), TimeUnit.HOURS);
        Map.Entry<Long, String> minutes = extractRawTime(hours.getValue(), TimeUnit.MINUTES);
        Map.Entry<Long, String> seconds = extractRawTime(minutes.getValue(), TimeUnit.SECONDS);
        return years.getKey() + months.getKey() + weeks.getKey() + days.getKey() + hours.getKey() + minutes.getKey() + seconds.getKey();
    }
    
    private static Map.Entry<Long, String> extractRawTime(String rawTime, TimeUnit unit) {
        rawTime = rawTime.toLowerCase();
        String[] rawArray;
        for (String alias : unit.getAliases()) {
            alias = alias.toLowerCase();
            if (rawTime.contains(alias)) {
                rawArray = rawTime.split(alias);
                String fh = rawArray[0];
                long rawLength;
                try {
                    rawLength = Integer.parseInt(fh);
                } catch (NumberFormatException e) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = fh.length() - 1; i > 0; i--) {
                        char c = fh.charAt(i);
                        if (Character.isDigit(c)) {
                            sb.insert(0, c);
                        } else {
                            break;
                        }
                    }
                    rawLength = Integer.parseInt(sb.toString());
                }
                rawTime = rawTime.replace(rawLength + alias, "");
                return new AbstractMap.SimpleEntry<>(unit.toMilliseconds(rawLength), rawTime);
            }
        }

        return new AbstractMap.SimpleEntry<>(0L, rawTime);
    }
    
    public static Calendar parseCalendarDate(String rawDate) throws IllegalArgumentException {
        short[] dateValues = parseDate(rawDate);
        if (dateValues == null) return null; 
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, dateValues[0]);
        calendar.set(Calendar.DAY_OF_MONTH, dateValues[1]);
        calendar.set(Calendar.YEAR, dateValues[2]);
        calendar.set(Calendar.HOUR, dateValues[3]);
        calendar.set(Calendar.MINUTE, dateValues[4]);
        calendar.set(Calendar.SECOND, dateValues[5]);
        return calendar;
    }
    
    public static short[] parseDate(String rawDate) throws IllegalArgumentException {
        String[] rawDateArray = rawDate.split("/");
        if (!(rawDateArray.length >= 3)) {
            throw new IllegalArgumentException("Invalid day of year arguments");
        }
        short month = parseTimeArgument(rawDateArray[0]), day = parseTimeArgument(rawDateArray[1]), year = parseTimeArgument(rawDateArray[2]);

        if (month == -2 || day == -2 || year == -2) {
            return null;
        }

        short hour, minute, second;

        if (rawDateArray.length == 4) {
            String[] rawTimeArray = rawDateArray[3].split(":");

            hour = parseTimeArgument(rawTimeArray[0]);
            minute = parseTimeArgument(rawTimeArray[1]);
            second = parseTimeArgument(rawTimeArray[2]);

            if (hour == -2 || minute == -2 || second == -2) {
                return null;
            }
        } else {
            hour = -1;
            minute = -1;
            second = -1;
        }

        return new short[]{month, day, year, hour, minute, second};
    }
    
    private static short parseTimeArgument(String arg) {
        try {
            return Short.parseShort(arg);
        } catch (NumberFormatException e) {
            return -2;
        }
    }
    
    public static String formatDate(long time) {
        if (time == 0) {
            return "0";
        }
        return new SimpleDateFormat(Constants.DATE_FORMAT).format(new Date(time));
    }
    
    public static String formatTime(long time) {
        Duration remainingTime = Duration.ofMillis(time);
        long days = remainingTime.toDays();
        remainingTime = remainingTime.minusDays(days);
        long hours = remainingTime.toHours();
        remainingTime = remainingTime.minusHours(hours);
        long minutes = remainingTime.toMinutes();
        remainingTime = remainingTime.minusMinutes(minutes);
        long seconds = remainingTime.getSeconds();

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d");
        }
        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m");
        }
        if (seconds > 0) {
            sb.append(seconds).append("s");
        }

        if (sb.toString().isEmpty()) {
            sb.append("0s");
        }
        //String st = sb.toString();
//        if (st != null || !st.isEmpty()) {
//            sb.append("0s");
//        }
        return sb.toString();
    }
    
    public static String niceTime(int seconds) {
        int hours = seconds / 3600;
        seconds -= hours * 3600;
        int minutes = seconds / 60;
        seconds -= minutes * 60;
        return niceTime(hours, minutes, seconds);
    }
    
    public static String niceTime(int seconds, boolean showEmptyHours) {
        int hours = seconds / 3600;
        seconds -= hours * 3600;
        int minutes = seconds / 60;
        seconds -= minutes * 60;
        return niceTime(hours, minutes, seconds, showEmptyHours);
    }
    
    public static String niceTime(int hours, int minutes, int seconds) {
        return niceTime(hours, minutes, seconds, true);
    }
    
    public static String niceTime(int hours, int minutes, int seconds, boolean showEmptyHours) {
        StringBuilder builder = new StringBuilder();

        // Skip hours
        if (hours > 0) {
            if (hours < 10) {
                builder.append('0');
            }
            builder.append(hours);
            builder.append(':');
        } else if (showEmptyHours) {
            builder.append("00:");
        }

        if (minutes < 10 && hours != -1) {
            builder.append('0');
        }
        builder.append(minutes);
        builder.append(':');

        if (seconds < 10) {
            builder.append('0');
        }
        builder.append(seconds);

        return builder.toString();
    }
}
