package com.thenexusreborn.api.util;

import java.util.Objects;

public record Range<T>(int min, int max, T object) {
    
    public boolean contains(int number) {
        return (number >= min && number <= max);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Range<?> range = (Range<?>) o;
        return min == range.min && max == range.max;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}