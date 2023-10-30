package com.thenexusreborn.api.storage.codec;

import com.thenexusreborn.api.helper.StringHelper;
import me.firestar311.starsql.api.objects.SqlCodec;

import java.util.*;

public class StringSetCodec implements SqlCodec<Set<String>> {
    @Override
    public String encode(Object object) {
        Set<String> stringSet = (Set<String>) object;
        if (stringSet.isEmpty()) {
            return null;
        }
        return StringHelper.join(stringSet, ",");
    }
    
    @Override
    public Set<String> decode(String encoded) {
        if (encoded == null) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(encoded.split(",")));
    }
}
