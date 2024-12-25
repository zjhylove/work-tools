package com.zjhy.love.worktools.handler;

import io.netty.buffer.ByteBuf;
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

        try {
            // 获取目标服务地址
            String target = serviceMapping.get(host);
            
            // 保留原始URI（包含路径和查询参数）
            String uri = request.uri();

            // 获取请求体内容
            ByteBuf content = request.content().copy();

            // 创建新的请求对象
            DefaultFullHttpRequest forwardRequest = new DefaultFullHttpRequest(
                request.protocolVersion(),
                request.method(),
                uri,
                content,
                request.headers().copy(),
                request.trailingHeaders().copy()
            );
            
            if (target != null) {
                LOGGER.debug("找到服务映射: {} -> {}, URI: {}", host, target, uri);
                forwardRequest.headers().set(HttpHeaderNames.HOST, target);
            } else {
                LOGGER.debug("未找到服务映射，使用原始地址: {}, URI: {}", host, uri);
            }
            
            // 更新Content-Length头
            forwardRequest.headers().set(
                HttpHeaderNames.CONTENT_LENGTH, 
                content.readableBytes()
            );
            
            // 保持连接
            forwardRequest.headers().set(
                HttpHeaderNames.CONNECTION,
                HttpHeaderValues.KEEP_ALIVE
            );
            
            // 转发请求
            ctx.fireChannelRead(forwardRequest);
            
        } catch (Exception e) {
            LOGGER.error("处理请求时发生错误", e);
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
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