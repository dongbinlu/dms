package com.shudun.dms.rpc;

import com.shudun.dms.frame.MessageHandler;
import com.shudun.dms.frame.NettyClient;
import com.shudun.dms.handshake.HsmInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.properties.DmsProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class DmsRpcTemplate implements DmsRpc {

    private NettyClient nettyClient;

    private DmsProperties dmsProperties;

    private MessageHandler messageHandler;

    private HsmInfo hsmInfo;

    public DmsRpcTemplate(DmsProperties dmsProperties, MessageHandler messageHandler, HsmInfo hsmInfo) {
        this.dmsProperties = dmsProperties;
        this.messageHandler = messageHandler;
        this.hsmInfo = hsmInfo;
    }

    public void init() {
        nettyClient = new NettyClient(dmsProperties, messageHandler, hsmInfo);
        new Thread(() -> {
            nettyClient.init();
        }).start();
    }

    public void destroy() {
        try {
            nettyClient.destroy();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void oneway(byte[] data) {
        this.oneway("", data);
    }

    @Override
    public void oneway(String deviceId, byte[] data) {
        NettyClient nettyClient = getNettyClient();
        nettyClient.oneway(deviceId, data);
    }

    @Override
    public Message sync(byte[] data) throws Exception {
        return this.sync("", data);
    }

    @Override
    public Message sync(String deviceId, byte[] data) throws Exception {
        return this.sync(deviceId, data, dmsProperties.getTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public Message sync(byte[] data, long timeout, TimeUnit unit) throws Exception {
        return this.sync("", data, timeout, unit);
    }

    @Override
    public Message sync(String deviceId, byte[] data, long timeout, TimeUnit unit) throws Exception {
        NettyClient nettyClient = getNettyClient();
        return nettyClient.sync(deviceId, data, timeout, unit);
    }

    private NettyClient getNettyClient() {
        return this.nettyClient;
    }

}
