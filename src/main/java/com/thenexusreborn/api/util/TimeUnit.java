package com.thenexusreborn.api.util;

public enum TimeUnit {
    TICKS("ticks", "tick", "t") {
        public long convertTime(long milliseconds) {
            return milliseconds / 50;
        }
    },
    
    SECONDS("seconds", "second", "s") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        }
    },

    MINUTES("minutes", "minute", "min", "m") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        }
    },

    HOURS("hours", "hour", "h") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toHours(milliseconds);
        }
    },

    DAYS("days", "day", "d") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(milliseconds);
        }
    },

    WEEKS("weeks", "week", "w") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(milliseconds) / 7;
        }
    },

    MONTHS("months", "month", "mo") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(milliseconds) / 30;
        }
    },

    YEARS("years", "year", "y") {
        public long convertTime(long milliseconds) {
            return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(milliseconds) / 365;
        }
    },

    UNDEFINED("undefined") {
        public long convertTime(long milliseconds) {
            return -1;
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