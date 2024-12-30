package com.zjhy.love.worktools.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Nacos服务UI展示模型
 */
public class NacosServiceItem {

    /**
     * 服务名称
     */
    private final StringProperty serviceName = new SimpleStringProperty();
    /**
     * 组信息
     */
    private final StringProperty groupName = new SimpleStringProperty();
    /**
     * 转发状态
     */
    private final StringProperty status = new SimpleStringProperty();

    public NacosServiceItem(String serviceName, String groupName) {
        setServiceName(serviceName);
        setGroupName(groupName);
        setStatus("未转发");
    }

    public String getServiceName() {
        return serviceName.get();
    }

    public void setServiceName(String value) {
        serviceName.set(value);
    }

    public StringProperty serviceNameProperty() {
        return serviceName;
    }

    public String getGroupName() {
        return groupName.get();
    }

    public void setGroupName(String value) {
        groupName.set(value);
    }

    public StringProperty groupNameProperty() {
        return groupName;
    }


    public String getStatus() {
        return status.get();
    }

    public void setStatus(String value) {
        status.set(value);
    }

    public StringProperty statusProperty() {
        return status;
    }
}