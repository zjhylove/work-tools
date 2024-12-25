package com.zjhy.love.worktools.model;

import javafx.beans.property.*;

/**
 * IP转发条目
 * 存储单个转发规则的配置信息
 */
public class ForwardEntry {
    /**
     * 转发规则名称
     */
    private final StringProperty name = new SimpleStringProperty();
    
    /**
     * 本地监听地址
     */
    private final StringProperty localHost = new SimpleStringProperty("127.0.0.1");
    
    /**
     * 本地监听端口
     */
    private final IntegerProperty localPort = new SimpleIntegerProperty();
    
    /**
     * 远程目标地址
     */
    private final StringProperty remoteHost = new SimpleStringProperty();
    
    /**
     * 远程目标端口
     */
    private final IntegerProperty remotePort = new SimpleIntegerProperty();

    public ForwardEntry() {
    }

    // Name property
    public String getName() {
        return name.get();
    }
    public void setName(String value) {
        name.set(value);
    }
    public StringProperty nameProperty() {
        return name;
    }

    // LocalHost property
    public String getLocalHost() {
        return localHost.get();
    }
    public void setLocalHost(String value) {
        localHost.set(value);
    }
    public StringProperty localHostProperty() {
        return localHost;
    }

    // LocalPort property
    public int getLocalPort() {
        return localPort.get();
    }
    public void setLocalPort(int value) {
        localPort.set(value);
    }
    public IntegerProperty localPortProperty() {
        return localPort;
    }

    // RemoteHost property
    public String getRemoteHost() {
        return remoteHost.get();
    }
    public void setRemoteHost(String value) {
        remoteHost.set(value);
    }
    public StringProperty remoteHostProperty() {
        return remoteHost;
    }

    // RemotePort property
    public int getRemotePort() {
        return remotePort.get();
    }
    public void setRemotePort(int value) {
        remotePort.set(value);
    }
    public IntegerProperty remotePortProperty() {
        return remotePort;
    }
} 