package com.thenexusreborn.api.network.netty.codec;

import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<NexusPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, NexusPacket msg, ByteBuf out) {
        NexusPacket.encodeString(out, msg.getOrigin());
        NexusPacket.encodeString(out, msg.getAction());
        
        if (msg.getData() == null) {
            out.writeInt(0);
        } else {
            out.writeInt(msg.getData().length);
        }
        
        if (msg.getData() != null && msg.getData().length > 0) {
            for (String data : msg.getData()) {
                NexusPacket.encodeString(out, data);
            }
        }
    }
}
