package com.thenexusreborn.api.storage.codec;

import com.starmediadev.starsql.objects.SqlCodec;
import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.stats.*;
import com.thenexusreborn.api.stats.Stat.Info;

public class StatInfoCodec implements SqlCodec<Info> {
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
