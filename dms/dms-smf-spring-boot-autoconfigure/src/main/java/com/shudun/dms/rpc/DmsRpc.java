package com.shudun.dms.rpc;

import com.shudun.dms.message.Message;

import java.util.concurrent.TimeUnit;

public interface DmsRpc {

    /**
     * Oneway 调用
     * 当前线程发起调用后，不关心调用结果，不做超时控制，只要请求已经发出，就完成本次调用。
     * 注意 Oneway 调用不保证成功，而且发起方无法知道调用结果。
     * 因此通常用于可以重试，或者定时通知类的场景，调用过程是有可能因为网络问题，机器故障等原因，导致请求失败。
     * 业务场景需要能接受这样的异常场景，才可以使用。
     *
     * @param data     PDU
     */
    void oneway(String deviceId, byte[] data);

    /**
     * Sync 同步调用
     * 当前线程发起调用后，需要在指定的超时时间内，等到响应结果，才能完成本次调用。
     * 如果超时时间内没有得到结果，那么会抛出超时异常。这种调用模式最常用。
     * 注意要根据对端的处理能力，合理设置超时时间。
     *
     * @param deviceId 设备ID
     * @param data     PDU
     * @param timeout  超时时间
     * @param unit     超时时间单位
     * @return
     */
    Message sync(String deviceId, byte[] data, long timeout, TimeUnit unit) throws Exception;

    /**
     * Sync 同步调用
     * 当前线程发起调用后，在默认的超时时间内，等到响应结果，才能完成本次调用。
     * 如果超时时间内没有得到结果，那么会抛出超时异常。这种调用模式最常用。
     * 注意要根据对端的处理能力，合理设置默认超时时间。
     *
     * @param deviceId 设备ID
     * @param data     PDU
     * @return
     */
    Message sync(String deviceId, byte[] data) throws Exception;


}
