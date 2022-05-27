package com.thenexusreborn.api.network.netty.app;

import com.thenexusreborn.api.network.netty.codec.ProcessingHandler;
import com.thenexusreborn.api.network.netty.codec.*;
import com.thenexusreborn.api.network.netty.model.NexusPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class NettyClient extends NettyApp {
    
    private SocketChannel channel;
    
    public NettyClient(String host, int port) {
        super(host, port);
    }
    
    public void init() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            setChannel(socketChannel);
                            socketChannel.pipeline().addLast(new PacketDecoder(), new PacketEncoder(), new ProcessingHandler());
                        }
                    });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
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
