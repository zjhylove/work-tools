package com.zjhy.love.worktools.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ObjectStorageConfig {
    private final StringProperty provider = new SimpleStringProperty();  // 提供商：ALIYUN/TENCENT
    private final StringProperty accessKeyId = new SimpleStringProperty();
    private final StringProperty accessKeySecret = new SimpleStringProperty();
    private final StringProperty endpoint = new SimpleStringProperty();
    private final StringProperty bucket = new SimpleStringProperty();
    private final StringProperty region = new SimpleStringProperty();  // 腾讯云需要

    // Getters and Setters
    public String getProvider() { return provider.get(); }
    public void setProvider(String value) { provider.set(value); }
    public StringProperty providerProperty() { return provider; }

    public String getAccessKeyId() { return accessKeyId.get(); }
    public void setAccessKeyId(String value) { accessKeyId.set(value); }
    public StringProperty accessKeyIdProperty() { return accessKeyId; }

    public String getAccessKeySecret() { return accessKeySecret.get(); }
    public void setAccessKeySecret(String value) { accessKeySecret.set(value); }
    public StringProperty accessKeySecretProperty() { return accessKeySecret; }

    public String getEndpoint() { return endpoint.get(); }
    public void setEndpoint(String value) { endpoint.set(value); }
    public StringProperty endpointProperty() { return endpoint; }

    public String getBucket() { return bucket.get(); }
    public void setBucket(String value) { bucket.set(value); }
    public StringProperty bucketProperty() { return bucket; }

    public String getRegion() { return region.get(); }
    public void setRegion(String value) { region.set(value); }
    public StringProperty regionProperty() { return region; }
} 