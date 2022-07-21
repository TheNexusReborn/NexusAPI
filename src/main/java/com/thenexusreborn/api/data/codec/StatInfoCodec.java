package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.data.objects.SqlCodec;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.stats.Stat.Info;

public class StatInfoCodec extends SqlCodec<Stat.Info> {
    @Override
    public String encode(Object object) {
        return ((Stat.Info) object).getName();
    }
    
    @Override
    public Info decode(String encoded) {
        return StatHelper.getInfo(encoded);
    }
}
