package com.thenexusreborn.api.network.netty.codec;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<NexusPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, NexusPacket msg, ByteBuf out) {
        NexusAPI.getApi().getLogger().info("Encoding " + msg.toString());
        out.writeInt(msg.getOrigin().length());
        out.writeCharSequence(msg.getOrigin(), NexusPacket.CHARSET);
        out.writeInt(msg.getAction().length());
        out.writeCharSequence(msg.getAction(), NexusPacket.CHARSET);
        
        if (msg.getData() == null) {
            out.writeInt(0);
        } else {
            out.writeInt(msg.getData().length);
        }
        
        if (msg.getData() != null && msg.getData().length > 0) {
            for (String data : msg.getData()) {
                out.writeInt(data.length());
                out.writeCharSequence(data, NexusPacket.CHARSET);
            }
        }
    }
}
