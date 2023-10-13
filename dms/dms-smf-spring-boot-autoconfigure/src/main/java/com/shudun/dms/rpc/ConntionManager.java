package com.shudun.dms.rpc;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConntionManager {

    private static final Map<String, Channel> CHANNEL_POOL = new ConcurrentHashMap();

    public List<String> getKeys() {
        return CHANNEL_POOL.keySet().stream().collect(Collectors.toList());
    }

    public void addChannel(String key, Channel channel) {
        CHANNEL_POOL.put(key, channel);
    }

    public void removChannel(String key) {
        CHANNEL_POOL.remove(key);
    }


    public Channel getChannelByKey(String key) {
        return CHANNEL_POOL.get(key);
    }

}
