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
     * 工作节点ID，用来生成消息唯一ID
     */
    private long workerId;

    /**
     * socket数量
     * 默认为1
     */
    private int initialSize = 1;


}
