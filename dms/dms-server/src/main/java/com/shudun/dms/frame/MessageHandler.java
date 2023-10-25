package com.shudun.dms.frame;

import cn.hutool.core.util.ByteUtil;
import com.shudun.dms.channel.IChannel;
import com.shudun.dms.channel.JavaChannel;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.MessageFuture;
import com.shudun.dms.pdu.PduProcessor;
import com.shudun.dms.pdu.PduProcessorFactory;
import com.shudun.dms.pdu.smf.SMFPduProcessor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理0xA3消息的handler
 */
@Service
@ChannelHandler.Sharable
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {

    @Autowired
    private SMFPduProcessor smfPduProcessor;

    @Autowired
    private PduProcessorFactory pduProcessorFactory;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        HeadInfo headInfo = msg.getHeadInfo();
        byte opType = headInfo.getOpType();

        if (opType == DmsConstants.MsgTypeEnum.DATA.getCode() || opType == DmsConstants.MsgTypeEnum.LOCAL_ERROR.getCode()) {

            long msgId = msg.getHeadInfo().getMsgId();
            log.info("服务端收到数据:{},msgId:{} ", new String(msg.getPdu()), msgId);

            // 1、DMS 发送
            Attribute<IChannel> attribute = ctx.channel().attr(GlobalVariable.CHANNEL_KEY);
            JavaChannel javaChannel = (JavaChannel) attribute.get();
            ConcurrentHashMap<Long, MessageFuture> futures = javaChannel.getFutures();
            MessageFuture messageFuture = futures.get(msgId);
            if (messageFuture != null) {
                if (opType == DmsConstants.MsgTypeEnum.LOCAL_ERROR.getCode()) {
                    byte[] pdu = msg.getPdu();
                    long errCode = ByteUtil.bytesToLong(pdu, ByteOrder.BIG_ENDIAN);
                    messageFuture.setErrCode(errCode);
                }
                messageFuture.setResultMessage(msg);
                futures.remove(msgId);
                return;
            }

            // 2、SMF 发送
            HandShake handShake = javaChannel.getHandShake();
            byte[] secretKey = handShake.getSecretKey();
            if (secretKey == null || secretKey.length == 0) {
                smfPduProcessor.handler(msg, javaChannel);
                return;
            }

            // 3、客户端主动请求
            PduProcessor processor = pduProcessorFactory.getProcessor(msg);
            if (processor != null) {
                processor.handler(msg, javaChannel);
                return;
            }
        } else if (opType == DmsConstants.MsgTypeEnum.GET_LOCAL_ID.getCode()) {
            HeadInfo localInHeadInfo = new HeadInfo();
            localInHeadInfo.setVersion(headInfo.getVersion());
            localInHeadInfo.setSecureModel(DmsConstants.SecureModelEnum.SDM_SECMODE_NOT.getCode());
            localInHeadInfo.setRetain(headInfo.getRetain());
            localInHeadInfo.setMsgId(headInfo.getMsgId());
            localInHeadInfo.setPduLength(0);
            localInHeadInfo.setDestId(Arrays.copyOf("".getBytes(), 32));
            localInHeadInfo.setSourceId(Arrays.copyOf("22222222222222222222222222222222".getBytes(), 32));
            localInHeadInfo.setOpType(DmsConstants.MsgTypeEnum.GET_LOCAL_ID.getCode());

            ctx.channel().writeAndFlush(Message.builder().headInfo(localInHeadInfo).build());
        } else {
            ctx.fireChannelRead(msg);
        }
    }

}
