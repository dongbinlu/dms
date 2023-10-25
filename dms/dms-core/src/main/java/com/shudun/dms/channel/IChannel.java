package com.shudun.dms.channel;

import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.message.Message;
import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

public interface IChannel {

    /**
     * Oneway 调用
     * 当前线程发起调用后，不关心调用结果，不做超时控制，只要请求已经发出，就完成本次调用。
     * 注意 Oneway 调用不保证成功，而且发起方无法知道调用结果。
     * 因此通常用于可以重试，或者定时通知类的场景，调用过程是有可能因为网络问题，机器故障等原因，导致请求失败。
     * 业务场景需要能接受这样的异常场景，才可以使用。
     *
     * @param data PDU
     */
    void oneway(byte[] data);

    void oneway(byte[] data, long msgId);

    void oneway(String destId, String sourceId, byte[] data, long msgId);

    void oneway(String destId, String sourceId, byte[] data, long msgId, byte enc, byte sign);

    void oneway(String destId, String sourceId, byte[] data, long msgId, byte opType, byte enc, byte sign);


    /**
     * Sync 同步调用
     * 当前线程发起调用后，需要在指定的超时时间内，等到响应结果，才能完成本次调用。
     * 如果超时时间内没有得到结果，那么会抛出超时异常。这种调用模式最常用。
     * 注意要根据对端的处理能力，合理设置超时时间。
     *
     * @param data    PDU
     * @param timeout 超时时间
     * @param unit    超时时间单位
     * @return
     */
    Message sync(byte[] data, long timeout, TimeUnit unit) throws Exception;

    Message sync(byte[] data, long msgId, long timeout, TimeUnit unit) throws Exception;

    Message sync(String destId, String sourceId, byte[] data, long msgId, long timeout, TimeUnit unit) throws Exception;

    Message sync(String destId, String sourceId, byte[] data, long msgId, long timeout, TimeUnit unit, byte enc, byte sign) throws Exception;

    Message sync(byte[] data) throws Exception;

    Message sync(byte[] data, long msgId) throws Exception;

    Message sync(String destId, String sourceId, byte[] data, long msgId) throws Exception;


    Channel getChannel();

    HandShake getHandShake();

}
