package com.thenexusreborn.api.storage.codec;

import com.starmediadev.starsql.objects.SqlCodec;
import com.thenexusreborn.api.helper.StringHelper;

import java.util.*;

public class StringSetCodec implements SqlCodec<Set<String>> {
    @Override
    public String encode(Object object) {
        return StringHelper.join((Set<String>) object, ",");
    }
    
    @Override
    public Set<String> decode(String encoded) {
        return new HashSet<>(Arrays.asList(encoded.split(",")));
    }
}
