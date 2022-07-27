package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.objects.SqlCodec;
import com.thenexusreborn.api.player.Preference;
import com.thenexusreborn.api.stats.Stat.Info;

public class PreferenceInfoCodec extends SqlCodec<Preference.Info> {
    @Override
    public String encode(Object object) {
        return ((Info) object).getName();
    }
    
    @Override
    public Preference.Info decode(String encoded) {
        return NexusAPI.getApi().getPreferenceRegistry().get(encoded);
    }
}
