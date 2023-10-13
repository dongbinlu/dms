package com.shudun.dms.frame;

import com.shudun.dms.properties.NettyProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableConfigurationProperties(NettyProperties.class)
public class NettyServer implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {

    private Channel channel;

    @Autowired
    private ServerInit serverInit;

    @Autowired
    private NettyProperties nettyProperties;

    // 配置服务端的NIO线程组
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private EventLoopGroup workerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors());

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(serverInit);
            log.info("netty server start ...... ");
            ChannelFuture future = bootstrap.bind(nettyProperties.getIp(), nettyProperties.getPort()).sync();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("监听端口{}成功", nettyProperties.getPort());
                    } else {
                        log.error("监听端口{}失败", nettyProperties.getPort());
                    }
                }
            });
            this.channel = future.channel();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if (this.channel != null) {
            this.channel.close();
        }
        log.info("netty server stop ......");
    }

}
