package com.zjhy.love.worktools.plugin.impl;

import com.zjhy.love.worktools.plugin.api.WorkToolsPlugin;
import com.zjhy.love.worktools.view.ObjectStorageView;
import javafx.scene.Node;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

public class ObjectStoragePlugin implements WorkToolsPlugin {
    private ObjectStorageView view;

    @Override
    public String getId() {
        return "object-storage";
    }

    @Override
    public String getName() {
        return "对象存储";
    }

    @Override
    public String getDescription() {
        return "对象存储管理工具";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getIcon() {
        return MaterialDesignC.CLOUD_OUTLINE.getDescription();
    }

    @Override
    public Node getView() {
        if (view == null) {
            view = new ObjectStorageView();
        }
        return view;
    }

    @Override
    public void destroy() {
        if (view != null) {
            view = null;
        }
    }
} 