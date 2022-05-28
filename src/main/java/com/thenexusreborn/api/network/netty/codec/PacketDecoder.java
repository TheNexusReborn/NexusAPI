package com.thenexusreborn.api.network.netty.codec;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class PacketDecoder extends ReplayingDecoder<NexusPacket> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int length = in.readInt();
        String origin = (String) in.readCharSequence(length, NexusPacket.CHARSET);
        length = in.readInt();
        String action = (String) in.readCharSequence(length, NexusPacket.CHARSET);
        length = in.readInt();
        String[] data = new String[length];
        for (int i = 0; i < length; i++) {
            int l = in.readInt();
            data[i] = (String) in.readCharSequence(l, NexusPacket.CHARSET);
        }
        
        NexusPacket nexusPacket = new NexusPacket(origin, action, data);
        out.add(nexusPacket);
        NexusAPI.getApi().getLogger().info("Received packet: " + nexusPacket);
    }
}