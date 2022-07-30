package com.thenexusreborn.api.network.netty.app;

import com.thenexusreborn.api.NexusAPI;
import com.thenexusreborn.api.network.netty.codec.*;
import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NettyServer extends NettyApp {
    
    private final ChannelGroup channels = new DefaultChannelGroup("channels", GlobalEventExecutor.INSTANCE);
    
    public NettyServer(String host, int port) {
        super(host, port);
    }
    
    public void init() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        this.groups.add(boss);
        this.groups.add(worker);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channels.add(channel);
                            channel.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new ProcessingHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(host, port);
            channelFuture.addListener((ChannelFutureListener) f -> {
                if (f == channelFuture) {
                    NexusAPI.getApi().getLogger().info("Netty server started successfully");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            NexusAPI.getApi().getLogger().info("Netty server shutting down.");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            throw e;
        }
    }
    
    public ChannelGroup getChannels() {
        return channels;
    }
    
    @Override
    public void send(NexusPacket packet) {
        channels.writeAndFlush(packet);
    }
}
