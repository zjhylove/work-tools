package com.zjhy.love.worktools.service;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zjhy.love.worktools.model.NacosConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * Nacos服务类
 * 处理与Nacos的交互
 */
public class NacosService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosService.class);
    private NamingService namingService;

    /**
     * 连接Nacos服务器
     */
    public void connect(NacosConfig config) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", config.getServerAddr());
        properties.setProperty("namespace", config.getNamespace());
        properties.setProperty("username", config.getUsername());
        properties.setProperty("password", config.getPassword());

        namingService = NacosFactory.createNamingService(properties);
        LOGGER.info("Nacos连接成功: {}", config.getServerAddr());
    }

    /**
     * 获取服务实例列表
     */
    public List<Instance> getServiceInstances(String serviceName, String groupName) throws Exception {
        if (namingService == null) {
            throw new IllegalStateException("Nacos未连接");
        }
        return namingService.getAllInstances(serviceName, groupName);
    }

    /**
     * 获取服务列表
     */
    public List<String> getServiceList(String groupName) throws Exception {
        if (namingService == null) {
            throw new IllegalStateException("Nacos未连接");
        }
        return namingService.getServicesOfServer(1, Integer.MAX_VALUE, groupName).getData();
    }

    /**
     * 关闭连接
     */
    public void shutdown() {
        if (namingService != null) {
            try {
                namingService.shutDown();
                LOGGER.info("Nacos连接已关闭");
            } catch (Exception e) {
                LOGGER.error("关闭Nacos连接失败", e);
            }
        }
    }
} 