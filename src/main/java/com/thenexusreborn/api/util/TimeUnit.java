package com.thenexusreborn.api.util;

public enum TimeUnit {
    TICKS("ticks", "tick", "t") {
        public long convertTime(long milliseconds) {
            return milliseconds / 50;
        }
        public long toMilliseconds(long time) {
            return time * 50;
        }
    },
    
    SECONDS("seconds", "second", "s") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        }
        public long toMilliseconds(long time) {
            return java.util.concurrent.TimeUnit.SECONDS.toMillis(time);
        }
    },

    MINUTES("minutes", "minute", "min", "m") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        }
        public long toMilliseconds(long time) {
            return java.util.concurrent.TimeUnit.MINUTES.toMillis(time);
        }
    },

    HOURS("hours", "hour", "h") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toHours(milliseconds);
        }
        public long toMilliseconds(long time) {
            return java.util.concurrent.TimeUnit.HOURS.toMillis(time);
        }
    },

    DAYS("days", "day", "d") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(milliseconds);
        }
        public long toMilliseconds(long time) {
            return java.util.concurrent.TimeUnit.DAYS.toMillis(time);
        }
    },

    WEEKS("weeks", "week", "w") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(milliseconds) / 7;
        }
        public long toMilliseconds(long time) {
            return java.util.concurrent.TimeUnit.DAYS.toMillis(time) * 7;
        }
    },

    MONTHS("months", "month", "mo") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(milliseconds) / 30;
        }
        public long toMilliseconds(long time) {
            return java.util.concurrent.TimeUnit.DAYS.toMillis(time) * 30;
        }
    },

    YEARS("years", "year", "y") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(milliseconds) / 365;
        }
        public long toMilliseconds(long time) {
            return java.util.concurrent.TimeUnit.DAYS.toMillis(time) * 365;
        }
    },

    UNDEFINED("undefined") {
        public long convertTime(long milliseconds) {
            return -1;
        }
        public long toMilliseconds(long time) {
            return 0;
        }
    };


    private final String name;
    private final String[] aliases;

    TimeUnit(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }
    
    public abstract long convertTime(long milliseconds);
    public abstract long toMilliseconds(long time);
    
    public static TimeUnit matchUnit(String unitString) {
        for (TimeUnit unit : values()) {
            if (unit.getName().equalsIgnoreCase(unitString)) {
                return unit;
            }

            for (String alias : unit.getAliases()) {
                if (alias.equalsIgnoreCase(unitString)) {
                    return unit;
                }
            }
        }

        return UNDEFINED;
    }
}