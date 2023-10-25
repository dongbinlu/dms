package com.shudun.dms.handler;

import com.shudun.dms.channel.IChannel;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 处理解密的handler
 */
public class MessageDecryptHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        HeadInfo headInfo = msg.getHeadInfo();
        byte secureModel = headInfo.getSecureModel();
        if ((secureModel & DmsConstants.SecureModelEnum.SDM_SECMODE_ENC.getCode()) != 0 && headInfo.getOpType() == DmsConstants.MsgTypeEnum.DATA.getCode()) {
            IChannel iChannel = ctx.channel().attr(GlobalVariable.CHANNEL_KEY).get();
            HandShake handShake = iChannel.getHandShake();
            byte[] data = handShake.getAbstractMessageAlgorithm().symmetricDecrypt(handShake.getSecretKey(), msg.getPdu(), handShake.getIv());
            msg.setPdu(data);
        }
        ctx.fireChannelRead(msg);
    }

}
