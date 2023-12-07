package com.shudun.dms.handshake;

import cn.com.shudun.cert.AbstractCertMaker;
import cn.com.shudun.cert.gmhelper.cert.SM2X509CertMakerImpl;
import com.shudun.dms.algorithm.AbstractMessageAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 建立安全通道-握手基本信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandShake {

    private AtomicLong atomicMsgId = new AtomicLong(0L);

    private AbstractMessageAlgorithm abstractMessageAlgorithm;

    /**
     * 目的方ID
     */
    private byte[] destId = new byte[32];

    /**
     * 发送方ID
     */
    private byte[] sourceId = new byte[32];

    /**
     * 本端签名公钥
     */
    private PublicKey localSignPk;

    /**
     * 本端签名私钥
     */
    private PrivateKey localSignSk;

    /**
     * 本端加密公钥
     */
    private PublicKey localEncPk;

    /**
     * 本端加密私钥
     */
    private PrivateKey localEncSk;

    /**
     * 对端签名公钥
     */
    private PublicKey peerSignPk;

    /**
     * 对端加密公钥
     */
    private PublicKey peerEncPk;

    /**
     * 随机数A -16字节
     */
    private byte[] randA;

    /**
     * 随机数B - 16字节
     */
    private byte[] randB;

    /**
     * 会话密钥
     */
    private byte[] secretKey;

    /**
     * 算法标识
     */
    private int alg;

    private byte[] iv;

    public void initHandShake(HsmInfo hsmInfo) throws Exception {
        AbstractCertMaker abstractCertMaker = new SM2X509CertMakerImpl();

        this.localSignPk = abstractCertMaker.byteConvertPublickey(hsmInfo.getLocalSignPk());
        this.localSignSk = abstractCertMaker.byteConvertPrivatekey(hsmInfo.getLocalSignSk());
        this.localEncPk = abstractCertMaker.byteConvertPublickey(hsmInfo.getLocalEncPk());
        this.localEncSk = abstractCertMaker.byteConvertPrivatekey(hsmInfo.getLocalEncSk());

        this.peerSignPk = abstractCertMaker.byteConvertPublickey(hsmInfo.getPeerSignPk());
        this.peerEncPk = abstractCertMaker.byteConvertPublickey(hsmInfo.getPeerEncPk());

        this.destId = Arrays.copyOf(hsmInfo.getDestId(), 32);
        this.sourceId = Arrays.copyOf(hsmInfo.getSourceId(), 32);

    }

    public long getMsgId() {
        return atomicMsgId.getAndIncrement();
    }

    public void createSecretKey() {
        if (randA != null && randB != null) {
            this.secretKey = new byte[randA.length];
            for (int index = 0; index < this.secretKey.length; index++) {
                this.secretKey[index] = (byte) (randA[index] ^ randB[index]);
            }
        }
    }


}
