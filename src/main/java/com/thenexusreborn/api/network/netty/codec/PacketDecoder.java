package com.thenexusreborn.api.network.netty.codec;

import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class PacketDecoder extends ReplayingDecoder<NexusPacket> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        String origin = NexusPacket.decodeString(in);
        String action = NexusPacket.decodeString(in);
        int dataLength = in.readInt();
        String[] data = new String[dataLength];
        for (int i = 0; i < dataLength; i++) {
            data[i] = NexusPacket.decodeString(in);
        }
        
        NexusPacket nexusPacket = new NexusPacket(origin, action, data);
        out.add(nexusPacket);
    }
}