package com.shudun.dms.frame;

import com.google.common.collect.Maps;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.MessageFuture;
import com.shudun.dms.properties.DmsProperties;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Data
@Slf4j
public class NettyClient {

    private final ScheduledExecutorService timerExecutor = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("timeoutChecker", true));

    private static ExecutorService onewayExecutor = new ThreadPoolExecutor(2, 10, 0L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

    private final ConcurrentHashMap<Long, MessageFuture> futures = new ConcurrentHashMap<>();

    private static final int TIMEOUT_CHECK_INTERNAL = 3000;

    /**
     * 负责重连的线程池
     */
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private Channel channel;

    private byte[] localId;

    private EventLoopGroup group = new NioEventLoopGroup();

    /**
     * 是否用户主动关闭连接的标志值
     */
    private volatile boolean userClose = false;

    private DmsProperties dmsProperties;


    private AtomicLong atomicMsgId = new AtomicLong(0L);

    public NettyClient(DmsProperties dmsProperties) {
        this.dmsProperties = dmsProperties;
    }

    public void init() {
        try {
            connect(dmsProperties.getIp(), dmsProperties.getPort());
        } catch (Exception e) {
            log.error("连接异常", e);
        }

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

    public void destroy() throws Exception {
        this.close();
        log.info("netty client stop ......");
    }

    /**
     * 连接服务器
     *
     * @param host
     * @param port
     * @throws InterruptedException
     */
    public void connect(String host, int port) throws Exception {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_RCVBUF, 2 * 1024 * 1024)// 接收缓冲区为2M
                    .option(ChannelOption.SO_SNDBUF, 2 * 1024 * 1024)// 发送缓冲区为2M
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, false)
                    .option(ChannelOption.SO_LINGER, 1) //关闭时等待1s发送关闭
                    .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                    .handler(new InitHandler(futures));
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            channel = future.channel();
            log.info("客户端{} 已连接服务器 {}", channel.localAddress(), channel.remoteAddress());
            getLocalId();
            channel.closeFuture().sync();
        } finally {
            if (!userClose) {

                //非正常关闭或连接失败，有可能发生了网络问题，进行重连

                log.warn("重连中,请稍等......");
                executor.execute(() -> {
                    //给操作系统足够的时间，去释放相关的资源
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        connect(dmsProperties.getIp(), dmsProperties.getPort());
                    } catch (Exception e) {
                        log.error("客户端重连异常", e);
                    }
                });

            }
        }
    }


    public void close() throws Exception {
        userClose = true;
        this.channel.close();
        group.shutdownGracefully().sync();
    }

    public void oneway(String deviceId, byte[] data) {
        checkChannel();
        final Message msg = new Message();

        // 消息头
        HeadInfo headInfo = new HeadInfo();
        headInfo.setVersion(DmsConstants.MSG_VERSION);
        // SMF-安全模式不能加密和签名-需在服务端补上加密和签名
        headInfo.setSecureModel(DmsConstants.SecureModelEnum.SDM_SECMODE_NOT.getCode());
        headInfo.setRetain(headInfo.getRetain());
        long msgId = atomicMsgId.getAndIncrement();
        headInfo.setMsgId(msgId);
        headInfo.setPduLength(data.length);
        // 目的方ID
        headInfo.setDestId(Arrays.copyOf(deviceId.getBytes(), 32));
        // 源ID
        headInfo.setSourceId(Arrays.copyOf(localId, 32));
        headInfo.setOpType(DmsConstants.MsgTypeEnum.DATA.getCode());
        msg.setHeadInfo(headInfo);

        //消息PDU
        msg.setPdu(data);

        ChannelFuture future = this.channel.writeAndFlush(msg);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("数据发送成功,msgId:{}", msgId);
                }
            }
        });
    }

    public Message sync(String deviceId, byte[] data, long timeout, TimeUnit unit) throws Exception {
        checkChannel();
        final Message msg = new Message();

        // 消息头
        HeadInfo headInfo = new HeadInfo();
        headInfo.setVersion((byte) 1);
        // SMF-安全模式不能加密和签名-需在服务端补上加密和签名
        headInfo.setSecureModel(DmsConstants.SecureModelEnum.SDM_SECMODE_RET.getCode());
        headInfo.setRetain(headInfo.getRetain());
        long msgId = atomicMsgId.getAndIncrement();
        headInfo.setMsgId(msgId);
        headInfo.setPduLength(data.length);
        // 目的方ID
        headInfo.setDestId(Arrays.copyOf(deviceId.getBytes(), 32));
        // 源ID
        headInfo.setSourceId(Arrays.copyOf(localId, 32));
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
                    log.info("数据发送成功,msgId:{}", msgId);
                }
            }
        });
        if (timeout < 0) {
            return null;
        }

        try {
            return messageFuture.get(timeout, unit);
        } catch (Exception e) {
            log.error("wait response error,deviceId:{},msgId:{},errorMsg:{}", deviceId, msgId, e.getMessage());
            if (e instanceof TimeoutException) {
                throw e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }


    public HashMap<Long, byte[]> get(String deviceId, long[] aids, long timeout, TimeUnit unit) throws Exception {
        checkChannel();
        final Message msg = new Message();

        // 消息头
        HeadInfo headInfo = new HeadInfo();
        headInfo.setVersion((byte) 1);
        // SMF-安全模式不能加密和签名-需在服务端补上加密和签名
        headInfo.setSecureModel(DmsConstants.SecureModelEnum.SDM_SECMODE_RET.getCode());
        headInfo.setRetain(headInfo.getRetain());
        long msgId = atomicMsgId.getAndIncrement();
        headInfo.setMsgId(msgId);

        byte[] data = getData(deviceId, aids);

        headInfo.setPduLength(data.length);
        // 目的方ID
        headInfo.setDestId(Arrays.copyOf(deviceId.getBytes(), 32));
        // 源ID
        headInfo.setSourceId(Arrays.copyOf(localId, 32));
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
                    log.info("数据发送成功,msgId:{}", msgId);
                }
            }
        });
        if (timeout < 0) {
            return null;
        }

        try {
            Message message = messageFuture.get(timeout, unit);
            byte[] response = message.getPdu();
            if (response == null || response.length == 0) {
                throw new RuntimeException("Wrong get fail!");
            }
            return setData(deviceId, response);
        } catch (Exception e) {
            log.error("wait response error,deviceId:{},msgId:{},errorMsg:{}", deviceId, msgId, e.getMessage());
            if (e instanceof TimeoutException) {
                throw e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private byte[] getData(String deviceId, long[] aids) throws Exception {
        if (StringUtils.isBlank(deviceId) || aids.length == 0 || aids == null) {
            throw new IllegalArgumentException("aid cannot be empty");
        }
        byte[] data;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            dataOutputStream.writeByte(0x00);
            dataOutputStream.writeByte(0xB0);
            for (long aid : aids) {
                dataOutputStream.writeLong(aid);
            }
            data = outputStream.toByteArray();
        }
        return data;
    }

    private HashMap<Long, byte[]> setData(String deviceId, byte[] data) throws Exception {
        HashMap<Long, byte[]> resultMap = Maps.newHashMap();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(data))) {
            byte header = input.readByte();
            byte type = input.readByte();
            if (header != (byte) 0x00 && type != (byte) 0xB2) {
                throw new RuntimeException("Wrong get error!");
            }
            byte flag = input.readByte();
            // 正常
            if (flag == 0x00) {
                while (input.available() > 0) {
                    long resAid = input.readLong();
                    int length = input.readInt();
                    byte[] value = new byte[length];
                    input.read(value);
                    resultMap.put(resAid, value);
                }
            } else {// 异常 0x01
                while (input.available() > 0) {
                    long resAid = input.readLong();
                    int code = input.readInt();
                    log.error("GM0050 get,deviceId:{},aid:{},code:{},codeString:{}", deviceId, resAid, code, Integer.toHexString(code));
                }
                throw new RuntimeException("Wrong get error!");
            }
        }
        return resultMap;
    }


    /**
     * 获取本地ID（中心ID），SMF接口获取数据时需要先获取本地中心ID
     *
     * @return
     */
    public byte[] getLocalId() {
        checkChannel();
        final Message msg = new Message();

        // 消息头
        HeadInfo headInfo = new HeadInfo();
        headInfo.setVersion(DmsConstants.MSG_VERSION);
        // SMF-安全模式不能加密和签名-需在服务端补上加密和签名
        headInfo.setSecureModel(DmsConstants.SecureModelEnum.SDM_SECMODE_RET.getCode());
        headInfo.setRetain(headInfo.getRetain());
        long msgId = atomicMsgId.getAndIncrement();
        headInfo.setMsgId(msgId);
        headInfo.setPduLength(0);
        // 目的方ID
        headInfo.setDestId(Arrays.copyOf("".getBytes(), 32));
        // 源ID
        headInfo.setSourceId(Arrays.copyOf("".getBytes(), 32));
        headInfo.setOpType(DmsConstants.MsgTypeEnum.GET_LOCAL_ID.getCode());
        msg.setHeadInfo(headInfo);

        final MessageFuture messageFuture = new MessageFuture();
        messageFuture.setMessage(msg);
        messageFuture.setTimeout(TimeUnit.SECONDS.toMillis(dmsProperties.getTimeout()));

        futures.put(msgId, messageFuture);

        ChannelFuture future = this.channel.writeAndFlush(msg);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("获取中心ID数据发送成功,msgId:{}", msgId);
                }
            }
        });
        Message message;
        try {
            message = messageFuture.get(dmsProperties.getTimeout(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("get local id response error,msgId:{},errorMsg:{}", msgId, e.getMessage());
            throw new RuntimeException(e);
        }
        byte[] sourceId = message.getHeadInfo().getSourceId();
        if (sourceId == null || sourceId.length == 0) {
            throw new RuntimeException("获取中心ID失败");
        }
        this.localId = sourceId;
        return this.localId;
    }

    private void checkChannel() {
        if (this.channel == null || !this.channel.isActive()) {
            throw new RuntimeException("和服务器还未建立起有效连接!请稍后再试!");
        }
    }

}
