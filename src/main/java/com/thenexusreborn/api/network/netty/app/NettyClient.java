package com.thenexusreborn.api.network.netty.app;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.network.netty.codec.*;
import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient extends NettyApp {
    
    private SocketChannel channel;
    
    public NettyClient(String host, int port) {
        super(host, port);
    }
    
    public void init() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        this.groups.add(group);
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            NexusAPI.getApi().getLogger().info("Init channel on client.");
                            setChannel(socketChannel);
                            socketChannel.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new ProcessingHandler());
                        }
                    });
            ChannelFuture channelFuture = clientBootstrap.connect(host, port);
            channelFuture.addListener((ChannelFutureListener) f -> {
                if (f == channelFuture) {
                    NexusAPI.getApi().getLogger().info("Netty client connected successfully");
                }
            });
        } catch (Exception e) {
            NexusAPI.getApi().getLogger().info("Netty Client shutting down.");
            group.shutdownGracefully();
            throw e;
        }
    }
    
    public SocketChannel getChannel() {
        return channel;
    }
    
    private void setChannel(SocketChannel channel) {
        this.channel = channel;
    }
    
    @Override
    public void send(NexusPacket packet) {
        channel.writeAndFlush(packet);
    }
}
