package com.zjhy.love.worktools.model;

/**
 * IP转发条目
 * 存储单个转发规则的配置信息
 */
public class ForwardEntry {
    /**
     * 本地监听地址
     */
    private String localHost;
    
    /**
     * 本地监听端口
     */
    private int localPort;
    
    /**
     * 远程目标地址
     */
    private String remoteHost;
    
    /**
     * 远程目标端口
     */
    private int remotePort;

    public ForwardEntry() {
        this.localHost = "127.0.0.1";
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
} 