package com.shudun.dms.frame;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HighAndLowWaterLevelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isWritable()) {
            ctx.channel().config().setAutoRead(true);
        } else {
            ctx.channel().config().setAutoRead(false);
        }
        super.channelWritabilityChanged(ctx);
    }
}