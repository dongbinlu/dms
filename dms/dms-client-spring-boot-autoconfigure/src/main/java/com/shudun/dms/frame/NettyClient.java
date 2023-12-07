package com.shudun.dms.frame;

import com.shudun.dms.channel.IChannel;
import com.shudun.dms.channel.JavaChannel;
import com.shudun.dms.constant.DmsConstants;
import com.shudun.dms.global.GlobalVariable;
import com.shudun.dms.handshake.HsmInfo;
import com.shudun.dms.message.HeadInfo;
import com.shudun.dms.message.Message;
import com.shudun.dms.message.MessageFuture;
import com.shudun.dms.properties.DmsProperties;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

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

    private EventLoopGroup group = new NioEventLoopGroup();

    /**
     * 是否用户主动关闭连接的标志值
     */
    private volatile boolean userClose = false;

    private DmsProperties dmsProperties;

    private MessageHandler messageHandler;

    private HsmInfo hsmInfo;

    private AtomicLong atomicMsgId = new AtomicLong(0L);

    public NettyClient(DmsProperties dmsProperties, MessageHandler messageHandler, HsmInfo hsmInfo) {
        this.dmsProperties = dmsProperties;
        this.messageHandler = messageHandler;
        this.messageHandler.setFutures(this.futures);
        this.hsmInfo = hsmInfo;
    }

    public void init() {
        try {
            if (channel != null){
                this.destroy();
            }
            connect(hsmInfo.getIp(), hsmInfo.getPort());
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
                    .handler(new ClientInit(messageHandler, hsmInfo));
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            channel = future.channel();
            log.info("客户端{} 已连接服务器 {}", channel.localAddress(), channel.remoteAddress());
            channel.closeFuture().sync();
        } finally {
            if (!userClose) {

                //非正常关闭或连接失败，有可能发生了网络问题，进行重连

                log.warn("重连中,请稍等......");
                executor.execute(() -> {
                    //给操作系统足够的时间，去释放相关的资源
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        connect(hsmInfo.getIp(), hsmInfo.getPort());
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
        if (this.channel == null || !this.channel.isActive()) {
            throw new RuntimeException("和服务器还未建立起有效连接!请稍后再试!");
        }

        Attribute<IChannel> attribute = channel.attr(GlobalVariable.CHANNEL_KEY);
        JavaChannel javaChannel = (JavaChannel) attribute.get();
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
        headInfo.setSourceId(Arrays.copyOf(javaChannel.getHandShake().getSourceId(), 32));
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
        if (this.channel == null || !this.channel.isActive()) {
            throw new RuntimeException("和服务器还未建立起有效连接!请稍后再试!");
        }

        Attribute<IChannel> attribute = channel.attr(GlobalVariable.CHANNEL_KEY);
        JavaChannel javaChannel = (JavaChannel) attribute.get();
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
        headInfo.setSourceId(Arrays.copyOf(javaChannel.getHandShake().getSourceId(), 32));
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

}
