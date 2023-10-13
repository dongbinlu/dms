package com.shudun.dms.handshake;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HsmInfo {

    //本端签名公钥
    private byte[] localSignPk;

    //本端签名私钥
    private byte[] localSignSk;

    //本端加密公钥
    private byte[] localEncPk;

    //本端加密私钥
    private byte[] localEncSk;

    //本端ID
    private byte[] localId;

    //对端签名公钥
    private byte[] peerSignPk;

    //对端加密公钥
    private byte[] peerEncPk;

    //对端ID
    private byte[] peerId;

    // IP
    private String ip;

    // 端口
    private int port;


}
