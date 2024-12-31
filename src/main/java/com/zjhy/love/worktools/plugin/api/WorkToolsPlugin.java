package com.zjhy.love.worktools.plugin.api;

import javafx.scene.Node;

/**
 * 工具插件接口
 * 所有的功能插件都需要实现此接口
 */
public interface WorkToolsPlugin {
    /**
     * 获取插件ID
     */
    String getId();

    /**
     * 获取插件名称
     */
    String getName();

    /**
     * 获取插件描述
     */
    String getDescription();

    /**
     * 获取插件版本
     */
    String getVersion();

    /**
     * 获取插件图标
     */
    String getIcon();

    /**
     * 获取插件视图
     */
    Node getView();

    /**
     * 插件初始化
     */
    default void init() {
    }

    /**
     * 插件销毁
     */
    default void destroy() {
    }
} 