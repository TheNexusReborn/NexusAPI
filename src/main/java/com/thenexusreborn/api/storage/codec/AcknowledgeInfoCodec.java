package com.thenexusreborn.api.storage.codec;

import com.thenexusreborn.api.storage.objects.SqlCodec;
import com.thenexusreborn.api.punishment.AcknowledgeInfo;

public class AcknowledgeInfoCodec implements SqlCodec<AcknowledgeInfo> {
    @Override
    public String encode(Object object) {
        AcknowledgeInfo acknowledgeInfo = (AcknowledgeInfo) object;
        if (acknowledgeInfo != null) {
            return "code=" + acknowledgeInfo.getCode() + ",time=" + acknowledgeInfo.getTime();
        }
        
        return "";
    }
    
    @Override
    public AcknowledgeInfo decode(String encoded) {
        AcknowledgeInfo acknowledgeInfo = null;
        if (encoded != null && !encoded.equals("")) {
            String[] piSplit = encoded.split(",");
            long ackTime = 0;
            String ackCode = "";
            if (piSplit != null && piSplit.length == 2) {
                for (String d : piSplit) {
                    String[] dSplit = d.split("=");
                    if (dSplit != null && dSplit.length == 2) {
                        if (dSplit[0].equalsIgnoreCase("time")) {
                            ackTime = Long.parseLong(dSplit[1]);
                        } else if (dSplit[0].equalsIgnoreCase("code")) {
                            ackCode = dSplit[1];
                        }
                    }
                }
            }
            acknowledgeInfo = new AcknowledgeInfo(ackCode, ackTime);
        }
        return acknowledgeInfo;
    }
}
