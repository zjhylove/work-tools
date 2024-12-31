package com.zjhy.love.worktools.plugin.impl;

import com.zjhy.love.worktools.plugin.api.WorkToolsPlugin;
import com.zjhy.love.worktools.view.ApiDocView;
import javafx.scene.Node;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;

public class ApiDocPlugin implements WorkToolsPlugin {
    private ApiDocView view;
    
    @Override
    public String getId() {
        return "api-doc";
    }
    
    @Override
    public String getName() {
        return "API文档";
    }
    
    @Override
    public String getDescription() {
        return "API接口文档生成工具";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getIcon() {
        return MaterialDesignF.FILE_DOCUMENT_OUTLINE.getDescription();
    }
    
    @Override
    public Node getView() {
        if (view == null) {
            view = new ApiDocView();
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