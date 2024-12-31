package com.zjhy.love.worktools.plugin;

import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.plugin.api.WorkToolsPlugin;
import com.zjhy.love.worktools.plugin.model.PluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件管理器
 * 负责插件的加载、安装、卸载等生命周期管理
 */
public class PluginManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);
    private static final String PLUGIN_HISTORY_KEY = "installed_plugins";
    
    // 已加载的插件实例
    private final Map<String, WorkToolsPlugin> loadedPlugins = new ConcurrentHashMap<>();
    // 可用的插件信息
    private final Map<String, PluginInfo> availablePlugins = new ConcurrentHashMap<>();
    
    /**
     * 初始化插件管理器
     */
    public void init() {
        try {
            // 1. 通过SPI机制加载所有可用插件
            ServiceLoader<WorkToolsPlugin> loader = ServiceLoader.load(WorkToolsPlugin.class);
            for (WorkToolsPlugin plugin : loader) {
                PluginInfo info = createPluginInfo(plugin);
                availablePlugins.put(info.getId(), info);
                LOGGER.info("发现插件: {}", info.getName());
            }
            
            // 2. 加载已安装插件的历史记录
            List<String> installedPluginIds = HistoryUtil.getHistory(PLUGIN_HISTORY_KEY, List.class);
            if (installedPluginIds != null) {
                installedPluginIds.forEach(this::installPlugin);
            }
            
            LOGGER.info("插件管理器初始化完成，共发现 {} 个插件", availablePlugins.size());
        } catch (Exception e) {
            LOGGER.error("插件管理器初始化失败", e);
            NotificationUtil.showError("插件加载失败", e.getMessage());
        }
    }
    
    /**
     * 安装插件
     */
    public void installPlugin(String pluginId) {
        try {
            PluginInfo info = availablePlugins.get(pluginId);
            if (info == null) {
                throw new IllegalArgumentException("插件不存在: " + pluginId);
            }
            
            if (info.isInstalled()) {
                LOGGER.warn("插件已安装: {}", pluginId);
                return;
            }
            
            // 通过SPI重新加载插件实例
            ServiceLoader<WorkToolsPlugin> loader = ServiceLoader.load(WorkToolsPlugin.class);
            for (WorkToolsPlugin plugin : loader) {
                if (plugin.getId().equals(pluginId)) {
                    // 初始化插件
                    plugin.init();
                    // 保存插件实例
                    loadedPlugins.put(pluginId, plugin);
                    // 更新插件状态
                    info.setInstalled(true);
                    // 保存安装记录
                    saveInstalledPlugins();
                    
                    LOGGER.info("插件安装成功: {}", info.getName());
                    NotificationUtil.showSuccess("插件安装成功", "已安装 " + info.getName());
                    return;
                }
            }
            
            throw new IllegalStateException("未找到插件实现类: " + pluginId);
        } catch (Exception e) {
            LOGGER.error("插件安装失败: {}", pluginId, e);
            NotificationUtil.showError("插件安装失败", e.getMessage());
        }
    }
    
    /**
     * 卸载插件
     */
    public void uninstallPlugin(String pluginId) {
        try {
            WorkToolsPlugin plugin = loadedPlugins.get(pluginId);
            if (plugin == null) {
                LOGGER.warn("插件未安装: {}", pluginId);
                return;
            }
            
            // 销毁插件
            plugin.destroy();
            // 移除插件实例
            loadedPlugins.remove(pluginId);
            // 更新插件状态
            PluginInfo info = availablePlugins.get(pluginId);
            if (info != null) {
                info.setInstalled(false);
            }
            // 保存安装记录
            saveInstalledPlugins();
            
            LOGGER.info("插件卸载成功: {}", info.getName());
            NotificationUtil.showSuccess("插件卸载成功", "已卸载 " + info.getName());
        } catch (Exception e) {
            LOGGER.error("插件卸载失败: {}", pluginId, e);
            NotificationUtil.showError("插件卸载失败", e.getMessage());
        }
    }
    
    /**
     * 获取所有可用插件信息
     */
    public Collection<PluginInfo> getAvailablePlugins() {
        return availablePlugins.values();
    }
    
    /**
     * 获取所有已加载的插件实例
     */
    public Collection<WorkToolsPlugin> getLoadedPlugins() {
        return loadedPlugins.values();
    }
    
    /**
     * 保存已安装插件记录
     */
    private void saveInstalledPlugins() {
        List<String> installedPluginIds = new ArrayList<>(loadedPlugins.keySet());
        HistoryUtil.saveHistory(PLUGIN_HISTORY_KEY, installedPluginIds);
    }
    
    /**
     * 创建插件信息对象
     */
    private PluginInfo createPluginInfo(WorkToolsPlugin plugin) {
        PluginInfo info = new PluginInfo();
        info.setId(plugin.getId());
        info.setName(plugin.getName());
        info.setDescription(plugin.getDescription());
        info.setVersion(plugin.getVersion());
        info.setIcon(plugin.getIcon());
        info.setInstalled(false);
        return info;
    }
    
    /**
     * 关闭插件管理器
     */
    public void shutdown() {
        // 销毁所有已加载的插件
        loadedPlugins.values().forEach(plugin -> {
            try {
                plugin.destroy();
            } catch (Exception e) {
                LOGGER.error("插件销毁失败: {}", plugin.getId(), e);
            }
        });
        
        // 清空插件列表
        loadedPlugins.clear();
        availablePlugins.clear();
        
        LOGGER.info("插件管理器已关闭");
    }
    
    /**
     * 根据插件ID获取已加载的插件实例
     *
     * @param id 插件ID
     * @return 插件实例，如果未找到则返回null
     */
    public PluginInfo getPluginById(String id) {
        return availablePlugins.get(id);
    }
} 