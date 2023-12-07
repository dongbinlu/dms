package com.shudun.dms.autoconfigure;


import com.shudun.dms.frame.MessageHandler;
import com.shudun.dms.handshake.HsmInfo;
import com.shudun.dms.pdu.PduProcessorFactory;
import com.shudun.dms.properties.DmsProperties;
import com.shudun.dms.properties.HsmInfoFactoryBean;
import com.shudun.dms.properties.HsmInfoProcessor;
import com.shudun.dms.rpc.DmsRpcTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

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
@Slf4j
public class DmsAutoConfiguration implements InitializingBean {

    private final DmsProperties dmsProperties;

    public DmsAutoConfiguration(DmsProperties dmsProperties) {
        this.dmsProperties = dmsProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public DmsRpcTemplate dmsRpcTemplate(MessageHandler messageHandler, HsmInfo hsmInfo) {
        return new DmsRpcTemplate(dmsProperties, messageHandler, hsmInfo);
    }

    @Bean
    public MessageHandler messageHandler(PduProcessorFactory pduProcessorFactory) {
        return new MessageHandler(pduProcessorFactory);
    }

    @Bean
    public PduProcessorFactory pduProcessorFactory() {
        return new PduProcessorFactory();
    }

    @Bean
    public HsmInfo hsmInfo(ApplicationContext applicationContext) throws Exception {
        HsmInfo hsmInfo;
        if (hasImplHsmInfoProcessor(applicationContext)) {
            hsmInfo = new HsmInfoFactoryBean(applicationContext.getBean(HsmInfoProcessor.class)).getObject();
        } else {
            hsmInfo = defaultHsmInfo();
        }
        checkConfig(hsmInfo);
        return hsmInfo;
    }

    public boolean hasImplHsmInfoProcessor(ApplicationContext applicationContext) {
        Map<String, HsmInfoProcessor> map = applicationContext.getBeansOfType(HsmInfoProcessor.class);
        return !map.isEmpty();
    }

    public HsmInfo defaultHsmInfo() {

        HsmInfo hsmInfo = null;
        try {
            hsmInfo = HsmInfo.builder()
                    .localSignPk(HsmInfo.toPublicKey(dmsProperties.getLocalSignPk()))
                    .localSignSk(Base64.getDecoder().decode(dmsProperties.getLocalSignSk()))
                    .localEncPk(HsmInfo.toPublicKey(dmsProperties.getLocalEncPk()))
                    .localEncSk(Base64.getDecoder().decode(dmsProperties.getLocalEncSk()))
                    .sourceId(Arrays.copyOf(dmsProperties.getSourceId().getBytes(), 32))
                    .peerSignPk(HsmInfo.toPublicKey(dmsProperties.getPeerSignPk()))
                    .peerEncPk(HsmInfo.toPublicKey(dmsProperties.getPeerEncPk()))
                    .destId(Arrays.copyOf(dmsProperties.getDestId().getBytes(), 32))
                    .ip(dmsProperties.getIp())
                    .port(dmsProperties.getPort())
                    .build();
        } catch (Exception e) {
            // do nothing
            log.error("builder hsm info error", e);
        }

        return hsmInfo;
    }

    private void checkConfig(HsmInfo hsmInfo) {
        if (ArrayUtils.isEmpty(hsmInfo.getLocalSignPk())) {
            log.warn("dms rpc localSignPk is not allowed to be empty");
        }
        if (ArrayUtils.isEmpty(hsmInfo.getLocalSignSk())) {
            log.warn("dms rpc localSignSk is not allowed to be empty");
        }
        if (ArrayUtils.isEmpty(hsmInfo.getLocalEncPk())) {
            log.warn("dms rpc localEncPk is not allowed to be empty");
        }
        if (ArrayUtils.isEmpty(hsmInfo.getLocalEncSk())) {
            log.warn("dms rpc localEncSk is not allowed to be empty");
        }
        if (ArrayUtils.isEmpty(hsmInfo.getSourceId())) {
            log.warn("dms rpc sourceId is not allowed to be empty");
        }
        if (ArrayUtils.isEmpty(hsmInfo.getPeerSignPk())) {
            log.warn("dms rpc peerSignPk is not allowed to be empty");
        }
        if (ArrayUtils.isEmpty(hsmInfo.getPeerEncPk())) {
            log.warn("dms rpc peerEncPk is not allowed to be empty");
        }
        if (ArrayUtils.isEmpty(hsmInfo.getDestId())) {
            log.warn("dms rpc destId is not allowed to be empty");
        }
        if (StringUtils.isBlank(hsmInfo.getIp())) {
            log.warn("dms rpc ip is not allowed to be empty");
        }
    }

}
