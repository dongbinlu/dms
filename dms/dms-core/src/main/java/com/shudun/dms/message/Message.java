package com.shudun.dms.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 安全通道消息格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * 消息头
     */
    private HeadInfo headInfo;

    /**
     * 消息PDU
     */
    private byte[] pdu;


    /**
     * 消息尾
     */
    private TailInfo tailInfo;


    /**
     * 消息体->消息头 + 消息PDU + 消息尾
     */
    private byte[] data;

    /**
     * 构建消息尾
     *
     * @param headInfo
     * @param pdu
     * @return
     * @throws IOException
     */
    public static byte[] builderMessageTrailer(HeadInfo headInfo, byte[] pdu) throws IOException {
        byte[] toProtected;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); DataOutputStream dataOutputStream = new DataOutputStream(outputStream);) {

            dataOutputStream.write(headInfo.getVersion());
            dataOutputStream.writeByte(headInfo.getSecureModel());
            dataOutputStream.write(headInfo.getRetain());
            dataOutputStream.writeLong(headInfo.getMsgId());
            dataOutputStream.writeInt(pdu.length);
            dataOutputStream.write(headInfo.getDestId());
            dataOutputStream.write(headInfo.getSourceId());
            dataOutputStream.write(headInfo.getOpType());
            dataOutputStream.write(pdu);
            toProtected = outputStream.toByteArray();
        }
        return toProtected;
    }


}
