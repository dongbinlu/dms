package com.shudun.dms.frame;

import com.shudun.dms.channel.IChannel;
import com.shudun.dms.channel.JavaChannel;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.handshake.ConnectionRequestMessage;
import com.shudun.dms.handshake.ConnectionResponseMessage;
import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.handshake.HsmInfo;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 发起密钥协商请求
 */
@Slf4j
public class KeyAgreementHandler extends SimpleChannelInboundHandler<Message> {

    private HandShake handShake;

    private HsmInfo hsmInfo;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

    public KeyAgreementHandler(HsmInfo hsmInfo) {
        this.hsmInfo = hsmInfo;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("[客户端]" + channel.localAddress() + " 发起密钥协商 " + LocalDateTime.now().format(dtf));

        Attribute<IChannel> attribute = channel.attr(GlobalVariable.CHANNEL_KEY);
        IChannel iChannel = attribute.get();
        if (iChannel == null) {
            iChannel = new JavaChannel(channel);
            attribute.set(iChannel);
        }
        handShake = ctx.channel().attr(GlobalVariable.CHANNEL_KEY).get().getHandShake();
        // 组装密钥协商基础数据
        handShake.initHandShake(hsmInfo);
        ConnectionRequestMessage connectionRequestMessage = new ConnectionRequestMessage(handShake);
        // 组装安全通道建立请求消息包
        ctx.channel().writeAndFlush(connectionRequestMessage.encode(null));

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        HeadInfo headInfo = msg.getHeadInfo();
        byte opType = headInfo.getOpType();
        if (opType == DmsConstants.MsgTypeEnum.RESPONSE.getCode()) {
            ConnectionResponseMessage connectionResponseMessage = new ConnectionResponseMessage(handShake);
            connectionResponseMessage.decode(msg);
            log.info("[客户端]" + ctx.channel().localAddress() + " 密钥协商成功 " + LocalDateTime.now().format(dtf));

        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
