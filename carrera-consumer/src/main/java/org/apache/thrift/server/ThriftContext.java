package org.apache.thrift.server;

import org.apache.thrift.transport.TNonblockingSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;


public class ThriftContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftContext.class);
    private static Field frameBufferField;
    private static Field transField;

    static {
        try {
            frameBufferField = Invocation.class.getDeclaredField("frameBuffer");
            frameBufferField.setAccessible(true);
            transField = AbstractNonblockingServer.FrameBuffer.class.getDeclaredField("trans_");
            transField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            LOGGER.error("create thrift context error", e);
        }
    }

    public String remoteAddress;

    public ThriftContext(Runnable invocation) {
        //command should be instanceof Invocation
        try {
            AbstractNonblockingServer.FrameBuffer fb = (AbstractNonblockingServer.FrameBuffer) frameBufferField.get(invocation);
            TNonblockingSocket socket = (TNonblockingSocket) transField.get(fb);
            InetSocketAddress address = (InetSocketAddress) socket.getSocketChannel().getRemoteAddress();
            remoteAddress = address.getAddress().getHostAddress();
            LOGGER.debug("get thrift client address:{}", remoteAddress);
        } catch (Exception e) {
            LOGGER.error("create thrift context error", e);
        }
    }
}