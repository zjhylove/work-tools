package com.zjhy.love.worktools.plugin.impl;

import com.zjhy.love.worktools.plugin.api.WorkToolsPlugin;
import com.zjhy.love.worktools.view.IpForwardView;
import javafx.scene.Node;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;

public class IpForwardPlugin implements WorkToolsPlugin {
    private IpForwardView view;

    @Override
    public String getId() {
        return "ip-forward";
    }

    @Override
    public String getName() {
        return "IP转发";
    }

    @Override
    public String getDescription() {
        return "IP端口转发配置工具";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getIcon() {
        return MaterialDesignA.ARROW_DECISION_OUTLINE.getDescription();
    }

    @Override
    public Node getView() {
        if (view == null) {
            view = new IpForwardView();
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