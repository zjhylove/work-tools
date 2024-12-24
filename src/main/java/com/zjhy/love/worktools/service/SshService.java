package com.zjhy.love.worktools.service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * SSH服务类
 * 处理SSH连接和端口转发功能
 */
public class SshService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshService.class);
    private Session session;

    /**
     * 连接SSH服务器
     * @param host SSH服务器地址
     * @param port SSH服务器端口
     * @param username 用户名
     * @param password 密码
     * @throws Exception 连接异常
     */
    public void connect(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect(30000);
        LOGGER.info("SSH连接成功: {}@{}", username, host);
    }

    /**
     * 添加端口转发规则
     * @param localHost 本地监听地址
     * @param localPort 本地监听端口
     * @param remoteHost 远程目标地址
     * @param remotePort 远程目标端口
     * @throws Exception 转发异常
     */
    public void addPortForwarding(String localHost, int localPort, String remoteHost, int remotePort) throws Exception {
        if (session == null || !session.isConnected()) {
            throw new IllegalStateException("SSH未连接");
        }
        session.setPortForwardingL(localHost, localPort, remoteHost, remotePort);
        LOGGER.info("添加端口转发: {}:{} -> {}:{}", localHost, localPort, remoteHost, remotePort);
    }

    /**
     * 断开SSH连接
     */
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            LOGGER.info("SSH连接已断开");
        }
    }

    /**
     * 检查SSH连接状态
     *
     * @return 是否已连接
     */
    public boolean isConnected() {
        return session != null && session.isConnected();
    }
} 