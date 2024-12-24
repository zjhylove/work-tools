package com.zjhy.love.worktools.service;

import com.zjhy.love.worktools.handler.HttpProxyHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP代理服务
 * 用于处理HTTP请求的转发
 */
public class HttpProxyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyService.class);
    
    private final Map<String, String> serviceMapping = new ConcurrentHashMap<>();
    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * 启动HTTP代理服务器
     */
    public void start(int port) {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                            new HttpServerCodec(),
                            new HttpObjectAggregator(65536),
                            new HttpProxyHandler(serviceMapping)
                        );
                    }
                });

            serverChannel = b.bind(port).sync().channel();
            LOGGER.info("HTTP代理服务器启动在端口: {}", port);
        } catch (Exception e) {
            LOGGER.error("启动HTTP代理服务器失败", e);
            shutdown();
        }
    }

    /**
     * 添加服务映射
     */
    public void addServiceMapping(String domain, String target) {
        serviceMapping.put(domain, target);
        LOGGER.info("添加服务映射: {} -> {}", domain, target);
    }

    /**
     * 关闭代理服务器
     */
    public void shutdown() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        LOGGER.info("HTTP代理服务器已关闭");
    }
} 