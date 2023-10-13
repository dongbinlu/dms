package com.shudun.dms.frame;

import com.shudun.dms.codec.MessageDecodeHandler;
import com.shudun.dms.codec.MessageEncoderHandler;
import com.shudun.dms.handler.MessageDecryptHandler;
import com.shudun.dms.handler.MessageEncryptHandler;
import com.shudun.dms.handler.MessageSignatureHandler;
import com.shudun.dms.handler.MessageVerifyHandler;
import com.shudun.dms.rpc.ConntionManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ServerInit extends ChannelInitializer<SocketChannel> {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MessageHandler messageHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ch.pipeline().addLast(new MessageEncoderHandler());
        ch.pipeline().addLast(new MessageSignatureHandler());
        ch.pipeline().addLast(new MessageEncryptHandler());

        ch.pipeline().addLast(new MessageDecodeHandler());
        ch.pipeline().addLast(new KeyAgreementHandler(applicationContext.getBean(ConntionManager.class)));
        ch.pipeline().addLast(new MessageDecryptHandler());
        ch.pipeline().addLast(new MessageVerifyHandler());
        ch.pipeline().addLast(messageHandler);

    }

}
