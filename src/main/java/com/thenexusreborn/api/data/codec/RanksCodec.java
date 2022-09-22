package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.data.objects.SqlCodec;
import com.thenexusreborn.api.player.*;

import java.util.*;
import java.util.Map.Entry;

public class RanksCodec extends SqlCodec<Map<Rank, Long>> {
    @Override
    public String encode(Object object) {
        Map<Rank, Long> ranks = (Map<Rank, Long>) object;
        
        StringBuilder sb = new StringBuilder();
    
        if (ranks.size() == 0) {
            return Rank.MEMBER.name() + "=-1";
        }
    
        for (Entry<Rank, Long> entry : ranks.entrySet()) {
            sb.append(entry.getKey().name()).append("=").append(entry.getValue()).append(",");
        }
    
        if (sb.length() > 0) {
            return sb.substring(0, sb.toString().length() - 1);
        } else {
            return "";
        }
    }
    
    @Override
    public Map<Rank, Long> decode(String encoded) {
        Map<Rank, Long> ranks = new EnumMap<>(Rank.class);
        if (encoded == null && encoded.equals("")) {
            return ranks;
        }
    
        String[] rawRanks = encoded.split(",");
        if (rawRanks == null || rawRanks.length == 0) {
            return ranks;
        }
    
        for (String rawRank : rawRanks) {
            String[] rankSplit = rawRank.split("=");
            if (rankSplit == null || rankSplit.length != 2) {
                continue;
            }
        
            Rank rank = Rank.parseRank(rankSplit[0]);
            long expire = Long.parseLong(rankSplit[1]);
            ranks.put(rank, expire);
        }
        return ranks;
    }
}
