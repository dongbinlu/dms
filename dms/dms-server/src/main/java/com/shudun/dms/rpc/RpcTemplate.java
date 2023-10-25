package com.shudun.dms.rpc;

import com.shudun.dms.channel.IChannel;
import com.shudun.dms.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RpcTemplate {

    @Autowired
    private ConntionManager conntionManager;

    public void oneway(String deviceId, byte[] data) {
        IChannel iChannel = conntionManager.getIChannelByKey(deviceId);
        if (iChannel.getChannel() == null || !iChannel.getChannel().isActive()) {
            throw new RuntimeException("设备" + deviceId + "和服务器还未建立起有效连接!请稍后再试!");
        }
        iChannel.oneway(data);
    }

    public Message sync(String deviceId, byte[] data, long timeout, TimeUnit unit) throws Exception {
        IChannel iChannel = conntionManager.getIChannelByKey(deviceId);
        if (iChannel.getChannel() == null || !iChannel.getChannel().isActive()) {
            throw new RuntimeException("设备" + deviceId + "和服务器还未建立起有效连接!请稍后再试!");
        }
        return iChannel.sync(data, timeout, unit);
    }

    public Message sync(String deviceId, byte[] data) throws Exception {
        IChannel iChannel = conntionManager.getIChannelByKey(deviceId);
        if (iChannel.getChannel() == null || !iChannel.getChannel().isActive()) {
            throw new RuntimeException("设备" + deviceId + "和服务器还未建立起有效连接!请稍后再试!");
        }
        return iChannel.sync(data);
    }

}
