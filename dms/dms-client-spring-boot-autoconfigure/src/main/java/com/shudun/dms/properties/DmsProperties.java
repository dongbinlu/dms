package com.shudun.dms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dms.rpc")
public class DmsProperties {

    private boolean flag;

    /**
     * dms服务端IP地址
     */
    private String ip;

    /**
     * dms服务端端口号
     * 默认10197
     */
    private int port = 10197;

    /**
     * rpc远程访问超时时间
     * 单位：秒
     * 默认3秒
     */
    private long timeout = 3L;

    /**
     * 目的方ID
     */
    private String destId;

    /**
     * 发送方ID
     */
    private String sourceId;

    /**
     * 本端签名公钥(Base64字符串)
     */
    private String localSignPk;

    /**
     * 本端签名私钥(Base64字符串)
     */
    private String localSignSk;

    /**
     * 本端加密公钥(Base64字符串)
     */
    private String localEncPk;

    /**
     * 本端加密私钥(Base64字符串)
     */
    private String localEncSk;

    /**
     * 对端签名公钥(Base64字符串)
     */
    private String peerSignPk;

    /**
     * 对端加密公钥(Base64字符串)
     */
    private String peerEncPk;


}
