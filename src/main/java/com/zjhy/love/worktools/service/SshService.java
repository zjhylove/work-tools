package com.zjhy.love.worktools.service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSH服务类
 * 处理SSH连接和端口转发功能
 */
public class SshService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshService.class);
    private final SimpleBooleanProperty connected = new SimpleBooleanProperty(false);
    private Session session;

    /**
     * 获取连接状态属性
     */
    public ReadOnlyBooleanProperty connectedProperty() {
        return connected;
    }

    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * 连接到SSH服务器
     *
     * @param host     SSH服务器地址
     * @param port     SSH服务器端口
     * @param username 用户名
     * @param password 密码
     * @throws Exception 连接异常
     */
    public void connect(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");

        try {
            session.connect();
            connected.set(true);
            LOGGER.info("SSH连接成功: {}:{}", host, port);
        } catch (Exception e) {
            session = null;
            connected.set(false);
            throw e;
        }
    }

    /**
     * 添加端口转发规则
     *
     * @param localHost  本地监听地址
     * @param localPort  本地监听端口
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
        if (session != null) {
            session.disconnect();
            session = null;
        }
        connected.set(false);
        LOGGER.info("SSH连接已断开");
    }

    /**
     * 移除端口转发规则
     *
     * @param localHost 本地监听地址
     * @param localPort 本地监听端口
     * @throws Exception 移除异常
     */
    public void removePortForwarding(String localHost, int localPort) throws Exception {
        if (session == null || !session.isConnected()) {
            throw new IllegalStateException("SSH未连接");
        }
        session.delPortForwardingL(localHost, localPort);
        LOGGER.info("移除端口转发: {}:{}", localHost, localPort);
    }
} 