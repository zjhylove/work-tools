package com.zjhy.love.worktools.service;

import com.zjhy.love.worktools.model.ObjectStorageConfig;
import com.zjhy.love.worktools.model.StorageObject;
import javafx.beans.property.DoubleProperty;

import java.io.File;
import java.util.List;

public interface ObjectStorageService {
    /**
     * 初始化存储服务
     */
    void init(ObjectStorageConfig config);

    /**
     * 上传文件
     * @param key 对象键
     * @param file 本地文件
     * @param progress 进度属性
     */
    void uploadFile(String key, File file, DoubleProperty progress);

    /**
     * 下载文件
     * @param key 对象键
     * @param targetFile 目标文件
     * @param progress 进度属性
     */
    void downloadFile(String key, File targetFile, DoubleProperty progress);

    /**
     * 列出对象
     * @param prefix 前缀
     * @param maxKeys 最大数量
     * @return 对象列表
     */
    List<StorageObject> listObjects(String prefix, int maxKeys);

    /**
     * 删除对象
     * @param key 对象键
     */
    void deleteObject(String key);

    /**
     * 获取对象URL
     * @param key 对象键
     * @return 访问URL
     */
    String getObjectUrl(String key);

    /**
     * 关闭服务
     */
    void shutdown();
}