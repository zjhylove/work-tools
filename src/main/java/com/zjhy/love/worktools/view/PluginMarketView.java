package com.zjhy.love.worktools.view;

import atlantafx.base.theme.Styles;
import com.zjhy.love.worktools.plugin.PluginManager;
import com.zjhy.love.worktools.plugin.model.PluginInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * 插件市场视图
 */
public class PluginMarketView extends VBox {
    private final PluginManager pluginManager;
    private final ListView<PluginInfo> pluginListView;
    private final ObservableList<PluginInfo> pluginList = FXCollections.observableArrayList();
    private final FilteredList<PluginInfo> filteredPlugins;
    private Runnable onPluginStateChanged;

    public PluginMarketView(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        this.pluginListView = new ListView<>();
        this.filteredPlugins = new FilteredList<>(pluginList);

        initView();
        loadPlugins();
    }

    private void initView() {
        setPadding(new Insets(10));
        setSpacing(10);

        // 标题
        Label titleLabel = new Label("可用插件");
        titleLabel.getStyleClass().add("h3");

        // 搜索区域
        HBox searchBox = createSearchBox();

        // 插件列表
        pluginListView.setCellFactory(param -> new PluginListCell());
        pluginListView.setItems(filteredPlugins);
        VBox.setVgrow(pluginListView, Priority.ALWAYS);

        getChildren().addAll(titleLabel, searchBox, pluginListView);
    }

    private HBox createSearchBox() {
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 5, 0, 5));

        // 搜索图标 - 使用 MaterialDesign 图标
        FontIcon searchIcon = new FontIcon(MaterialDesignM.MAGNIFY);
        searchIcon.getStyleClass().addAll("text-subtle", "icon-16");

        // 搜索输入框
        TextField searchField = new TextField();
        searchField.setPromptText("搜索插件...");
        searchField.setPrefHeight(32);
        searchField.getStyleClass().addAll(Styles.FLAT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // 实现搜索过滤
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            String searchText = newText.toLowerCase().trim();
            if (searchText.isEmpty()) {
                filteredPlugins.setPredicate(null);
            } else {
                filteredPlugins.setPredicate(createSearchPredicate(searchText));
            }
        });

        // 清除按钮 - 使用 MaterialDesign 图标
        Button clearButton = new Button("", new FontIcon(MaterialDesignC.CLOSE));
        clearButton.getStyleClass().addAll("button-icon", "flat");
        ((FontIcon) clearButton.getGraphic()).getStyleClass().add("icon-16");
        clearButton.setVisible(false);
        clearButton.setOnAction(e -> searchField.clear());

        // 当搜索框有内容时显示清除按钮
        searchField.textProperty().addListener((obs, old, text) ->
                clearButton.setVisible(!text.isEmpty())
        );

        searchBox.getChildren().addAll(searchIcon, searchField, clearButton);
        return searchBox;
    }

    private Predicate<PluginInfo> createSearchPredicate(String searchText) {
        return plugin -> {
            if (plugin.getName().toLowerCase().contains(searchText)) {
                return true;
            }
            if (plugin.getDescription().toLowerCase().contains(searchText)) {
                return true;
            }
            return plugin.getId().toLowerCase().contains(searchText);
        };
    }

    private void loadPlugins() {
        Collection<PluginInfo> availablePlugins = pluginManager.getAvailablePlugins();
        pluginList.setAll(availablePlugins);
    }

    public void setOnPluginStateChanged(Runnable callback) {
        this.onPluginStateChanged = callback;
    }

    private class PluginListCell extends ListCell<PluginInfo> {
        private final HBox container;
        private final VBox infoContainer;
        private final Label nameLabel;
        private final Label descLabel;
        private final Label versionLabel;
        private final Button actionButton;
        private final Region spacer;

        public PluginListCell() {
            container = new HBox(15);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(10));

            // 创建信息容器
            infoContainer = new VBox(5);
            nameLabel = new Label();
            nameLabel.getStyleClass().add("h4");

            descLabel = new Label();
            descLabel.getStyleClass().add("text-subtle");
            descLabel.setWrapText(true);

            versionLabel = new Label();
            versionLabel.getStyleClass().addAll("text-subtle", "small");

            infoContainer.getChildren().addAll(nameLabel, descLabel, versionLabel);

            spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            actionButton = new Button();
            actionButton.getStyleClass().add("button-outlined");

            // 初始时不添加任何子节点，在updateItem中再添加
            container.getChildren().clear();
        }

        @Override
        protected void updateItem(PluginInfo item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            // 获取对应的插件实现类
            PluginInfo plugin = pluginManager.getPluginById(item.getId());

            // 创建图标
            FontIcon icon;
            if (plugin != null && plugin.getIcon() != null) {
                // 使用插件定义的图标
                icon = new FontIcon(plugin.getIcon());
            } else {
                // 使用默认图标
                icon = new FontIcon(MaterialDesignP.PUZZLE_OUTLINE);
            }
            icon.getStyleClass().add("icon-24");  // 使用稍大的图标尺寸

            nameLabel.setText(item.getName());
            descLabel.setText(item.getDescription());
            versionLabel.setText("版本: " + item.getVersion());

            actionButton.setText(item.isInstalled() ? "卸载" : "安装");
            actionButton.setOnAction(e -> {
                if (item.isInstalled()) {
                    pluginManager.uninstallPlugin(item.getId());
                } else {
                    pluginManager.installPlugin(item.getId());
                }

                // 立即刷新按钮状态
                actionButton.setText(item.isInstalled() ? "卸载" : "安装");

                // 刷新整个列表视图
                loadPlugins();

                // 通知外部状态变化
                if (onPluginStateChanged != null) {
                    onPluginStateChanged.run();
                }
            });

            container.getChildren().setAll(icon, infoContainer, spacer, actionButton);
            setGraphic(container);
        }
    }
} 