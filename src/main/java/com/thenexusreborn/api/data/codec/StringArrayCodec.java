package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.data.objects.SqlCodec;

public class StringArrayCodec extends SqlCodec<String[]> {
    @Override
    public String encode(Object object) {
        String[] value = (String[]) object;
        StringBuilder sb = new StringBuilder();
        for (String s : value) {
            sb.append(s).append(",");
        }
        
        return sb.substring(0, sb.length() - 1);
    }
    
    @Override
    public String[] decode(String encoded) {
        return encoded.split(",");
    }
}
