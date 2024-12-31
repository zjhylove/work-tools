package com.zjhy.love.worktools.plugin.impl;

import com.zjhy.love.worktools.plugin.api.WorkToolsPlugin;
import com.zjhy.love.worktools.view.DbDocView;
import javafx.scene.Node;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;

public class DbDocPlugin implements WorkToolsPlugin {
    private DbDocView view;

    @Override
    public String getId() {
        return "db-doc";
    }

    @Override
    public String getName() {
        return "DB文档";
    }

    @Override
    public String getDescription() {
        return "数据库文档导出工具";
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
            view = new DbDocView();
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