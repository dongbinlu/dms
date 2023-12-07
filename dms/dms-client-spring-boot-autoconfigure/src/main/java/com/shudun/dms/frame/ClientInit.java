package com.shudun.dms.frame;

import com.shudun.dms.codec.MessageDecodeHandler;
import com.shudun.dms.codec.MessageEncoderHandler;
import com.shudun.dms.handler.MessageDecryptHandler;
import com.shudun.dms.handler.MessageEncryptHandler;
import com.shudun.dms.handler.MessageSignatureHandler;
import com.shudun.dms.handler.MessageVerifyHandler;
import com.shudun.dms.handshake.HsmInfo;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ClientInit extends ChannelInitializer<SocketChannel> {

    private MessageHandler messageHandler;

    private HsmInfo hsmInfo;

    public ClientInit(MessageHandler messageHandler , HsmInfo hsmInfo) {
        this.messageHandler = messageHandler;
        this.hsmInfo = hsmInfo;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ch.pipeline().addLast(new MessageEncoderHandler());
        ch.pipeline().addLast(new MessageSignatureHandler());
        ch.pipeline().addLast(new MessageEncryptHandler());

        ch.pipeline().addLast(new MessageDecodeHandler());
        ch.pipeline().addLast(new KeyAgreementHandler(hsmInfo));
        ch.pipeline().addLast(new MessageDecryptHandler());
        ch.pipeline().addLast(new MessageVerifyHandler());
        ch.pipeline().addLast(messageHandler);

    }
}

