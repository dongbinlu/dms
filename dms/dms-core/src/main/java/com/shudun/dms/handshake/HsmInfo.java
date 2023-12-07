package com.shudun.dms.handshake;

import cn.com.shudun.util.CertTools;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.cert.X509Certificate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HsmInfo{

    /**
     * 本端签名公钥
     */
    private byte[] localSignPk;

    /**
     * 本端签名私钥
     */
    private byte[] localSignSk;

    /**
     * 本端加密公钥
     */
    private byte[] localEncPk;

    /**
     * 本端加密私钥
     */
    private byte[] localEncSk;

    /**
     * 发送方ID
     */
    private byte[] sourceId;

    /**
     * 对端签名公钥
     */
    private byte[] peerSignPk;

    /**
     * 对端加密公钥
     */
    private byte[] peerEncPk;

    /**
     * 目的方ID
     */
    private byte[] destId;

    /**
     * IP
     */
    private String ip;

    /**
     * 端口
     */
    private int port;



    public static byte[] toPublicKey(String baseCert) {
        X509Certificate x509Certificate = null;
        try {
            x509Certificate = CertTools.generateCertificate(baseCert);
        } catch (Exception e) {
            throw new RuntimeException("证书处理异常");
        }
        return x509Certificate.getPublicKey().getEncoded();
    }


}
