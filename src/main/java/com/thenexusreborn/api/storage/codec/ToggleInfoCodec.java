package com.thenexusreborn.api.storage.codec;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.player.Toggle;
import com.thenexusreborn.api.player.Toggle.Info;
import me.firestar311.starsql.api.objects.SqlCodec;

public class ToggleInfoCodec implements SqlCodec<Info> {
    @Override
    public String encode(Object object) {
        return ((Toggle.Info) object).getName();
    }
    
    @Override
    public Toggle.Info decode(String encoded) {
        return NexusAPI.getApi().getToggleRegistry().get(encoded);
    }
}
