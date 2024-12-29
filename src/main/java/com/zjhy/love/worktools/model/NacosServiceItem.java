package com.zjhy.love.worktools.model;

import javafx.beans.property.*;

/**
 * Nacos服务UI展示模型
 */
public class NacosServiceItem {
    private final StringProperty serviceName = new SimpleStringProperty();
    private final StringProperty groupName = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty status = new SimpleStringProperty();

    public NacosServiceItem(String serviceName, String groupName) {
        setServiceName(serviceName);
        setGroupName(groupName);
        setStatus("未转发");
    }

    public String getServiceName() { return serviceName.get(); }
    public void setServiceName(String value) { serviceName.set(value); }
    public StringProperty serviceNameProperty() { return serviceName; }

    public String getGroupName() { return groupName.get(); }
    public void setGroupName(String value) { groupName.set(value); }
    public StringProperty groupNameProperty() { return groupName; }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean value) { selected.set(value); }
    public BooleanProperty selectedProperty() { return selected; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }
} 