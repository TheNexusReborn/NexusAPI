package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.data.objects.SqlCodec;
import com.thenexusreborn.api.helper.StringHelper;

import java.util.*;

public class StringSetCodec extends SqlCodec<Set<String>> {
    @Override
    public String encode(Object object) {
        return StringHelper.join((Set<String>) object, ",");
    }
    
    @Override
    public Set<String> decode(String encoded) {
        return new HashSet<>(Arrays.asList(encoded.split(",")));
    }
}
