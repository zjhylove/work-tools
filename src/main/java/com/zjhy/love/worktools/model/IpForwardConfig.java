package com.zjhy.love.worktools.model;

import java.util.ArrayList;
import java.util.List;

/**
 * IP转发配置
 * 用于保存SSH连接信息和转发规则
 */
public class IpForwardConfig {
    /**
     * SSH服务器地址
     */
    private String host;
    
    /**
     * SSH服务器端口
     */
    private int port;
    
    /**
     * SSH用户名
     */
    private String username;
    
    /**
     * SSH密码
     */
    private String password;
    
    /**
     * 转发规则列表
     */
    private List<ForwardEntry> forwardEntries = new ArrayList<>();

    // Getters and Setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<ForwardEntry> getForwardEntries() {
        return forwardEntries;
    }

    public void setForwardEntries(List<ForwardEntry> forwardEntries) {
        this.forwardEntries = forwardEntries;
    }
} 