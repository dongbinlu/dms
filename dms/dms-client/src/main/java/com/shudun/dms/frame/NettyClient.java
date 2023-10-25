package com.shudun.dms.frame;

import com.shudun.dms.properties.NettyProperties;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableConfigurationProperties(NettyProperties.class)
public class NettyClient implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {

    /*负责重连的线程池*/
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private Channel channel;

    private EventLoopGroup group = new NioEventLoopGroup();

    /*是否用户主动关闭连接的标志值*/
    private volatile boolean userClose = false;

    @Autowired
    private NettyProperties nettyProperties;

    @Autowired
    private ClientInit clientInit;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        connect(nettyProperties.getIp(), nettyProperties.getPort());
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        try {
            this.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("netty client stop ......");
    }

    /*连接服务器*/
    public void connect(String host, int port) throws InterruptedException {
        try {
            /*客户端启动必备*/
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)/*指定使用NIO的通信模式*/
                    .option(ChannelOption.SO_RCVBUF, 2 * 1024 * 1024)// 接收缓冲区为2M
                    .option(ChannelOption.SO_SNDBUF, 2 * 1024 * 1024)// 发送缓冲区为2M
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, false)
                    .option(ChannelOption.SO_LINGER, 1) //关闭时等待1s发送关闭
                    .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                    .handler(clientInit);
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
                        connect(nettyProperties.getIp(), nettyProperties.getPort());
                    } catch (InterruptedException e) {
                        log.error("客户端重连异常", e);
                    }
                });

            }
        }
    }


    public void close() throws InterruptedException {
        this.userClose = true;
        this.channel.close();
        group.shutdownGracefully().sync();
    }
}
