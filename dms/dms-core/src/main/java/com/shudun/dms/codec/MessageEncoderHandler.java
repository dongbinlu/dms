package com.shudun.dms.codec;

import cn.hutool.core.util.ByteUtil;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.TailInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteOrder;

public class MessageEncoderHandler extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf buffer) throws Exception {
        HeadInfo headInfo = msg.getHeadInfo();
        byte[] pdu = msg.getPdu();
        TailInfo tailInfo = msg.getTailInfo();
        // 消息头
        buffer.writeByte(headInfo.getVersion());
        buffer.writeByte(headInfo.getSecureModel());
        buffer.writeBytes(headInfo.getRetain());
        long msgId = headInfo.getMsgId();
        buffer.writeBytes(ByteUtil.longToBytes(msgId, ByteOrder.BIG_ENDIAN));
        buffer.writeInt(pdu == null || pdu.length == 0 ? 0 : pdu.length);
        buffer.writeBytes(headInfo.getDestId());
        buffer.writeBytes(headInfo.getSourceId());
        buffer.writeByte(headInfo.getOpType());

        if (pdu != null && pdu.length != 0) {
            // 消息PDU
            buffer.writeBytes(pdu);
        }
        byte secureModel = headInfo.getSecureModel();
        if ((secureModel & DmsConstants.SecureModelEnum.SDM_SECMODE_SIGN.getCode()) != 0 && tailInfo != null && tailInfo.getLength() > 0) {
            // 消息尾
            buffer.writeInt(tailInfo.getLength());
            buffer.writeBytes(tailInfo.getMsg());
        }
    }
}
