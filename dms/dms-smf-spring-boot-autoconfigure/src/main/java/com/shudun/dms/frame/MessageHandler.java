package com.shudun.dms.frame;

import cn.hutool.core.util.ByteUtil;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.MessageFuture;
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

    /**
     * 这里通过构造方法将futures传入进来，因为SMF可有连接多个Socket,
     * 所以每个Sokcet对于一个futures。
     */
    private ConcurrentHashMap<Long, MessageFuture> futures;

    public MessageHandler(ConcurrentHashMap<Long, MessageFuture> futures) {
        this.futures = futures;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        HeadInfo headInfo = msg.getHeadInfo();
        byte opType = headInfo.getOpType();
        if (opType == DmsConstants.MsgTypeEnum.DATA.getCode() ||
                opType == DmsConstants.MsgTypeEnum.GET_LOCAL_ID.getCode() ||
                opType == DmsConstants.MsgTypeEnum.LOCAL_ERROR.getCode()) {
            long msgId = msg.getHeadInfo().getMsgId();
            log.info("收到响应数据,msgId:{}", msgId);
            MessageFuture messageFuture = futures.get(msgId);
            if (messageFuture != null) {
                if (opType == DmsConstants.MsgTypeEnum.LOCAL_ERROR.getCode()) {
                    byte[] pdu = msg.getPdu();
                    int errCode = ByteUtil.bytesToInt(pdu, ByteOrder.BIG_ENDIAN);
                    messageFuture.setErrCode(errCode);
                }
                messageFuture.setResultMessage(msg);
                futures.remove(msgId);
                log.info("接收响应数据,msgId:{}", msgId);
                return;
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
