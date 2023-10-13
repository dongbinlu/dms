package com.shudun.dms.codec;

import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.TailInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

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
        buffer.writeLong(headInfo.getMsgId());
        buffer.writeInt(pdu == null || pdu.length == 0 ? 0 : pdu.length);
        buffer.writeBytes(headInfo.getDestId());
        buffer.writeBytes(headInfo.getSourceId());
        buffer.writeByte(headInfo.getOpType());

        if (pdu != null && pdu.length != 0) {
            // 消息PDU
            buffer.writeBytes(pdu);
        }
        byte secureModel = headInfo.getSecureModel();
        boolean signed = (secureModel & (byte) 0B00000001) > 0;
        if (signed && tailInfo != null && tailInfo.getLength() > 0) {
            // 消息尾
            buffer.writeInt(tailInfo.getLength());
            buffer.writeBytes(tailInfo.getMsg());
        }
    }
}
