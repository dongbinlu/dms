package com.shudun.dms.handshake;

import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.TailInfo;

import java.io.IOException;

public abstract class AbstractConnectionResponseMessage extends AbstractMessage implements IResponse {

    public AbstractConnectionResponseMessage(HandShake handShake) {
        super(handShake);
    }

    @Override
    public byte[] decode(Message msg) {
        try {
            // 消息头
            HeadInfo headInfo = msg.getHeadInfo();

            // 消息PDU
            byte[] pdu = msg.getPdu();

            // 消息尾
            TailInfo tailInfo = msg.getTailInfo();

            byte[] toProtected = Message.builderMessageTrailer(headInfo, pdu);

            // 验签
            verifyMessageTrailer(toProtected, tailInfo.getMsg());

            return decodeMessageBody(pdu);
        } catch (Exception e) {
            throw new RuntimeException("解析失败");
        }
    }

    public Message encode(Message msg) throws IOException {
        // 消息头
        HeadInfo headInfo = this.getHeadInfo();
        headInfo.setMsgId(msg.getHeadInfo().getMsgId());

        //消息PDU
        byte[] pdu = encodeMessageBody(null);
        headInfo.setPduLength(pdu.length);

        // 消息尾
        byte[] toProtected = Message.builderMessageTrailer(headInfo, pdu);
        byte[] messageTrailer = createMessageTrailer(toProtected);
        TailInfo tailInfo = TailInfo.builder()
                .length(messageTrailer.length)
                .msg(messageTrailer)
                .build();

        return Message.builder()
                .headInfo(headInfo)
                .pdu(pdu)
                .tailInfo(tailInfo)
                .build();
    }

    protected abstract byte[] decodeMessageBody(byte[] data);

    protected abstract byte[] encodeMessageBody(byte[] data) throws IOException;

    protected abstract void verifyMessageTrailer(byte[] src, byte[] trailer);

    protected abstract byte[] createMessageTrailer(byte[] data);


}
