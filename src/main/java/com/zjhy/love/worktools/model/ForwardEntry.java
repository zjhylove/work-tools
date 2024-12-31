package com.zjhy.love.worktools.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * IP转发条目
 * 用于存储和管理单个IP转发规则的配置信息，包括本地监听配置和远程目标配置
 * 使用JavaFX属性支持UI数据绑定
 */
public class ForwardEntry {

    public static final String TYPE_NACOS = "NACOS";
    public static final String TYPE_MANUAL = "MANUAL";


    /**
     * 转发规则名称
     * 用于标识和区分不同的转发规则
     */
    private final StringProperty name = new SimpleStringProperty();

    /**
     * 本地监听地址
     * 默认为127.0.0.1，表示本地回环地址
     */
    private final StringProperty localHost = new SimpleStringProperty("127.0.0.1");

    /**
     * 本地监听端口
     * 用于接收需要转发的连接请求
     */
    private final IntegerProperty localPort = new SimpleIntegerProperty();

    /**
     * 远程目标地址
     * 转发的目标服务器地址
     */
    private final StringProperty remoteHost = new SimpleStringProperty();

    /**
     * 远程目标端口
     * 转发的目标服务器端口
     */
    private final IntegerProperty remotePort = new SimpleIntegerProperty();

    /**
     * 规则类型：MANUAL-手动添加，NACOS-Nacos服务添加
     */

    private final StringProperty type = new SimpleStringProperty("MANUAL");

    /**
     * 默认构造函数
     */
    public ForwardEntry() {
    }

    /**
     * 创建转发规则
     *
     * @param name       规则名称
     * @param localHost  本地监听地址
     * @param localPort  本地监听端口
     * @param remoteHost 远程目标地址
     * @param remotePort 远程目标端口
     */
    public ForwardEntry(String name, String localHost, int localPort,
                        String remoteHost, int remotePort) {
        setName(name);
        setLocalHost(localHost);
        setLocalPort(localPort);
        setRemoteHost(remoteHost);
        setRemotePort(remotePort);
    }

    /**
     * 获取规则名称
     */
    public String getName() {
        return name.get();
    }

    /**
     * 设置规则名称
     */
    public void setName(String value) {
        name.set(value);
    }

    /**
     * 获取规则名称属性
     * 用于JavaFX数据绑定
     */
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

    public String getType() {
        return type.get();
    }

    public void setType(String value) {
        type.set(value);
    }

    public StringProperty typeProperty() {
        return type;
    }
}