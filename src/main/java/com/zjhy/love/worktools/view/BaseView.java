package com.zjhy.love.worktools.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 基础视图类
 * 提供统一的布局结构
 */
public abstract class BaseView extends VBox {
    
    private final VBox contentBox;
    
    protected BaseView() {
        // 设置外层容器样式
        getStyleClass().add("surface");
        setSpacing(0);
        setPadding(new Insets(20));
        
        // 创建内容容器
        contentBox = new VBox();
        contentBox.setSpacing(15);
        contentBox.setPadding(new Insets(25,0,0,0));
        contentBox.getStyleClass().add("surface-card");
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        
        getChildren().add(contentBox);
    }
    
    /**
     * 添加内容到视图
     */
    protected void addContent(Node... nodes) {
        contentBox.getChildren().addAll(nodes);
    }
    
    /**
     * 获取内容容器
     */
    protected VBox getContentBox() {
        return contentBox;
    }
} 