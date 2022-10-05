package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.data.objects.SqlCodec;
import com.thenexusreborn.api.player.Toggle;

public class PreferenceInfoCodec extends SqlCodec<Toggle.Info> {
    @Override
    public String encode(Object object) {
        return ((Toggle.Info) object).getName();
    }
    
    @Override
    public Toggle.Info decode(String encoded) {
        return NexusAPI.getApi().getPreferenceRegistry().get(encoded);
    }
}
