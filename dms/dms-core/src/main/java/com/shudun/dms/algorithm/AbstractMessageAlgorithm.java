package com.shudun.dms.algorithm;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractMessageAlgorithm {

    //生成随机数
    public abstract byte[] generateRandom();

    //对称加密
    public abstract byte[] symmetricEncryption(byte[] secretKey, byte[] data, byte[] iv);

    //对称解密
    public abstract byte[] symmetricDecrypt(byte[] secretKey, byte[] data, byte[] iv);

    //非对称加密
    public abstract byte[] asymmetricEncryption(PublicKey publicKey, byte[] data);

    //非对称解密
    public abstract byte[] asymmetricDecrypt(PrivateKey publicKey, byte[] data);

    //私钥签名
    public abstract byte[] sign(PrivateKey privateKey, byte[] data);

    //公钥验签
    public abstract boolean verify(PublicKey privateKey, byte[] src, byte[] sign);

    public abstract byte[] hmac(byte[] secretKey, byte[] data);

    public abstract byte[] generateIv();


    protected byte[] generateRandom(int length) {
        byte[] randomByte = new byte[length];
        ThreadLocalRandom.current().nextBytes(randomByte);
        return randomByte;
    }
    protected  byte[] padding(byte[] plain) {
        byte[] pads = {Byte.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int paddingNum = 16 - plain.length % 16;
        byte[] retByte = new byte[plain.length + paddingNum];
        System.arraycopy(plain, 0, retByte, 0, plain.length);
        System.arraycopy(pads, 0, retByte, plain.length, paddingNum);
        return retByte;
    }

    protected byte[] unpadding(byte[] plain) {
        if (plain.length % 16 != 0) {
            throw new RuntimeException("块长度不等于16的倍数");
        }
        int index = plain.length - 1;
        for (; plain.length - index < 16 && plain[index] == 0; index--) {
        }
        if (plain[index] != Byte.MIN_VALUE) {
            throw new RuntimeException("去除补丁失败");
        }
        return Arrays.copyOfRange(plain, 0, index);
    }

}
