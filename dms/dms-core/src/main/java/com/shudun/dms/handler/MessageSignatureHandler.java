package com.shudun.dms.handler;

import com.shudun.dms.channel.IChannel;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.TailInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.Arrays;

/**
 * 处理签名的handler
 */
public class MessageSignatureHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {
        Message msg = (Message) obj;
        HeadInfo headInfo = msg.getHeadInfo();
        byte secureModel = headInfo.getSecureModel();
        boolean signed = (secureModel & (byte) 0B00000001) > 0;
        if (signed && headInfo.getOpType() == (byte) 0xA3) {
            IChannel iChannel = ctx.channel().attr(GlobalVariable.CHANNEL_KEY).get();
            HandShake handShake = iChannel.getHandShake();

            byte[] toProtected = Message.builderMessageTrailer(headInfo, msg.getPdu());

            byte[] hmac = handShake.getAbstractMessageAlgorithm().hmac(handShake.getSecretKey(), toProtected);
            byte[] hmacs = Arrays.copyOfRange(hmac, 0, 16);

            TailInfo tailInfo = new TailInfo();
            tailInfo.setLength(hmacs.length);
            tailInfo.setMsg(hmacs);
            msg.setTailInfo(tailInfo);
        }
        ctx.writeAndFlush(msg, promise);

    }

}
