package com.shudun.dms.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * 消息尾
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TailInfo {

    /**
     * 本端签名私钥对消息头和消息PDU的签名值长度或
     * 安全通道会话密钥对消息头和消息PDU计算的HMAC长度
     */
    private int length;

    /**
     * 本端签名私钥对消息头和消息PDU的签名值或
     * 安全通道会话密钥对消息头和消息PDU计算的16字节的HMAC值
     */
    private byte[] msg;

    public void decode(byte[] data) throws IOException {
        try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data))) {
            this.length = dataInputStream.readInt();
            msg = new byte[this.length];
            dataInputStream.read(msg);
        }
    }
}
