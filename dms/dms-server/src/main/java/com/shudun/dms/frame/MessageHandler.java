package com.shudun.dms.frame;

import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.rpc.RpcRemoting;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 处理0xA3消息的handler
 */
@Service
@ChannelHandler.Sharable
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {

    @Autowired
    private RpcRemoting rpcRemoting;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        HeadInfo headInfo = msg.getHeadInfo();
        byte opType = headInfo.getOpType();
        if (opType == DmsConstants.MsgTypeEnum.DATA.getCode()) {

            long msgId = msg.getHeadInfo().getMsgId();

            rpcRemoting.setMessage(msgId, msg);
            log.info("服务端收到数据:{},msgId:{} ", new String(msg.getPdu()), msgId);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

}
