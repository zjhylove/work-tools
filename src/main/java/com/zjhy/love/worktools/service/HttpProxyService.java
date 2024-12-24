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
 * 处理HTTP请求的转发，支持基于域名的请求路由
 */
public class HttpProxyService {
    /**
     * 日志记录器
     * 用于记录代理服务的运行日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyService.class);
    
    /**
     * 服务映射表
     * 存储域名到目标服务器的映射关系
     */
    private final Map<String, String> serviceMapping = new ConcurrentHashMap<>();

    /**
     * 服务器通道
     * 代理服务器的主监听通道
     */
    private Channel serverChannel;

    /**
     * 主事件循环组
     * 用于处理连接接受事件
     */
    private EventLoopGroup bossGroup;

    /**
     * 工作事件循环组
     * 用于处理连接的I/O事件
     */
    private EventLoopGroup workerGroup;

    /**
     * 启动HTTP代理服务器
     * 在指定端口启动代理服务，并初始化事件处理器
     * 
     * @param port 监听端口
     */
    public void start(int port) {
        // 创建事件循环组
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            // 配置服务器
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

            // 绑定端口并启动服务器
            serverChannel = b.bind(port).sync().channel();
            LOGGER.info("HTTP代理服务器启动在端口: {}", port);
        } catch (Exception e) {
            LOGGER.error("启动HTTP代理服务器失败", e);
            shutdown();
        }
    }

    /**
     * 添加服务映射
     * 将域名映射到目标服务器地址
     * 
     * @param domain 域名
     * @param target 目标地址
     */
    public void addServiceMapping(String domain, String target) {
        serviceMapping.put(domain, target);
        LOGGER.info("添加服务映射: {} -> {}", domain, target);
    }

    /**
     * 关闭代理服务器
     * 清理所有资源并停止服务
     */
    public void shutdown() {
        // 关闭服务器通道
        if (serverChannel != null) {
            serverChannel.close();
        }
        
        // 关闭事件循环组
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        
        LOGGER.info("HTTP代理服务器已关闭");
    }
} 