package com.thenexusreborn.api.network.netty.codec;

import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<NexusPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, NexusPacket msg, ByteBuf out) {
        out.writeInt(msg.origin().length());
        out.writeCharSequence(msg.origin(), NexusPacket.CHARSET);
        out.writeInt(msg.action().length());
        out.writeCharSequence(msg.action(), NexusPacket.CHARSET);
        
        if (msg.data() == null) {
            out.writeInt(0);
        } else {
            out.writeInt(msg.data().length);
        }
        
        if (msg.data() != null) {
            for (String data : msg.data()) {
                out.writeInt(data.length());
                out.writeCharSequence(data, NexusPacket.CHARSET);
            }
        }
    }
}
