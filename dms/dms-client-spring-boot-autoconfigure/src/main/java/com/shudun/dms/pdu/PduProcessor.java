package com.shudun.dms.pdu;


import com.shudun.dms.channel.IChannel;
import com.shudun.dms.message.Message;


/**
 * 消息分解器
 */
public interface PduProcessor {
    /***
     * 路由PUD解析器
     * @param message:
     * @return: boolean
     **/
    boolean validate(Message message);

    /***
     * PUD处理
     * @param message:
     * @param iChannel:
     * @return: void
     **/
    void handler(Message message, IChannel iChannel) throws Exception;


}
