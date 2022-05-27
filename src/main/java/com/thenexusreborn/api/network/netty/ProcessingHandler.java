package com.thenexusreborn.api.network.netty;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.channel.*;

public class ProcessingHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        NexusPacket packet = (NexusPacket) msg;
        NexusAPI.getApi().getNetworkManager().handleInboundPacket(packet);
    }
}
