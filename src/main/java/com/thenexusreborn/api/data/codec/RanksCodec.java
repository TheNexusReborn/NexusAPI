package com.thenexusreborn.api.data.codec;

import com.thenexusreborn.api.data.objects.SqlCodec;
import com.thenexusreborn.api.player.*;

import java.util.*;
import java.util.Map.Entry;

public class RanksCodec extends SqlCodec<PlayerRanks> {
    @Override
    public String encode(Object object) {
        Map<Rank, Long> ranks = ((PlayerRanks) object).findAll();
        
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
    public PlayerRanks decode(String encoded) {
        PlayerRanks playerRanks = new PlayerRanks(null);
        Map<Rank, Long> ranks = new EnumMap<>(Rank.class);
        if (encoded == null || encoded.equals("")) {
            return playerRanks;
        }
    
        String[] rawRanks = encoded.split(",");
        if (rawRanks == null || rawRanks.length == 0) {
            return playerRanks;
        }
    
        for (String rawRank : rawRanks) {
            String[] rankSplit = rawRank.split("=");
            if (rankSplit == null || rankSplit.length != 2) {
                continue;
            }
        
            Rank rank = Rank.parseRank(rankSplit[0]);
            long expire = Long.parseLong(rankSplit[1]);
            playerRanks.add(rank, expire);
        }
        return playerRanks;
    }
}
