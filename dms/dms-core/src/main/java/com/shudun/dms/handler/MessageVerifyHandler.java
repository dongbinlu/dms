package com.shudun.dms.handler;

import com.shudun.dms.Exception.VerifyMessageException;
import com.shudun.dms.channel.IChannel;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.TailInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.bouncycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * 处理解密验签的handler
 */
public class MessageVerifyHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        HeadInfo headInfo = msg.getHeadInfo();
        byte secureModel = headInfo.getSecureModel();
        if ((secureModel & DmsConstants.SecureModelEnum.SDM_SECMODE_ENC.getCode()) != 0 && headInfo.getOpType() == DmsConstants.MsgTypeEnum.DATA.getCode()) {
            IChannel iChannel = ctx.channel().attr(GlobalVariable.CHANNEL_KEY).get();
            HandShake handShake = iChannel.getHandShake();
            TailInfo tailInfo = msg.getTailInfo();
            byte[] toProtected = Arrays.copyOfRange(msg.getData(), 0, msg.getData().length - 4 - tailInfo.getMsg().length);

            byte[] src = Arrays.copyOfRange(handShake.getAbstractMessageAlgorithm().hmac(handShake.getSecretKey(), toProtected), 0, 16);
            if (!Arrays.equals(src, tailInfo.getMsg())) {
                throw new VerifyMessageException("verifyMessage error src:" + Hex.toHexString(src) + ", pdu:" + Hex.toHexString(tailInfo.getMsg()));
            }
        }
        ctx.fireChannelRead(msg);
    }

}
