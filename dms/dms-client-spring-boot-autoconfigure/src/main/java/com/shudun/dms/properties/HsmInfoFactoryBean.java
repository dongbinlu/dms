package com.shudun.dms.properties;

import com.shudun.dms.handshake.HsmInfo;
import org.springframework.beans.factory.FactoryBean;

import java.util.Arrays;
import java.util.Base64;

public class HsmInfoFactoryBean implements FactoryBean<HsmInfo> {

    private HsmInfoProcessor hsmInfoProcessor;

    public HsmInfoFactoryBean(HsmInfoProcessor hsmInfoProcessor) {
        this.hsmInfoProcessor = hsmInfoProcessor;
    }

    @Override
    public HsmInfo getObject() throws Exception {

        HsmInfo hsmInfo = null;
        try {
            hsmInfo = HsmInfo.builder()
                    .localSignPk(HsmInfo.toPublicKey(hsmInfoProcessor.getLocalSignPk()))
                    .localSignSk(Base64.getDecoder().decode(hsmInfoProcessor.getLocalSignSk()))
                    .localEncPk(HsmInfo.toPublicKey(hsmInfoProcessor.getLocalEncPk()))
                    .localEncSk(Base64.getDecoder().decode(hsmInfoProcessor.getLocalEncSk()))
                    .sourceId(Arrays.copyOf(hsmInfoProcessor.getSourceId().getBytes(), 32))
                    .peerSignPk(HsmInfo.toPublicKey(hsmInfoProcessor.getPeerSignPk()))
                    .peerEncPk(HsmInfo.toPublicKey(hsmInfoProcessor.getPeerEncPk()))
                    .destId(Arrays.copyOf(hsmInfoProcessor.getDestId().getBytes(), 32))
                    .ip(hsmInfoProcessor.getIp())
                    .port(hsmInfoProcessor.getPort())
                    .build();
        } catch (Exception e) {
            hsmInfo = new HsmInfo();
        }
        return hsmInfo;
    }

    @Override
    public Class<?> getObjectType() {
        return HsmInfo.class;
    }

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }
}
