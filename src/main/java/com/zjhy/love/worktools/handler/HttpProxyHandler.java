package com.zjhy.love.worktools.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * HTTP代理处理器
 * 处理HTTP请求的转发逻辑
 */
public class HttpProxyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyHandler.class);
    private final Map<String, String> serviceMapping;

    public HttpProxyHandler(Map<String, String> serviceMapping) {
        this.serviceMapping = serviceMapping;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String host = request.headers().get(HttpHeaderNames.HOST);
        if (host == null) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        // 获取目标服务地址
        String target = serviceMapping.get(host);
        if (target == null) {
            LOGGER.warn("未找到服务映射: {}", host);
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // 修改请求
        request.headers().set(HttpHeaderNames.HOST, target);
        
        // 转发请求
        ctx.fireChannelRead(request.retain());
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, status
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("代理处理异常", cause);
        ctx.close();
    }
} 