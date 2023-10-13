package com.shudun.dms.rpc;

import com.shudun.dms.channel.IChannel;
import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class RpcRemoting {

    @Autowired
    private ConntionManager conntionManager;

    protected final ScheduledExecutorService timerExecutor = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("timeoutChecker", true));

    protected final ConcurrentHashMap<Long, MessageFuture> futures = new ConcurrentHashMap<>();

    private static final int TIMEOUT_CHECK_INTERNAL = 3000;

    @PostConstruct
    public void init() {
        timerExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                List<MessageFuture> timeoutMessageFutures = new ArrayList<>(futures.size());
                for (MessageFuture future : futures.values()) {
                    if (future.isTimeout()) {
                        timeoutMessageFutures.add(future);
                    }
                }
                for (MessageFuture messageFuture : timeoutMessageFutures) {
                    futures.remove(messageFuture.getRequestMessage().getHeadInfo().getMsgId());
                    messageFuture.setRequestMessage(null);
                }
            }
        }, TIMEOUT_CHECK_INTERNAL, TIMEOUT_CHECK_INTERNAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Oneway 调用
     * 当前线程发起调用后，不关心调用结果，不做超时控制，只要请求已经发出，就完成本次调用。
     * 注意 Oneway 调用不保证成功，而且发起方无法知道调用结果。
     * 因此通常用于可以重试，或者定时通知类的场景，调用过程是有可能因为网络问题，机器故障等原因，导致请求失败。
     * 业务场景需要能接受这样的异常场景，才可以使用。
     *
     * @param deviceId
     */
    public void oneway(String deviceId) {

    }

    /**
     * Sync 同步调用
     * 当前线程发起调用后，需要在指定的超时时间内，等到响应结果，才能完成本次调用。
     * 如果超时时间内没有得到结果，那么会抛出超时异常。这种调用模式最常用。
     * 注意要根据对端的处理能力，合理设置超时时间。
     *
     * @param deviceId
     * @param data
     * @param timeout
     * @return
     */
    public Message sync(String deviceId, byte[] data, long timeout) throws Exception {

        IChannel iChannelByKey = conntionManager.getIChannelByKey(deviceId);
        if (iChannelByKey.getChannel() == null || !iChannelByKey.getChannel().isActive()) {
            throw new RuntimeException("设备" + deviceId + "和服务器还未建立起有效连接!请稍后再试!");
        }
        HandShake handShake = iChannelByKey.getHandShake();

        final Message msg = new Message();

        // 消息头
        HeadInfo headInfo = new HeadInfo();
        msg.setHeadInfo(headInfo);
        headInfo.setVersion((byte) 1);
        headInfo.setSecureModel((byte) 0B00000111);
        headInfo.setRetain(headInfo.getRetain());
        headInfo.setMsgId(handShake.getMsgId());
        headInfo.setPduLength(data.length);
        headInfo.setDestId(Arrays.copyOf(deviceId.getBytes(), 32));
        headInfo.setSourceId(Arrays.copyOf("11111111111111111111111111111111".getBytes(), 32));
        headInfo.setOpType((byte) 0xA3);

        // 消息PDU
        msg.setPdu(data);


        final MessageFuture messageFuture = new MessageFuture();
        messageFuture.setRequestMessage(msg);
        messageFuture.setTimeOut(timeout);

        futures.put(msg.getHeadInfo().getMsgId(), messageFuture);

        Channel channel = conntionManager.getChannelByKey(deviceId.trim());
        ChannelFuture future = channel.writeAndFlush(msg);
        log.info("服务端发送数据,msgId:{},等待响应...", headInfo.getMsgId());
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    futures.remove(msg.getHeadInfo().getMsgId());
                }
            }
        });

        if (timeout > 0) {
            try {
                return messageFuture.get(timeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("wait response error: " + deviceId, e.getMessage());
                if (e instanceof TimeoutException) {
                    throw (TimeoutException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        } else {
            return null;
        }
    }

    public void setMessage(long msgId, Message message) {
        MessageFuture messageFuture = futures.get(msgId);
        if (messageFuture != null) {
            messageFuture.setResultMessage(message);
        }
    }

}
