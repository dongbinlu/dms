package com.shudun.dms.rpc;

import com.shudun.dms.channel.IChannel;
import com.shudun.dms.global.GlobalVariable;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ConntionManager {

    private static final Map<String, IChannel> CHANNEL_POOL = new ConcurrentHashMap();

    public List<String> getKeys() {
        return CHANNEL_POOL.keySet().stream().collect(Collectors.toList());
    }

    public void addChannel(String key, IChannel channel) {
        CHANNEL_POOL.put(key, channel);
    }

    public void removChannel(String key) {
        CHANNEL_POOL.remove(key);
    }

    public IChannel getIChannelByChannel(Channel channel) {
        Attribute<IChannel> attr = channel.attr(GlobalVariable.CHANNEL_KEY);
        return attr.get();
    }

    public IChannel getIChannelByKey(String key) {
        return CHANNEL_POOL.get(key);
    }

    public Channel getChannelByKey(String key) {
        IChannel iChannel = CHANNEL_POOL.get(key);
        return iChannel.getChannel();
    }

}
