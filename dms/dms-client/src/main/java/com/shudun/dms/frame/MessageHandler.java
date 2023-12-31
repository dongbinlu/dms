package com.shudun.dms.frame;

import cn.hutool.core.util.ByteUtil;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.constant.ErrorCodeEnum;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteOrder;

/**
 * 处理0xA3消息的handler
 */
@Slf4j
@Service
@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        HeadInfo headInfo = msg.getHeadInfo();
        byte opType = headInfo.getOpType();
        if (opType == DmsConstants.MsgTypeEnum.DATA.getCode()) {
            log.info("客户端收到数据:{},msgId:{}", new String(msg.getPdu()), msg.getHeadInfo().getMsgId());
            // 在这可以判断是否需要回复
            ctx.channel().writeAndFlush(buildResponse(msg));
        } else {
            ctx.fireChannelRead(msg);
        }
    }
    private Message buildErrorResponse(Message msg) {
        byte[] pdu = ByteUtil.intToBytes(ErrorCodeEnum.SMR_NOSUCHDEV.getCode(), ByteOrder.BIG_ENDIAN);
        // 消息头
        HeadInfo headInfo = new HeadInfo();
        headInfo.setVersion((byte) 1);
        headInfo.setSecureModel((byte) 0B00000000);
        headInfo.setRetain(msg.getHeadInfo().getRetain());
        headInfo.setMsgId(msg.getHeadInfo().getMsgId());
        headInfo.setPduLength(pdu.length);
        headInfo.setDestId(msg.getHeadInfo().getSourceId());
        headInfo.setSourceId(msg.getHeadInfo().getDestId());
        headInfo.setOpType(DmsConstants.MsgTypeEnum.LOCAL_ERROR.getCode());

        // 消息PDU

        return Message.builder()
                .headInfo(headInfo)
                .pdu(pdu)
                .build();


    }

    private Message buildResponse(Message msg) {
        byte[] pdu = "hello server".getBytes();
        // 消息头
        HeadInfo headInfo = new HeadInfo();
        headInfo.setVersion((byte) 1);
        headInfo.setSecureModel((byte) 0B00000011);
        headInfo.setRetain(msg.getHeadInfo().getRetain());
        headInfo.setMsgId(msg.getHeadInfo().getMsgId());
        headInfo.setPduLength(pdu.length);
        headInfo.setDestId(msg.getHeadInfo().getSourceId());
        headInfo.setSourceId(msg.getHeadInfo().getDestId());
        headInfo.setOpType((byte) 0xA3);

        // 消息PDU

        return Message.builder()
                .headInfo(headInfo)
                .pdu(pdu)
                .build();


    }

}
