package com.shudun.dms.handshake;

import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.TailInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public abstract class AbstractConnectionRequestMessage extends AbstractMessage implements IRequest {

    public AbstractConnectionRequestMessage(HandShake handShake) {
        super(handShake);
    }

    /**
     * 组装安全通道建立请求消息包
     *
     * @param data
     * @return
     */
    @Override
    public Message encode(byte[] data) {
        try {
            // 消息头
            HeadInfo headInfo = this.getHeadInfo();
            // 消息PDU
            byte[] pdu = this.encodeMessageBody(data);
            headInfo.setPduLength(pdu.length);

            // 消息尾
            byte[] toProtected = Message.builderMessageTrailer(headInfo, pdu);
            byte[] messageTrailer = this.createMessageTrailer(toProtected);
            TailInfo tailInfo = TailInfo.builder()
                    .length(messageTrailer.length)
                    .msg(messageTrailer)
                    .build();

            return Message.builder()
                    .headInfo(headInfo)
                    .pdu(pdu)
                    .tailInfo(tailInfo)
                    .build();
        } catch (Exception e) {
            log.error("AbstractConnectionRequestMessage encode error", e);
            throw new RuntimeException("解析错误", e);
        }
    }

    public byte[] decode(Message message) throws IOException {
        if ((message.getHeadInfo().getSecureModel() & DmsConstants.SecureModelEnum.SDM_SECMODE_SIGN.getCode()) != 0) {
            TailInfo tailInfo = message.getTailInfo();
            byte[] signData = tailInfo.getMsg();
            byte[] toProtected = Arrays.copyOfRange(message.getData(), 0, message.getData().length - 4 - tailInfo.getLength());
            verifyMessageTrailer(toProtected, signData);
        }
        return decodeMessageBody(message.getPdu());

    }

    protected abstract byte[] encodeMessageBody(byte[] data);

    protected abstract byte[] decodeMessageBody(byte[] data) throws IOException;

    protected abstract byte[] createMessageTrailer(byte[] data);

    public abstract void verifyMessageTrailer(byte[] src, byte[] trailer);


}
