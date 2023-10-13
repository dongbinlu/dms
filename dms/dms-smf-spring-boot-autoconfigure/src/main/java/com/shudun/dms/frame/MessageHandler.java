package com.shudun.dms.frame;

import cn.hutool.core.util.ByteUtil;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.rpc.MessageFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理0xA3消息的handler
 */
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {

    private ConcurrentHashMap<Long, MessageFuture> futures;

    public MessageHandler(ConcurrentHashMap<Long, MessageFuture> futures) {
        this.futures = futures;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        HeadInfo headInfo = msg.getHeadInfo();
        byte opType = headInfo.getOpType();
        if (opType == (byte) 0xA3 || opType == (byte) 0xF0 || opType == (byte) 0xFE) {
            long msgId = msg.getHeadInfo().getMsgId();
            log.info("收到响应数据,msgId:{}", msgId);
            MessageFuture messageFuture = futures.get(msgId);
            if (messageFuture != null) {
                if (opType == (byte) 0xFE) {
                    byte[] pdu = msg.getPdu();
                    int errCode = ByteUtil.bytesToInt(pdu, ByteOrder.BIG_ENDIAN);
                    messageFuture.setErrCode(errCode);
                }
                messageFuture.setResultMessage(msg);
                log.info("接收响应数据,msgId:{}", msgId);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("MessageHandler Exception", cause);
        ctx.fireExceptionCaught(cause);
    }
}