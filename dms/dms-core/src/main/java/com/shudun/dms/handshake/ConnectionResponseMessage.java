package com.shudun.dms.handshake;

import java.io.*;
import java.util.Arrays;

public class ConnectionResponseMessage extends AbstractConnectionResponseMessage {

    private final byte opType = (byte) 0xA2;

    private final byte secureModel = (byte) 0B00000001; // 无需回复、无加密、已签名


    public ConnectionResponseMessage(HandShake handShake) {
        super(handShake);
    }

    @Override
    protected byte[] decodeMessageBody(byte[] data) {

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data); DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            int cipherABLength = dataInputStream.readInt();
            byte[] cipherAB = new byte[cipherABLength];
            dataInputStream.readFully(cipherAB);

            byte[] randomAB = this.abstractMessageAlgorithm.asymmetricDecrypt(handShake.getLocalEncSk(), cipherAB);
            byte[] randomB = Arrays.copyOfRange(randomAB, 16, 32);
            handShake.setRandB(randomB);

            int signatureBLength = dataInputStream.readInt();
            byte[] signatureB = new byte[signatureBLength];
            dataInputStream.readFully(signatureB);
            boolean verify = this.abstractMessageAlgorithm.verify(handShake.getPeerSignPk(), randomB, signatureB);
            if (!verify) {
                throw new RuntimeException("验证RandomB签名失败");
            }
            handShake.createSecretKey();
            handShake.setIv(this.abstractMessageAlgorithm.generateIv());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    @Override
    protected void verifyMessageTrailer(byte[] src, byte[] trailer) {
        boolean verify = abstractMessageAlgorithm.verify(handShake.getPeerSignPk(), src, trailer);
        if (!verify) {
            throw new RuntimeException("verifyMessage error");
        }
    }

    @Override
    protected byte[] encodeMessageBody(byte[] data) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

            // 生成随机数B
            byte[] randomB = abstractMessageAlgorithm.generateRandom();
            handShake.setRandB(randomB);

            byte[] randomAB = new byte[32];
            System.arraycopy(handShake.getRandA(), 0, randomAB, 0, 16);
            System.arraycopy(handShake.getRandB(), 0, randomAB, 16, 16);

            //对随机数AB进行加密
            byte[] encRandomAB = abstractMessageAlgorithm.asymmetricEncryption(handShake.getPeerEncPk(), randomAB);
            dataOutputStream.writeInt(encRandomAB.length);
            dataOutputStream.write(encRandomAB);

            // 对随机数B进行签名
            byte[] sign = abstractMessageAlgorithm.sign(handShake.getLocalSignSk(), randomB);
            dataOutputStream.writeInt(sign.length);
            dataOutputStream.write(sign);

            // 密钥协商
            handShake.createSecretKey();
            //设置初始化IV
            handShake.setIv(abstractMessageAlgorithm.generateIv());

            return outputStream.toByteArray();
        }
    }

    @Override
    protected byte[] createMessageTrailer(byte[] data) {
        return abstractMessageAlgorithm.sign(handShake.getLocalSignSk(), data);
    }

    @Override
    protected byte secureModel() {
        return this.secureModel;
    }

    @Override
    protected byte opType() {
        return this.opType;
    }
}
