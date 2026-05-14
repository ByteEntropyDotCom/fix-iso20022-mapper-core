package com.byteentropy.fix_iso20022_mapper_core.engine;

import com.byteentropy.fix_iso20022_mapper_core.config.AppConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NettyTcpServer {
    private static final Logger log = LoggerFactory.getLogger(NettyTcpServer.class);

    private final FixFrameHandler handler;
    private final AppConfig appConfig;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyTcpServer(FixFrameHandler handler, AppConfig appConfig) {
        this.handler = handler;
        this.appConfig = appConfig;
    }

    @PostConstruct
    public void start() {
        Thread.ofVirtual().start(this::run);
    }

    private void run() {
        int port = appConfig.tcpPort();
        if (port == 0) {
            log.info("Netty server disabled (port 0).");
            return;
        }

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    // 1. Split the stream into frames based on Newline (\n or \r\n)
                    pipeline.addLast(new io.netty.handler.codec.LineBasedFrameDecoder(1024));
                    // 2. Convert bytes to String
                    pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                    // 3. Handle the message
                    pipeline.addLast(handler);
                }
             });

            ChannelFuture f = b.bind(port).sync();
            log.info("Netty FIX Server started on port {}", port);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stop();
        }
    }

    @PreDestroy
    public void stop() {
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }
}