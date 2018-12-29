package com.xiaojukeji.chronos.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;


public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());   /*HTTP 服务的解码器*/
        p.addLast(new HttpObjectAggregator(65536));  /*HTTP 消息的合并处理*/
        p.addLast(new NettyHttpServerHandler());
    }
}