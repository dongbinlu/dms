package com.shudun.dms.channel;

import com.shudun.dms.handshake.HandShake;
import io.netty.channel.Channel;

public class JavaChannel implements IChannel {

    private Channel channel;

    private HandShake handShake;

    public JavaChannel(Channel channel) {
        this.channel = channel;
        this.handShake = new HandShake();
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public HandShake getHandShake() {
        return this.handShake;
    }
}
