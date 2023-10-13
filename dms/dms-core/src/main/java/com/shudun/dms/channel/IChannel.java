package com.shudun.dms.channel;

import com.shudun.dms.handshake.HandShake;
import io.netty.channel.Channel;

public interface IChannel {

    Channel getChannel();

    HandShake getHandShake();

}
