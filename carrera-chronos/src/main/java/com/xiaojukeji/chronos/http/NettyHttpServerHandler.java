package com.xiaojukeji.chronos.http;

import com.xiaojukeji.chronos.db.BackupDB;
import com.xiaojukeji.chronos.enums.BackupState;
import com.xiaojukeji.chronos.enums.RestoreState;
import com.xiaojukeji.chronos.utils.LogUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;


public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogUtils.BACKUP_RESTORE_LOGGER;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BackupState backupState = BackupState.ERROR;

        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            if (req.getUri().equals("/chronos/backup")) {
                backupState = BackupDB.backup();
                LOGGER.info("backupState:{}", backupState.getDesc());

                RestoreState restoreState = BackupDB.restore();
                LOGGER.info("restoreState:{}", restoreState.getDesc());
            }
        } else {
            LOGGER.error("request is not FullHttpRequest");
        }

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(backupState.getDesc().getBytes("utf-8")));
        response.headers().set(Names.CONTENT_TYPE, "text/plain;charset=UTF-8");
        response.headers().set(Names.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(Names.CONNECTION, Values.KEEP_ALIVE);
        ctx.write(response);
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}