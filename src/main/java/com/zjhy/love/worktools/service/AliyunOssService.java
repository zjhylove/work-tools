package com.zjhy.love.worktools.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.model.*;
import com.zjhy.love.worktools.model.ObjectStorageConfig;
import com.zjhy.love.worktools.model.StorageObject;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AliyunOssService implements ObjectStorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunOssService.class);
    private OSS ossClient;
    private String bucket;

    @Override
    public void init(ObjectStorageConfig config) {
        ossClient = new OSSClientBuilder().build(
            config.getEndpoint(),
            config.getAccessKeyId(),
            config.getAccessKeySecret()
        );
        bucket = config.getBucket();
        LOGGER.info("阿里云OSS客户端初始化成功");
    }

    @Override
    public void uploadFile(String key, File file, DoubleProperty progress) {
        try {
            PutObjectRequest request = new PutObjectRequest(bucket, key, file);
            
            // 使用 PutObjectRequest 的进度回调
            request.withProgressListener(progressEvent -> {
                Platform.runLater(() -> {
                    if (progressEvent.getEventType() == ProgressEventType.TRANSFER_COMPLETED_EVENT) {
                        progress.set(1.0);
                    } else if (progressEvent.getEventType() == ProgressEventType.REQUEST_CONTENT_LENGTH_EVENT) {
                        // 记录总大小
                        progress.set(0.0);
                    } else if (progressEvent.getEventType() == ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT) {
                        // 更新进度
                        long transferred = progressEvent.getBytes();
                        long total = file.length();
                        if (total > 0) {
                            progress.set((double) transferred / total);
                        }
                    }
                });
            });
            
            ossClient.putObject(request);
        } catch (Exception e) {
            LOGGER.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadFile(String key, File targetFile, DoubleProperty progress) {
        try {
            GetObjectRequest request = new GetObjectRequest(bucket, key);
            
            // 先获取文件大小
            ObjectMetadata metadata = ossClient.getObjectMetadata(bucket, key);
            long totalSize = metadata.getContentLength();
            
            // 使用 GetObjectRequest 的进度回调
            request.withProgressListener(progressEvent -> {
                Platform.runLater(() -> {
                    if (progressEvent.getEventType() == ProgressEventType.TRANSFER_COMPLETED_EVENT) {
                        progress.set(1.0);
                    } else if (progressEvent.getEventType() == ProgressEventType.RESPONSE_BYTE_TRANSFER_EVENT) {
                        // 更新进度
                        long transferred = progressEvent.getBytes();
                        if (totalSize > 0) {
                            progress.set((double) transferred / totalSize);
                        }
                    }
                });
            });
            
            ossClient.getObject(request, targetFile);
        } catch (Exception e) {
            LOGGER.error("下载文件失败", e);
            throw new RuntimeException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<StorageObject> listObjects(String prefix, int maxKeys) {
        try {
            ListObjectsRequest request = new ListObjectsRequest(bucket);
            request.setPrefix(prefix);
            request.setMaxKeys(maxKeys);
            
            ObjectListing result = ossClient.listObjects(request);
            List<StorageObject> objects = new ArrayList<>();
            
            for (OSSObjectSummary summary : result.getObjectSummaries()) {
                objects.add(new StorageObject(
                    summary.getKey(),
                    summary.getSize(),
                    summary.getLastModified().toString()
                ));
            }
            return objects;
        } catch (Exception e) {
            LOGGER.error("列举对象失败", e);
            throw new RuntimeException("列举对象失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteObject(String key) {
        try {
            ossClient.deleteObject(bucket, key);
        } catch (Exception e) {
            LOGGER.error("删除对象失败", e);
            throw new RuntimeException("删除对象失败: " + e.getMessage());
        }
    }

    @Override
    public String getObjectUrl(String key) {
        try {
            return ossClient.generatePresignedUrl(bucket, key, 
                new Date(System.currentTimeMillis() + 3600 * 1000)).toString();
        } catch (Exception e) {
            LOGGER.error("获取对象URL失败", e);
            throw new RuntimeException("获取对象URL失败: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
            LOGGER.info("阿里云OSS客户端已关闭");
        }
    }
} 