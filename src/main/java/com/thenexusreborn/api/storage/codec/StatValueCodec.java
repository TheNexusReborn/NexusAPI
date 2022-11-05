package com.thenexusreborn.api.storage.codec;

import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.storage.objects.SqlCodec;

public class StatValueCodec extends SqlCodec<StatValue> {
    @Override
    public String encode(Object object) {
        StatValue statValue = (StatValue) object;
        if (statValue == null) {
            return "null";
        }
        String encoded = statValue.getType() + ":";
        if (statValue.get() == null) {
            return encoded + "null";
        } else {
            return encoded + StatHelper.serializeStatValue(statValue.getType(), statValue.get());
        }
    }
    
    @Override
    public StatValue decode(String encoded) {
        if (encoded == null || encoded.equals("") || encoded.equalsIgnoreCase("null")) {
            return null;
        }
        String[] split = encoded.split(":");
        if (split.length == 1) {
            return new StatValue(StatType.valueOf(split[0]), null);
        }
    
        StatType type = StatType.valueOf(split[0].toUpperCase());
        Object value = StatHelper.parseValue(type, split[1]);
        return new StatValue(type, value);
    }
}
