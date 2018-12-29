package com.xiaojukeji.chronos.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NettyHttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpServer.class);

    private static volatile NettyHttpServer instance = null;

    private ChannelFuture channel;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public NettyHttpServer() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
    }

    public void start() {
        LOGGER.info("start netty http server");
        final ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ServerInitializer())
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            channel = bootstrap.bind(2222).sync();
        } catch (InterruptedException e) {
            LOGGER.error("error while start netty http server, err:{}", e.getMessage(), e);
        }
    }

    public void shutdown() {
        LOGGER.info("shutdown netty http server");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();

        try {
            channel.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("error while shutdown netty http server, err:{}", e.getMessage(), e);
        }
    }

    public static NettyHttpServer getInstance() {
        if (instance == null) {
            synchronized (NettyHttpServer.class) {
                if (instance == null) {
                    instance = new NettyHttpServer();
                }
            }
        }
        return instance;
    }
}