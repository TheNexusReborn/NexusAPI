package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.data.objects.SqlCodec;

import java.util.UUID;

public class UUIDCodec extends SqlCodec<UUID> {
    @Override
    public String encode(Object object) {
        return object.toString();
    }
    
    @Override
    public UUID decode(String encoded) {
        return UUID.fromString(encoded);
    }
}
