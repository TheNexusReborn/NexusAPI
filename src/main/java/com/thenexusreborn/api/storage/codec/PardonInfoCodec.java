package com.thenexusreborn.api.storage.codec;

import com.thenexusreborn.api.storage.objects.SqlCodec;
import com.thenexusreborn.api.punishment.PardonInfo;

public class PardonInfoCodec implements SqlCodec<PardonInfo> {
    @Override
    public String encode(Object object) {
        PardonInfo pardonInfo = (PardonInfo) object;
        if (pardonInfo != null) {
            return "date=" + pardonInfo.getDate() + ",actor=" + pardonInfo.getActor() + ",reason=" + pardonInfo.getReason();
        }
        
        return "";
    }
    
    @Override
    public PardonInfo decode(String encoded) {
        PardonInfo pardonInfo = null;
        if (encoded != null && !encoded.equals("") && !encoded.equalsIgnoreCase("null")) {
            String[] piSplit = encoded.split(",");
            long pardonDate = 0;
            String pardonActor = "";
            String pardonReason = "";
            if (piSplit != null && piSplit.length == 3) {
                for (String d : piSplit) {
                    String[] dSplit = d.split("=");
                    if (dSplit != null && dSplit.length == 2) {
                        if (dSplit[0].equalsIgnoreCase("date")) {
                            pardonDate = Long.parseLong(dSplit[1]);
                        } else if (dSplit[0].equalsIgnoreCase("actor")) {
                            pardonActor = dSplit[1];
                        } else if (dSplit[0].equalsIgnoreCase("reason")) {
                            pardonReason = dSplit[1];
                        }
                    }
                }
            }
            pardonInfo = new PardonInfo(pardonDate, pardonActor, pardonReason);
        }
        return pardonInfo;
    }
}
