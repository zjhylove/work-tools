package com.zjhy.love.worktools.model;

public class StorageObject {
    private final String key;
    private final long size;
    private final String lastModified;
    private final String eTag;

    public StorageObject(
            String key,
            long size,
            String lastModified,
            String eTag
    ) {
        this.key = key;
        this.size = size;
        this.lastModified = lastModified;
        this.eTag = eTag;
    }

    public String key() {
        return key;
    }

    public long size() {
        return size;
    }

    public String lastModified() {
        return lastModified;
    }

    public String eTag() {
        return eTag;
    }
}
