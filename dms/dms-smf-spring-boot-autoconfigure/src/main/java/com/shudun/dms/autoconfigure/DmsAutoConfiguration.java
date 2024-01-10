package com.shudun.dms.autoconfigure;


import com.shudun.dms.properties.DmsProperties;
import com.shudun.dms.rpc.DmsRpcTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 当配置开关开启时生效，未开启时不生效
 * 从容器中获取DmsRpcTemplate,用来执行远程调用
 */
@Configuration
@EnableConfigurationProperties({DmsProperties.class})
/**
 * 只有在dms.rpc.flag配置为true时生效，默认不生效
 */
@ConditionalOnProperty(prefix = "dms.rpc", name = "flag", havingValue = "true", matchIfMissing = false)
public class DmsAutoConfiguration implements InitializingBean {

    private final DmsProperties dmsProperties;

    public DmsAutoConfiguration(DmsProperties dmsProperties) {
        this.dmsProperties = dmsProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.checkConfig();
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public DmsRpcTemplate dmsRpcTemplate() {
        return new DmsRpcTemplate(dmsProperties);
    }

    private void checkConfig() {
        if (StringUtils.isBlank(dmsProperties.getIp())) {
            throw new IllegalArgumentException("dms rpc ip is not allowed to be empty");
        }
        if (dmsProperties.getInitialSize() <= 0) {
            throw new IllegalArgumentException("dms rpc initialSize is not allowed to be less than 0");
        }
    }

}
