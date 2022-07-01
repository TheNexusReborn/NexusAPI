package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.data.objects.SqlCodec;
import com.thenexusreborn.api.tags.Tag;

public class TagCodec extends SqlCodec<Tag> {
    @Override
    public String encode(Object object) {
        return ((Tag) object).getName();
    }
    
    @Override
    public Tag decode(String encoded) {
        return new Tag(encoded);
    }
}
