package com.shudun.dms.global;

import com.shudun.dms.channel.IChannel;
import io.netty.util.AttributeKey;

public class GlobalVariable {

    public static final AttributeKey<IChannel> CHANNEL_KEY = AttributeKey.newInstance("CHANNEL_KEY");

    public static final AttributeKey<Long> MSG_KEY = AttributeKey.newInstance("MSG_KEY");

}
