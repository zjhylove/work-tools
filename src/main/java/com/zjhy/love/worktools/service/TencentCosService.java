package com.zjhy.love.worktools.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import com.zjhy.love.worktools.model.ObjectStorageConfig;
import com.zjhy.love.worktools.model.StorageObject;
import javafx.beans.property.DoubleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TencentCosService implements ObjectStorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TencentCosService.class);
    private COSClient cosClient;
    private TransferManager transferManager;
    private String bucket;
    private String region;

    @Override
    public void init(ObjectStorageConfig config) {
        COSCredentials credentials = new BasicCOSCredentials(
            config.getAccessKeyId(), 
            config.getAccessKeySecret()
        );
        
        ClientConfig clientConfig = new ClientConfig(new Region(config.getRegion()));
        cosClient = new COSClient(credentials, clientConfig);
        transferManager = new TransferManager(cosClient);
        
        this.bucket = config.getBucket();
        this.region = config.getRegion();
        
        LOGGER.info("腾讯云COS客户端初始化成功");
    }

    @Override
    public void uploadFile(String key, File file, DoubleProperty progress) {
        try {
            PutObjectRequest request = new PutObjectRequest(bucket, key, file);
            Upload upload = transferManager.upload(request);
            
            while (!upload.isDone()) {
                progress.set(upload.getProgress().getPercentTransferred() / 100);
                Thread.sleep(100);
            }
            
            upload.waitForCompletion();
            progress.set(1.0);
        } catch (Exception e) {
            LOGGER.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadFile(String key, File targetFile, DoubleProperty progress) {
        try {
            GetObjectRequest request = new GetObjectRequest(bucket, key);
            Download download = transferManager.download(request, targetFile);
            
            while (!download.isDone()) {
                progress.set(download.getProgress().getPercentTransferred() / 100);
                Thread.sleep(100);
            }
            
            download.waitForCompletion();
            progress.set(1.0);
        } catch (Exception e) {
            LOGGER.error("下载文件失败", e);
            throw new RuntimeException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public List<StorageObject> listObjects(String prefix, int maxKeys) {
        try {
            ListObjectsRequest request = new ListObjectsRequest();
            request.setBucketName(bucket);
            request.setPrefix(prefix);
            request.setMaxKeys(maxKeys);
            
            ObjectListing result = cosClient.listObjects(request);
            List<StorageObject> objects = new ArrayList<>();
            
            for (COSObjectSummary summary : result.getObjectSummaries()) {
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
            cosClient.deleteObject(bucket, key);
        } catch (Exception e) {
            LOGGER.error("删除对象失败", e);
            throw new RuntimeException("删除对象失败: " + e.getMessage());
        }
    }

    @Override
    public String getObjectUrl(String key) {
        try {
            Date expirationDate = new Date(System.currentTimeMillis() + 3600 * 1000);
            return cosClient.generatePresignedUrl(bucket, key, expirationDate).toString();
        } catch (Exception e) {
            LOGGER.error("获取对象URL失败", e);
            throw new RuntimeException("获取对象URL失败: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        if (transferManager != null) {
            transferManager.shutdownNow();
        }
        if (cosClient != null) {
            cosClient.shutdown();
        }
        LOGGER.info("腾讯云COS客户端已关闭");
    }
} 