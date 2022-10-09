package com.thenexusreborn.api.storage.codec;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.storage.objects.SqlCodec;
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