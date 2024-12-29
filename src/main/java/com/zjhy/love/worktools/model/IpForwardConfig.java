package com.zjhy.love.worktools.model;

import java.util.ArrayList;
import java.util.List;

/**
 * IP转发配置类
 * 用于保存SSH连接信息和转发规则配置
 *
 * @author zhengjun
 */
public class IpForwardConfig {
    /**
     * SSH服务器地址
     * 用于建立SSH隧道的远程服务器地址
     */
    private String host;
    
    /**
     * SSH服务器端口
     * 默认为22，用于SSH连接的端口号
     */
    private int port;
    
    /**
     * SSH用户名
     * 用于SSH服务器认证的用户名
     */
    private String username;
    
    /**
     * SSH密码
     * 用于SSH服务器认证的密码
     */
    private String password;
    
    /**
     * 转发规则列表
     * 存储所有配置的端口转发规则
     */
    private List<ForwardEntry> forwardEntries = new ArrayList<>();
    
    /**
     * Nacos配置
     * 用于Nacos服务发现的相关配置
     */
    private NacosConfig nacosConfig;

    // Getters and Setters
    /**
     * 获取SSH服务器地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置SSH服务器地址
     */
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

    public NacosConfig getNacosConfig() {
        return nacosConfig;
    }

    public void setNacosConfig(NacosConfig nacosConfig) {
        this.nacosConfig = nacosConfig;
    }
} 