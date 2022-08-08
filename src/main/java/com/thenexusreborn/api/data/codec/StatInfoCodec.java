package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.NexusAPI;
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
        Info info = StatHelper.getInfo(encoded);
        if (info == null) {
            NexusAPI.getApi().getLogger().warning("Info for stat " + encoded + " is null.");
        }
        return info;
    }
}
