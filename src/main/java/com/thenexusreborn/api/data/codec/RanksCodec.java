package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.data.objects.SqlCodec;
import com.thenexusreborn.api.player.Rank;

import java.util.*;

public class RanksCodec extends SqlCodec<Map<Rank, Long>> {
    @Override
    public String encode(Object object) {
        return null; //TODO
    }
    
    @Override
    public Map<Rank, Long> decode(String encoded) {
        return null; //TODO
    }
}
