package com.shudun.dms.handshake;

import com.shudun.dms.Exception.VerifyMessageException;
import com.shudun.dms.constant.DmsConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ConnectionRequestMessage extends AbstractConnectionRequestMessage {

    private final byte opType = DmsConstants.MsgTypeEnum.CONNECT.getCode();
    // 需要回复、无加密、已签名
    private final byte secureModel = (byte) (DmsConstants.SecureModelEnum.SDM_SECMODE_RET.getCode() | DmsConstants.SecureModelEnum.SDM_SECMODE_SIGN.getCode());

    public ConnectionRequestMessage(HandShake handShake) {
        super(handShake);
    }

    @Override
    protected byte[] encodeMessageBody(byte[] data) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

            // 生成随机数A
            byte[] randomA = abstractMessageAlgorithm.generateRandom();
            handShake.setRandA(randomA);

            // 对随机数A加密
            byte[] encRandomA = abstractMessageAlgorithm.asymmetricEncryption(handShake.getPeerEncPk(), randomA);
            dataOutputStream.writeInt(encRandomA.length);
            dataOutputStream.write(encRandomA);

            // 对随机数A签名
            byte[] sign = abstractMessageAlgorithm.sign(handShake.getLocalSignSk(), randomA);
            dataOutputStream.writeInt(sign.length);
            dataOutputStream.write(sign);

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("随机数A生成异常", e.getMessage());
            throw new RuntimeException("ConnectionRequestMessage encodeMessageBody error ", e);
        }
    }

    @Override
    protected byte[] decodeMessageBody(byte[] data) throws IOException {

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data); DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            // 获取加密随机数A
            int cipherALength = dataInputStream.readInt();
            byte[] cipherA = new byte[cipherALength];
            dataInputStream.read(cipherA);

            // 获取签名随机数A
            int signALength = dataInputStream.readInt();
            byte[] signA = new byte[signALength];
            dataInputStream.read(signA);

            // 对随机数A解密
            byte[] randomA = abstractMessageAlgorithm.asymmetricDecrypt(handShake.getLocalEncSk(), cipherA);
            handShake.setRandA(randomA);

            // 对随机数A验签
            boolean verify = abstractMessageAlgorithm.verify(handShake.getPeerSignPk(), randomA, signA);
            if (!verify) {
                throw new VerifyMessageException("验证RandomA签名失败");
            }
        }
        return null;
    }

    @Override
    protected byte[] createMessageTrailer(byte[] data) {
        return abstractMessageAlgorithm.sign(handShake.getLocalSignSk(), data);
    }

    @Override
    public void verifyMessageTrailer(byte[] src, byte[] trailer) {
        boolean verify = abstractMessageAlgorithm.verify(handShake.getPeerSignPk(), src, trailer);
        if (!verify) {
            throw new RuntimeException("verifyMessage error");
        }
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
