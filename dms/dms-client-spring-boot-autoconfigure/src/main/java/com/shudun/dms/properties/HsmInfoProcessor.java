package com.shudun.dms.properties;

public interface HsmInfoProcessor {


    /**
     * 服务端IP
     *
     * @return
     */
    String getIp();

    /**
     * 服务端端口，默认10197
     *
     * @return
     */
    int getPort();

    /**
     * 目的方ID
     */
    String getDestId();

    /**
     * 发送方ID
     */
    String getSourceId();

    /**
     * 本端签名公钥(Base64字符串)
     */
    String getLocalSignPk();

    /**
     * 本端签名私钥(Base64字符串)
     */
    String getLocalSignSk();

    /**
     * 本端加密公钥(Base64字符串)
     */
    String getLocalEncPk();

    /**
     * 本端加密私钥(Base64字符串)
     */
    String getLocalEncSk();

    /**
     * 对端签名公钥(Base64字符串)
     */
    String getPeerSignPk();

    /**
     * 对端加密公钥(Base64字符串)
     */
    String getPeerEncPk();

}
