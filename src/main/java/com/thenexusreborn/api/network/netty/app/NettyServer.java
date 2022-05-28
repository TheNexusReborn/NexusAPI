package com.thenexusreborn.api.network.netty.app;

import com.thenexusreborn.api.network.netty.codec.*;
import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;

public class NettyServer extends NettyApp {
    
    private ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
    public NettyServer(String host, int port) {
        super(host, port);
    }
    
    public void init() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(host, port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) {
                    channels.add(channel);
                    channel.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new ProcessingHandler());
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
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
