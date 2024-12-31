package com.zjhy.love.worktools.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zjhy.love.worktools.model.NacosConfig;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Nacos服务发现管理类
 * 提供Nacos服务的连接管理、服务发现、实例监听等功能
 *
 * @author zhengjun
 */
public class NacosService {
    /**
     * 日志记录器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosService.class);
    /**
     * 服务列表变更监听器集合
     * key为服务名称，value为监听器回调函数
     */
    private final Map<String, Consumer<List<String>>> serviceListeners = new ConcurrentHashMap<>();
    /**
     * 服务实例变更监听器集合
     * key为服务名称，value为监听器回调函数
     */
    private final Map<String, Consumer<List<Instance>>> instanceListeners = new ConcurrentHashMap<>();
    /**
     * Nacos命名服务客户端
     */
    private NamingService namingService;
    /**
     * 连接状态标志
     */
    private boolean connected;
    /**
     * 最后一次使用的配置信息
     */
    private NacosConfig lastConfig;

    /**
     * 连接到Nacos服务器
     * 初始化命名服务客户端并建立连接
     *
     * @param config Nacos连接配置信息
     * @throws NacosException 连接过程中的异常
     */
    public void connect(NacosConfig config) throws NacosException {
        this.lastConfig = config;

        Properties properties = new Properties();
        properties.setProperty("serverAddr", config.getServerAddr());
        properties.setProperty("namespace", config.getNamespace());

        // 添加认证信息
        if (StrUtil.isNotBlank(config.getUsername())) {
            properties.setProperty("username", config.getUsername());
            properties.setProperty("password", config.getPassword());
        }

        namingService = NacosFactory.createNamingService(properties);
        //test connect
        getServiceList(config.getGroupName());
        connected = true;
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
    public List<String> getServiceList(String groupName) {
        if (namingService == null) {
            throw new IllegalStateException("Nacos未连接");
        }
        try {
            return namingService.getServicesOfServer(1, Integer.MAX_VALUE, groupName).getData();
        } catch (Exception e) {
            LOGGER.error("获取服务列表失败", e);
            connected = false; // 连接可能已断开
            throw new RuntimeException(e);
        }
    }

    /**
     * 订阅服务列表变化
     *
     * @param groupName 分组名称
     * @param listener  服务列表变化监听器
     */
    public void subscribeServices(String groupName, Consumer<List<String>> listener) throws Exception {
        if (namingService == null) {
            throw new IllegalStateException("Nacos未连接");
        }

        serviceListeners.put(groupName, listener);

        // 启动定期刷新服务列表的任务
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (connected) {
                    try {
                        List<String> services = getServiceList(groupName);
                        Platform.runLater(() -> listener.accept(services));
                    } catch (Exception e) {
                        if (e.getMessage().contains("user not found")) {
                            // 尝试重新连接
                            try {
                                namingService.shutDown();
                                connect(lastConfig);
                            } catch (Exception ex) {
                                LOGGER.error("重新连接失败", ex);
                            }
                        } else {
                            LOGGER.error("刷新服务列表失败", e);
                        }
                    }
                }
            }
        }, 0, 30000); // 每30秒刷新一次
    }

    /**
     * 订阅服务实例变化
     *
     * @param serviceName 服务名称
     * @param groupName   分组名称
     * @param listener    实例变化监听器
     */
    public void subscribeService(String serviceName, String groupName,
                                 Consumer<List<Instance>> listener) throws Exception {
        if (namingService == null) {
            throw new IllegalStateException("Nacos未连接");
        }

        String key = groupName + "@" + serviceName;
        instanceListeners.put(key, listener);

        namingService.subscribe(serviceName, groupName, event -> {
            if (event instanceof NamingEvent) {
                List<Instance> instances = ((NamingEvent) event).getInstances();
                Platform.runLater(() -> listener.accept(instances));
            }
        });

        LOGGER.info("已订阅服务: {} ({})", serviceName, groupName);
    }

    /**
     * 取消订阅服务
     */
    public void unsubscribeService(String serviceName, String groupName) throws Exception {
        if (namingService == null) {
            return;
        }

        String key = groupName + "@" + serviceName;
        Consumer<List<Instance>> listener = instanceListeners.remove(key);
        if (listener != null) {
            namingService.unsubscribe(serviceName, groupName, event -> {
            });
            LOGGER.info("已取消订阅服务: {} ({})", serviceName, groupName);
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return connected && namingService != null;
    }


    public void shutdown() {
        if (namingService != null) {
            try {
                // 取消所有服务订阅
                for (Map.Entry<String, Consumer<List<Instance>>> entry :
                        new HashMap<>(instanceListeners).entrySet()) {
                    String[] parts = entry.getKey().split("@");
                    unsubscribeService(parts[1], parts[0]);
                }

                serviceListeners.clear();
                instanceListeners.clear();

                namingService.shutDown();
                connected = false;
                LOGGER.info("Nacos连接已关闭");
            } catch (Exception e) {
                LOGGER.error("关闭Nacos连接失败", e);
            }
        }
    }
} 