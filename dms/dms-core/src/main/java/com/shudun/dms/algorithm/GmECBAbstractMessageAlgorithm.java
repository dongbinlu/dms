package com.shudun.dms.algorithm;

import cn.com.shudun.cert.gmhelper.SM2Util;
import cn.com.shudun.cert.gmhelper.SM3Util;
import cn.com.shudun.cert.gmhelper.SM4Util;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.security.PrivateKey;
import java.security.PublicKey;

public class GmECBAbstractMessageAlgorithm extends AbstractMessageAlgorithm {

    @Override
    public byte[] generateRandom() {
        return generateRandom(16);
    }


    @Override
    public byte[] symmetricEncryption(byte[] secretKey, byte[] data, byte[] iv) {
        byte[] padding = padding(data);
        try {
            return SM4Util.encrypt_ECB_NoPadding(secretKey, padding);
        } catch (Exception ex) {
            throw new RuntimeException("GmECBAbstractMessageAlgorithm symmetricEncryption error ", ex);
        }
    }

    @Override
    public byte[] symmetricDecrypt(byte[] secretKey, byte[] data, byte[] iv) {
        try {
            byte[] bytes = SM4Util.decrypt_ECB_NoPadding(secretKey, data);
            return unpadding(bytes);
        } catch (Exception ex) {
            throw new RuntimeException("GmECBAbstractMessageAlgorithmsymmetricEncryption error ", ex);
        }
    }

    //公钥非对称加密
    @Override
    public byte[] asymmetricEncryption(PublicKey publicKey, byte[] data) {
        byte[] encrypt = new byte[0];
        try {
            encrypt = SM2Util.encryptAsn1((BCECPublicKey) publicKey, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypt;
    }

    @Override
    public byte[] asymmetricDecrypt(PrivateKey privateKey, byte[] data) {
        byte[] decrypt = null;
        try {
            decrypt = SM2Util.decryptAsn1((BCECPrivateKey) privateKey, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypt;
    }

    //私钥签名
    @Override
    public byte[] sign(PrivateKey privateKey, byte[] data) {
        byte[] sign = new byte[0];
        try {
            sign = SM2Util.sign((BCECPrivateKey) privateKey, data);
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        return sign;
    }

    //公钥验签
    @Override
    public boolean verify(PublicKey publicKey, byte[] src, byte[] sign) {
        boolean verify = SM2Util.verify((BCECPublicKey) publicKey, src, sign);
        return verify;
    }

    @Override
    public byte[] hmac(byte[] secretKey, byte[] data) {
        return SM3Util.hmac(secretKey, data);
    }

    @Override
    public byte[] generateIv() {
        return new byte[16];
    }


}
