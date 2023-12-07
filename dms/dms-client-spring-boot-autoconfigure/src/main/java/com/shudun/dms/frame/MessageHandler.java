package com.shudun.dms.frame;

import cn.hutool.core.util.ByteUtil;
import com.shudun.dms.channel.IChannel;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.MessageFuture;
import com.shudun.dms.pdu.PduProcessor;
import com.shudun.dms.pdu.PduProcessorFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理0xA3消息的handler
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<Message> {

    private ConcurrentHashMap<Long, MessageFuture> futures;

    private PduProcessorFactory pduProcessorFactory;

    public MessageHandler(PduProcessorFactory pduProcessorFactory) {
        this.pduProcessorFactory = pduProcessorFactory;
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
            // 主动发送
            Attribute<IChannel> attribute = ctx.channel().attr(GlobalVariable.CHANNEL_KEY);

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

            // 被动接收
            PduProcessor pduProcessor = pduProcessorFactory.getProcessor(msg);
            if (null == pduProcessor) {
                log.warn("pdu processor cannot be found");
                return;
            }
            pduProcessor.handler(msg, attribute.get());
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("MessageHandler Exception", cause);
        ctx.fireExceptionCaught(cause);
    }

    public void setFutures(ConcurrentHashMap<Long, MessageFuture> futures) {
        this.futures = futures;
    }
}
