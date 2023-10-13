package com.shudun.dms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("dms.netty")
public class NettyProperties {

    private String ip;

    private int port;

}
