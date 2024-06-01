package com.thenexusreborn.api.sql.objects.codecs;

import com.thenexusreborn.api.sql.objects.SqlCodec;

import java.util.HashMap;
import java.util.Map;

public class ValueDataCodec implements SqlCodec<Map<String, String>> {
    @Override
    public String encode(Object object) {
        Map<String, String> data = (Map<String, String>) object;
        StringBuilder sb = new StringBuilder();
        data.forEach((key, value) -> sb.append(key).append("=").append(value).append(","));
        
        sb.deleteCharAt(sb.length() - 1);
        
        return sb.toString();
    }

    @Override
    public Map<String, String> decode(String encoded) {
        Map<String, String> data = new HashMap<>();
        
        String[] elements = encoded.split(",");
        for (String element : elements) {
            String[] keyValue = element.split("=");
            data.put(keyValue[0], keyValue[1]);
        }
        
        return data;
    }
}
