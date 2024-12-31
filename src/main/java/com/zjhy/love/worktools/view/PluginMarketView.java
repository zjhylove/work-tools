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
import org.controlsfx.glyphfont.Glyph;

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
        
        // 搜索图标
        Glyph searchIcon = new Glyph("FontAwesome", "SEARCH");
        searchIcon.getStyleClass().add("text-subtle");
        
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
        
        // 清除按钮
        Button clearButton = new Button("", new Glyph("FontAwesome", "TIMES"));
        clearButton.getStyleClass().addAll("button-icon", "flat");
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
        private final Label nameLabel;
        private final Label descLabel;
        private final Label versionLabel;
        private final Button actionButton;
        
        public PluginListCell() {
            container = new HBox(15);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(10));

            VBox infoContainer = new VBox(5);
            nameLabel = new Label();
            nameLabel.getStyleClass().add("h4");
            
            descLabel = new Label();
            descLabel.getStyleClass().add("text-subtle");
            descLabel.setWrapText(true);
            
            versionLabel = new Label();
            versionLabel.getStyleClass().addAll("text-subtle", "small");
            
            infoContainer.getChildren().addAll(nameLabel, descLabel, versionLabel);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            actionButton = new Button();
            actionButton.getStyleClass().add("button-outlined");
            
            container.getChildren().addAll(
                new Glyph("FontAwesome", "PUZZLE_PIECE"),
                    infoContainer,
                spacer,
                actionButton
            );
        }
        
        @Override
        protected void updateItem(PluginInfo item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            
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
            
            setGraphic(container);
        }
    }
} 