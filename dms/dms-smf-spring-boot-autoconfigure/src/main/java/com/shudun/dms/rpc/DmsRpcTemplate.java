package com.shudun.dms.rpc;

import com.google.common.collect.Lists;
import com.shudun.dms.frame.NettyClient;
import com.shudun.dms.message.Message;
import com.shudun.dms.properties.DmsProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class DmsRpcTemplate implements DmsRpc {

    private List<NettyClient> nettyClients = Lists.newArrayList();

    private DmsProperties dmsProperties;

    private static volatile int index;

    public DmsRpcTemplate(DmsProperties dmsProperties) {
        this.dmsProperties = dmsProperties;
    }

    public void init() throws Exception {

        int initialSize = dmsProperties.getInitialSize();
        for (int i = 0; i < initialSize; i++) {
            NettyClient nettyClient = new NettyClient(dmsProperties);
            new Thread(() -> {
                nettyClient.init();
            }).start();
            nettyClients.add(nettyClient);
        }
    }

    public void destroy() {
        nettyClients.stream().forEach(nettyClient -> {
            try {
                nettyClient.destroy();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public void oneway(String deviceId, byte[] data) {
        NettyClient nettyClient = getNettyClient();
        nettyClient.oneway(deviceId, data);
    }

    @Override
    public Message sync(String deviceId, byte[] data) throws Exception {
        return sync(deviceId, data, dmsProperties.getTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public Message sync(String deviceId, byte[] data, long timeout, TimeUnit unit) throws Exception {
        NettyClient nettyClient = getNettyClient();
        return nettyClient.sync(deviceId, data, timeout, unit);
    }

    @Override
    public HashMap<Long, byte[]> get(String deviceId, long[] aids) throws Exception {
        return this.get(deviceId, aids, dmsProperties.getTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public HashMap<Long, byte[]> get(String deviceId, long[] aids, long timeout, TimeUnit unit) throws Exception {
        NettyClient nettyClient = getNettyClient();
        return nettyClient.get(deviceId, aids, timeout, unit);
    }

    private NettyClient getNettyClient() {
        NettyClient nettyClient = nettyClients.get(index);

        index++;

        // 如果索引越界，将其重置为0，以重新开始轮询
        if (index >= nettyClients.size()) {
            index = 0;
        }

        if (!nettyClient.getChannel().isActive()) {
            getNettyClient();
        }

        return nettyClient;
    }

}
