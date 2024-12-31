package com.zjhy.love.worktools.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StorageObject {

    private final StringProperty key = new SimpleStringProperty();
    private final StringProperty size = new SimpleStringProperty();
    private final StringProperty lastModified = new SimpleStringProperty();

    public StorageObject() {
    }

    public StorageObject(String key, long size, String lastModified) {
        setKey(key);
        setSize(String.valueOf(size));
        setLastModified(lastModified);
    }


    public String getKey() {
        return key.get();
    }

    public void setKey(String value) {
        key.set(value);
    }

    public StringProperty keyProperty() {
        return key;
    }

    public String getSize() {
        return size.get();
    }

    public void setSize(String value) {
        size.set(value);
    }

    public StringProperty sizeProperty() {
        return size;
    }

    public String getLastModified() {
        return lastModified.get();
    }

    public void setLastModified(String value) {
        lastModified.set(value);
    }

    public StringProperty lastModifiedProperty() {
        return lastModified;
    }

    /**
     * 格式化文件大小
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    @Override
    public String toString() {
        return "StorageObjectItem{" +
                "key='" + getKey() + '\'' +
                ", size='" + getSize() + '\'' +
                ", lastModified='" + getLastModified() + '\'' +
                '}';
    }
}
