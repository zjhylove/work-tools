package com.zjhy.love.worktools.service;

import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * HTTP代理服务
 * 处理HTTP请求的转发，支持基于域名的请求路由
 */
public class HttpProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProxyService.class);

    private static final StreamProgress STREAM_PROGRESS = new StreamProgress() {
        @Override
        public void start() {
            LOGGER.info("开始拷贝转发内容");
        }

        @Override
        public void progress(long total, long progressSize) {
            LOGGER.info("拷贝转发内容进度：{}%", progressSize * 100 / total);
        }

        @Override
        public void finish() {
            LOGGER.info("完成拷贝转发内容");
        }
    };

    /**
     * 服务映射表
     * 存储域名到目标服务器的映射关系
     */
    private final Map<String, String> serviceMapping = new ConcurrentHashMap<>();

    private Tomcat tomcat;

    private Context ctx;

    /**
     * 启动HTTP代理服务器
     * 在指定端口启动代理服务，并初始化事件处理器
     *
     * @param port 监听端口
     */
    public void start(int port) {
        tomcat = new Tomcat();
        tomcat.setPort(port);
        ctx = tomcat.addContext("", null);
        try {
            Tomcat.addServlet(ctx, "hostProxy", new HttpServlet() {
                @Override
                protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                    doProxy(req, resp, HttpUtil::createPost);
                }

                @Override
                protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                    doProxy(req, resp, HttpUtil::createGet);
                }
            });
            ctx.addServletMappingDecoded("/*", "hostProxy");
            tomcat.getConnector();
            tomcat.start();
            LOGGER.info("HTTP代理服务器启动在端口: {}", port);
        } catch (Exception e) {
            LOGGER.error("启动HTTP代理服务器失败", e);
            shutdown();
        }
    }


    public void doProxy(HttpServletRequest req, HttpServletResponse resp, Function<String, HttpRequest> proxyCreator) throws IOException {
        String originHost = req.getHeader("host");
        LOGGER.info("originHost:{}", originHost);
        String forwardHost = serviceMapping.get(originHost);
        if (Objects.isNull(forwardHost)) {
            forwardHost = originHost;
        }
        LOGGER.info("远程请求代理为本地请求:{}", forwardHost);
        String forwardUrl = forwardHost + req.getRequestURI();
        String queryString = req.getQueryString();
        if (CharSequenceUtil.isNotBlank(queryString)) {
            forwardUrl += "?" + queryString;
        }
        LOGGER.info("请求地地址：{}", forwardUrl);
        HttpRequest httpRequest = proxyCreator.apply("http://" + forwardUrl);
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            httpRequest.header(name, req.getHeader(name));
        }
        try (ServletInputStream inputStream = req.getInputStream()) {
            httpRequest.body(new InputStreamResource(inputStream));
            try (HttpResponse response = httpRequest.execute()) {
                response.charset(StandardCharsets.UTF_8);
                Map<String, List<String>> headers = response.headers();
                headers.forEach((k, v) -> resp.setHeader(k, ArrayUtil.join(v.toArray(), ",")));
                try (ServletOutputStream outputStream = resp.getOutputStream()) {
                    response.writeBody(outputStream, true, STREAM_PROGRESS);
                }
            }
        }
    }

    /**
     * 关闭理服务器
     * 清理所有资源并停止服务
     */
    public void shutdown() {
        if (ctx != null) {
            try {
                ctx.stop();
            } catch (LifecycleException e) {
                LOGGER.error("停止http代理服务出错", e);
            }
        }

        if (tomcat != null) {
            try {
                tomcat.stop();
            } catch (LifecycleException e) {
                LOGGER.error("停止http代理服务出错", e);
            }
        }
        LOGGER.info("HTTP代理服务器已关闭");
    }

    /**
     * 注册服务映射
     * 将服务名和本地端口映射到域名
     *
     * @param remoteAddr  远程地址
     * @param serviceName 服务名称
     * @param localPort   本地端口
     */
    public void registerService(String remoteAddr, String serviceName, int localPort) {
        String target = "127.0.0.1:" + localPort;
        serviceMapping.put(remoteAddr, target);
        LOGGER.info("注册服务映射: {} ({}) -> {}", remoteAddr, serviceName, target);
    }

    /**
     * 移除服务映射
     * 删除域名和目标地址的映射关系
     *
     * @param remoteAddr 远程地址
     */
    public void removeServiceMapping(String remoteAddr) {
        serviceMapping.remove(remoteAddr);
        LOGGER.info("移除服务映射: {}", remoteAddr);
    }

    /**
     * 检查服务是否正在运行
     */
    public boolean isRunning() {
        return tomcat != null && tomcat.getServer().getState().isAvailable();
    }

    /**
     * 停止代理服务
     */
    public void stop() {
        shutdown();
    }
}
