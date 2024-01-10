package com.shudun.dms.channel;

import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.handshake.HandShake;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.MessageFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class JavaChannel implements IChannel {

    private Channel channel;

    private HandShake handShake;

    // TODO 设计有问题，这样会导致一个socket创建一个线程池。
    // 写一个工具类处理
    protected final ScheduledExecutorService timerExecutor = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("timeoutChecker", true));

    protected final ConcurrentHashMap<Long, MessageFuture> futures = new ConcurrentHashMap<>();

    private static final int TIMEOUT_CHECK_INTERNAL = 3000;

    public JavaChannel(Channel channel) {
        this.channel = channel;
        this.handShake = new HandShake();
        this.init();
    }

    private void init() {
        timerExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Long> timeoutMessageFutures = new ArrayList<>(futures.size());

                    futures.forEach((key, value) -> {
                        if (value.isTimeout()) {
                            timeoutMessageFutures.add(key);
                        }
                    });

                    for (Long key : timeoutMessageFutures) {
                        futures.remove(key);
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }
        }, TIMEOUT_CHECK_INTERNAL, TIMEOUT_CHECK_INTERNAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void oneway(byte[] data) {
        this.oneway(data, 0);
    }

    @Override
    public void oneway(byte[] data, long msgId) {
        this.oneway("", "", data, msgId);
    }

    @Override
    public void oneway(String destId, String sourceId, byte[] data, long msgId) {
        this.oneway(destId, sourceId, data, msgId, DmsConstants.SecureModelEnum.SDM_SECMODE_ENC.getCode(), DmsConstants.SecureModelEnum.SDM_SECMODE_SIGN.getCode());
    }

    @Override
    public void oneway(String destId, String sourceId, byte[] data, long msgId, byte enc, byte sign) {
        this.oneway(destId, sourceId, data, msgId, DmsConstants.MsgTypeEnum.DATA.getCode(), enc, sign);
    }

    @Override
    public void oneway(String destId, String sourceId, byte[] data, long msgId, byte opType, byte enc, byte sign) {
        if (this.channel == null || !this.channel.isActive()) {
            throw new RuntimeException("和服务器还未建立起有效连接!请稍后再试!");
        }
        final Message msg = new Message();

        // 消息头
        HeadInfo headInfo = new HeadInfo();
        headInfo.setVersion(DmsConstants.MSG_VERSION);
        // 安全模式
        headInfo.setSecureModel((byte) (DmsConstants.SecureModelEnum.SDM_SECMODE_NORET.getCode() | enc | sign));
        headInfo.setRetain(headInfo.getRetain());

        Attribute<Long> attribute = channel.attr(GlobalVariable.MSG_KEY);

        msgId = msgId == 0 ? attribute.get() : msgId;
        headInfo.setMsgId(msgId);
        headInfo.setPduLength(data.length);
        // 目的方ID
        headInfo.setDestId(Arrays.copyOf(StringUtils.isBlank(destId) ? handShake.getDestId() : destId.getBytes(), 32));
        // 源ID,本地ID
        headInfo.setSourceId(Arrays.copyOf(StringUtils.isBlank(sourceId) ? handShake.getSourceId() : sourceId.getBytes(), 32));
        headInfo.setOpType(opType);
        msg.setHeadInfo(headInfo);

        //消息PDU
        msg.setPdu(data);

        ChannelFuture future = this.channel.writeAndFlush(msg);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("数据发送成功,msgId:{}", headInfo.getMsgId());
                }
            }
        });
    }

    @Override
    public Message sync(byte[] data, long timeout, TimeUnit unit) throws Exception {
        return this.sync(data, 0, timeout, unit);
    }


    @Override
    public Message sync(byte[] data, long msgId, long timeout, TimeUnit unit) throws Exception {
        return this.sync("", "", data, msgId, timeout, unit);
    }


    @Override
    public Message sync(String destId, String sourceId, byte[] data, long msgId, long timeout, TimeUnit unit) throws Exception {
        return this.sync(destId, sourceId, data, msgId, timeout, unit, DmsConstants.SecureModelEnum.SDM_SECMODE_ENC.getCode(), DmsConstants.SecureModelEnum.SDM_SECMODE_SIGN.getCode());
    }

    @Override
    public Message sync(String destId, String sourceId, byte[] data, long msgId, long timeout, TimeUnit unit, byte enc, byte sign) throws Exception {
        if (this.channel == null || !this.channel.isActive()) {
            throw new RuntimeException("和服务器还未建立起有效连接!请稍后再试!");
        }
        final Message msg = new Message();

        // 消息头
        HeadInfo headInfo = new HeadInfo();
        headInfo.setVersion(DmsConstants.MSG_VERSION);
        // 安全模式需回复，要加密和签名
        headInfo.setSecureModel((byte) (DmsConstants.SecureModelEnum.SDM_SECMODE_RET.getCode() | enc | sign));
        headInfo.setRetain(headInfo.getRetain());

        Attribute<Long> attribute = channel.attr(GlobalVariable.MSG_KEY);

        msgId = msgId == 0 ? attribute.get() : msgId;
        headInfo.setPduLength(data.length);
        // 目的方ID
        headInfo.setDestId(Arrays.copyOf(StringUtils.isBlank(destId) ? handShake.getDestId() : destId.getBytes(), 32));
        // 源ID,本地ID
        headInfo.setSourceId(Arrays.copyOf(StringUtils.isBlank(sourceId) ? handShake.getSourceId() : sourceId.getBytes(), 32));
        headInfo.setOpType(DmsConstants.MsgTypeEnum.DATA.getCode());
        msg.setHeadInfo(headInfo);

        //消息PDU
        msg.setPdu(data);

        final MessageFuture messageFuture = new MessageFuture();
        messageFuture.setMessage(msg);
        messageFuture.setTimeout(unit.toMillis(timeout));

        futures.put(msgId, messageFuture);

        ChannelFuture future = this.channel.writeAndFlush(msg);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("数据发送成功,msgId:{}", headInfo.getMsgId());
                }
            }
        });
        if (timeout < 0) {
            return null;
        }

        try {
            return messageFuture.get(timeout, unit);
        } catch (Exception e) {
            log.error("wait response error,deviceId:{},msgId:{},errorMsg:{}", new String(handShake.getDestId()), msgId, e.getMessage());
            if (e instanceof TimeoutException) {
                throw e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public Message sync(byte[] data) throws Exception {
        return this.sync(data, 0);
    }

    @Override
    public Message sync(byte[] data, long msgId) throws Exception {
        return this.sync("", "", data, msgId);
    }

    @Override
    public Message sync(String destId, String sourceId, byte[] data, long msgId) throws Exception {
        return this.sync(destId, sourceId, data, msgId, 10, TimeUnit.SECONDS);
    }


    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public HandShake getHandShake() {
        return this.handShake;
    }

    public ConcurrentHashMap<Long, MessageFuture> getFutures() {
        return futures;
    }
}
