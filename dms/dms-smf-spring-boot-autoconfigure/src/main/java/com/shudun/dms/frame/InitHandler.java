package com.shudun.dms.frame;

import com.shudun.dms.codec.MessageDecodeHandler;
import com.shudun.dms.codec.MessageEncoderHandler;
import com.shudun.dms.rpc.MessageFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.ConcurrentHashMap;

public class InitHandler extends ChannelInitializer<SocketChannel> {

    private ConcurrentHashMap<Long, MessageFuture> futures;

    public InitHandler(ConcurrentHashMap<Long, MessageFuture> futures) {
        this.futures = futures;
    }

    /**
     * SMF 接口没有加密、解密、签名、验签
     *
     * @param ch
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new HighAndLowWaterLevelHandler());//水位线
        ch.pipeline().addLast(new MessageEncoderHandler());
        ch.pipeline().addLast(new MessageDecodeHandler());
        ch.pipeline().addLast(new MessageHandler(futures));
    }
}

