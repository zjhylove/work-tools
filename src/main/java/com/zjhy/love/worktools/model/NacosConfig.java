package com.zjhy.love.worktools.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Nacos配置
 * 用于存储Nacos连接信息和服务列表
 */
public class NacosConfig {
    /**
     * Nacos服务器地址
     */
    private String serverAddr;
    
    /**
     * 命名空间
     */
    private String namespace;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 要转发的服务名称列表
     */
    private List<String> serviceNames = new ArrayList<>();

    /**
     * 分组名称
     */
    private String groupName = "DEFAULT_GROUP";

    // Getters and Setters
    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
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

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
} 