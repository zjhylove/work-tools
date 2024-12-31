package com.zjhy.love.worktools.plugin.impl;

import com.zjhy.love.worktools.plugin.api.WorkToolsPlugin;
import com.zjhy.love.worktools.view.AuthView;
import javafx.scene.Node;

public class AuthPlugin implements WorkToolsPlugin {
    private AuthView view;
    
    @Override
    public String getId() {
        return "auth";
    }
    
    @Override
    public String getName() {
        return "身份验证";
    }
    
    @Override
    public String getDescription() {
        return "用户认证与授权工具";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getIcon() {
        return "fas-user-shield";
    }
    
    @Override
    public Node getView() {
        if (view == null) {
            view = new AuthView();
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