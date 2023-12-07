package com.shudun.dms.message;

import cn.hutool.core.util.ByteUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * 消息头
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadInfo {

    /**
     * 版本号-1字节
     * 目前为1
     */
    private byte version;

    /**
     * 安全模式-1字节
     * 目前只设置低 3 位(D2D1D0),分别代表
     * 是否需要回复信息(D2),是否加密(D1)和是否签名(DO)。
     * D2 置为 0 表示不需回复,1 表示需要回复;
     * D1 置为 0表示未加密1表示已加密;
     * DO 置为0表示未签名/未计算 HMAC,1表示已签名/已计算 HMAC
     */
    private byte secureModel;

    /**
     * 保留-2字节
     */
    private byte[] retain = new byte[2];

    /**
     * 消息ID-8字节
     * 用来防止重放,每个被管设备自已维护,依次递增,当大于某个指定值时,应重新建立
     * 安全通道，重新建立以后该消息 ID清 0;
     */
    private long msgId;

    /**
     * PDU长度-4字节
     * 消息PDU字节长度
     */
    private int pduLength;

    /**
     * 目的方ID-32字节
     * 当目的方ID 与接收方自身ID 不一致时,则接受方尝试通过接收方与目的方的安全通道转发这条消息,
     * 如安全通道不存在,则中止转发;
     */
    private byte[] destId = new byte[32];

    /**
     * 发送方ID-32字节
     */
    private byte[] sourceId = new byte[32];

    /**
     * 操作类型-1字节
     * 安全通道建立请求-0xA1
     * 安全通道建立响应-0xA2
     * 安全通道发送消息-0xA3
     * 安全通道重启-0xA4
     */
    private byte opType;

    public void decode(byte[] data) throws IOException {

        try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data))) {
            this.version = dataInputStream.readByte();
            this.secureModel = dataInputStream.readByte();
            dataInputStream.read(this.retain);
            byte[] msgId = new byte[8];
            dataInputStream.readFully(msgId);
            this.msgId = ByteUtil.bytesToLong(msgId, ByteOrder.BIG_ENDIAN);;
            this.pduLength = dataInputStream.readInt();
            dataInputStream.read(this.destId);
            dataInputStream.read(this.sourceId);
            this.opType = dataInputStream.readByte();
        }
    }


}
