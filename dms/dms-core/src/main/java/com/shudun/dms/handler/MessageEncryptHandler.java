package com.shudun.dms.handler;

import com.shudun.dms.channel.IChannel;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * 处理加密的handler
 */
public class MessageEncryptHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) throws Exception {

        Message msg = (Message) obj;
        HeadInfo headInfo = msg.getHeadInfo();
        byte secureModel = headInfo.getSecureModel();
        if (secureModel == DmsConstants.SecureModelEnum.SDM_SECMODE_ENC.getCode() && headInfo.getOpType() == DmsConstants.MsgTypeEnum.DATA.getCode()) {
            IChannel iChannel = ctx.channel().attr(GlobalVariable.CHANNEL_KEY).get();
            HandShake handShake = iChannel.getHandShake();
            byte[] data = handShake.getAbstractMessageAlgorithm().symmetricEncryption(handShake.getSecretKey(), msg.getPdu(), handShake.getIv());
            headInfo.setPduLength(data.length);
            msg.setPdu(data);
        }
        ctx.writeAndFlush(msg, promise);
    }
}
