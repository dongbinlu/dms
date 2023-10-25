package com.shudun.dms.pdu;


import com.shudun.dms.channel.IChannel;
import com.shudun.dms.message.Message;


/**
 * 消息分解器
 */
public interface PduProcessor {

    boolean validate(Message message);

    void handler(Message message, IChannel iChannel) throws Exception;

}
