package com.zjhy.love.worktools.view;

import atlantafx.base.theme.*;
import com.zjhy.love.worktools.common.ShutdownHook;
import com.zjhy.love.worktools.common.util.DialogUtil;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.common.util.SystemUtil;
import com.zjhy.love.worktools.plugin.PluginManager;
import com.zjhy.love.worktools.plugin.api.WorkToolsPlugin;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LayoutView extends HBox {

    private final StackPane contentArea = new StackPane();
    private final ListView<MenuItem> menuList = new ListView<>();
    private final List<WeakReference<ShutdownHook>> shutdownList = new ArrayList<>();
    private final PluginManager pluginManager = new PluginManager();
    
    public LayoutView() {
        // 基本设置
        setSpacing(0);
        setPadding(new Insets(0));
        getStyleClass().add("surface");
        
        // 创建侧边栏容器
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(250);
        sidebar.getStyleClass().add("surface-card");
        sidebar.setPadding(new Insets(15));
        
        // 创建标题
        Label title = new Label("Work Tools");
        title.getStyleClass().addAll("h3", "accent");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        
        // 初始化插件管理器
        pluginManager.init();
        
        // 配置菜单列表
        configureMenuList();
        VBox.setVgrow(menuList, Priority.ALWAYS);
        
        // 创建底部工具栏
        HBox toolbar = createToolbar();
        
        // 组装侧边栏
        sidebar.getChildren().addAll(title, menuList, toolbar);
        
        // 配置内容区域
        contentArea.getStyleClass().add("surface");
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        
        // 组装布局
        getChildren().addAll(sidebar, contentArea);
        
        // 设置最小高度，确保有足够的空间
        setMinHeight(600);
    }

    private void configureMenuList() {
        menuList.getStyleClass().add("dense");
        
        // 从插件管理器获取已安装的插件创建菜单项
        ObservableList<MenuItem> items = FXCollections.observableArrayList();
        for (WorkToolsPlugin plugin : pluginManager.getLoadedPlugins()) {
            items.add(new MenuItem(
                plugin.getName(),
                plugin.getIcon() != null ? plugin.getIcon() : "fas-cube",
                plugin.getDescription()
            ));
        }
        
        menuList.setItems(items);
        menuList.setCellFactory(view -> new MenuCell());
        
        menuList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    handleMenuSelection(newVal);
                }
            }
        );
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER);
        
        // 设置按钮 - 使用齿轮图标
        Button settingsBtn = new Button("", new FontIcon(MaterialDesignC.COG_OUTLINE));
        settingsBtn.getStyleClass().addAll("button-icon", "flat");
        settingsBtn.setOnAction(e -> showSettingsDialog());
        
        // 日志按钮 - 使用文档列表图标
        Button logBtn = new Button("", new FontIcon(MaterialDesignT.TEXT_BOX_OUTLINE));
        logBtn.getStyleClass().addAll("button-icon", "flat");
        logBtn.setOnAction(e -> showLogDialog());
        
        // 插件市场按钮 - 使用插件图标
        Button pluginMarketBtn = new Button("", new FontIcon(MaterialDesignP.PUZZLE_OUTLINE));
        pluginMarketBtn.getStyleClass().addAll("button-icon", "flat");
        pluginMarketBtn.setOnAction(e -> showPluginMarketDialog());
        
        // 设置图标大小
        settingsBtn.getGraphic().getStyleClass().add("icon-16");
        logBtn.getGraphic().getStyleClass().add("icon-16");
        pluginMarketBtn.getGraphic().getStyleClass().add("icon-16");
        
        toolbar.getChildren().addAll(settingsBtn, logBtn, pluginMarketBtn);
        return toolbar;
    }
    
    private void showLogDialog() {
        Dialog<Void> dialog = DialogUtil.createCommonDataDialog("系统日志");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setPrefSize(800, 600);

        // 创建日志视图
        LogView logView = new LogView();
        
        // 添加关闭按钮
        ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeButton);
        
        // 设置按钮样式
        Button closeBtn = (Button) dialogPane.lookupButton(closeButton);
        closeBtn.getStyleClass().add(Styles.BUTTON_OUTLINED);
        
        // 设置内容
        dialogPane.setContent(logView);
        
        // 显示对话框
        dialog.show();
    }
    
    private void showSettingsDialog() {
        Dialog<Void> dialog = DialogUtil.createCommonDataDialog("设置");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setPrefSize(500, 400);
        
        // 创建设置内容
        TabPane settingsPane = new TabPane();
        settingsPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // 外观设置选项卡
        Tab appearanceTab = new Tab("外观");
        VBox appearanceContent = new VBox(15);
        appearanceContent.setPadding(new Insets(20));
        
        // 主题设置
        ComboBox<String> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(
            "Primer Light",
            "Primer Dark",
            "Nord Light",
            "Nord Dark",
            "Cupertino Light",
            "Cupertino Dark",
            "Dracula"
        );
        
        // 获取当前主题
        String currentTheme = HistoryUtil.getHistory("theme", String.class);
        themeComboBox.setValue(currentTheme != null ? currentTheme : "Primer Light");
        
        // 主题实时预览
        themeComboBox.setOnAction(e -> {
            applyTheme(dialogPane, themeComboBox.getValue());
            HistoryUtil.saveHistory("theme", themeComboBox.getValue());
        });
        
        appearanceContent.getChildren().addAll(
            createSettingGroup("主题设置", 
                createSettingItem("主题样式", themeComboBox)
            )
        );
        appearanceTab.setContent(appearanceContent);
        
        // 通用设置选项卡
        Tab generalTab = new Tab("通用");
        VBox generalContent = new VBox(15);
        generalContent.setPadding(new Insets(20));
        
        // 添加一些通用设置项
        CheckBox autoStartBox = new CheckBox();
        autoStartBox.setSelected(SystemUtil.isAutoStartEnabled());
        autoStartBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            SystemUtil.setAutoStart(newVal);
        });
        
        CheckBox minimizeToTrayBox = new CheckBox();
        String minimizeToTray = HistoryUtil.getHistory("minimizeToTray", String.class);
        minimizeToTrayBox.setSelected(Boolean.parseBoolean(minimizeToTray));
        minimizeToTrayBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            HistoryUtil.saveHistory("minimizeToTray", newVal ? "true" : "false");
            NotificationUtil.showSuccess("设置已保存", "最小化到系统托盘已" + (newVal ? "启用" : "禁用"));
        });
        
        generalContent.getChildren().addAll(
            createSettingGroup("启动选项",
                createSettingItem("开机自动启动", autoStartBox),
                createSettingItem("最小化到系统托盘", minimizeToTrayBox)
            )
        );
        generalTab.setContent(generalContent);
        
        // 添加选项卡
        settingsPane.getTabs().addAll(appearanceTab, generalTab);
        
        // 设置内容
        dialogPane.setContent(settingsPane);
        
        // 添加关闭按钮
        ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeButton);
        Button closeBtn = (Button) dialogPane.lookupButton(closeButton);
        closeBtn.getStyleClass().add(Styles.BUTTON_OUTLINED);
        
        // 显示对话框
        dialog.show();

    }
    
    // 创建设置组
    private VBox createSettingGroup(String title, Node... items) {
        VBox group = new VBox(10);
        group.getStyleClass().add("setting-group");
        
        Label groupTitle = new Label(title);
        groupTitle.getStyleClass().add("setting-group-title");
        
        VBox itemsContainer = new VBox(8);
        itemsContainer.getChildren().addAll(items);
        itemsContainer.setPadding(new Insets(0, 0, 0, 20));
        
        group.getChildren().addAll(groupTitle, itemsContainer);
        return group;
    }
    
    // 创建设置项
    private HBox createSettingItem(String label, Node control) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Label itemLabel = new Label(label);
        itemLabel.setMinWidth(100);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        item.getChildren().addAll(itemLabel, spacer, control);
        return item;
    }
    
    private void applyTheme(DialogPane dialogPane, String themeName) {
        Application.setUserAgentStylesheet(getThemeStylesheet(themeName));
        NotificationUtil.showSuccess("主题已更改", "已切换到 " + themeName);
    }
    
    public static String getThemeStylesheet(String themeName) {
        Theme theme = switch (themeName) {
            case "Primer Light" -> new PrimerLight();
            case "Primer Dark" -> new PrimerDark();
            case "Nord Light" -> new NordLight();
            case "Nord Dark" -> new NordDark();
            case "Cupertino Light" -> new CupertinoLight();
            case "Cupertino Dark" -> new CupertinoDark();
            case "Dracula" -> new Dracula();
            default -> new PrimerLight();
        };
        return theme.getUserAgentStylesheet();
    }
    
    private void handleMenuSelection(MenuItem item) {
        WorkToolsPlugin plugin = pluginManager.getLoadedPlugins().stream()
            .filter(p -> p.getName().equals(item.title()))
            .findFirst()
            .orElse(null);
            
        if (plugin != null) {
            Node content = plugin.getView();
            if (content instanceof ShutdownHook) {
                shutdownList.add(new WeakReference<>((ShutdownHook) content));
            }
            contentArea.getChildren().setAll(content);
        }
    }
    
    // 菜单项数据类
    private record MenuItem(String title, String icon, String description) {}
    
    // 菜单项单元格
    private static class MenuCell extends ListCell<MenuItem> {
        private final HBox container;
        private final Label titleLabel;
        private final Label descLabel;
        
        public MenuCell() {
            container = new HBox(10);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(5, 10, 5, 10));
            
            VBox textContainer = new VBox(2);
            titleLabel = new Label();
            titleLabel.getStyleClass().add("h4");
            
            descLabel = new Label();
            descLabel.getStyleClass().addAll("text-subtle", "small");
            descLabel.setWrapText(true);
            
            textContainer.getChildren().addAll(titleLabel, descLabel);
            container.getChildren().add(textContainer);
        }
        
        @Override
        protected void updateItem(MenuItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setGraphic(null);
            } else {
                titleLabel.setText(item.title());
                descLabel.setText(item.description());
                // 清除旧的图标
                container.getChildren().removeIf(node -> node instanceof FontIcon);
                // 添加新的图标
                if (item.icon() != null) {
                    FontIcon icon = new FontIcon(item.icon());
                    icon.getStyleClass().add("icon-16");
                    container.getChildren().addFirst(icon);  // 将图标添加到最前面
                }
                setGraphic(container);
            }
        }
    }

    public void doShutDown() {
        shutdownList.forEach(s -> {
            ShutdownHook shutdownHook = s.get();
            if (Objects.nonNull(shutdownHook)) {
                shutdownHook.shutdown();
            }
            s.clear();
        });
    }

    private void showPluginMarketDialog() {
        Dialog<Void> dialog = DialogUtil.createCommonDataDialog("插件市场");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setPrefSize(600, 400);
        
        // 创建插件市场视图
        PluginMarketView marketView = new PluginMarketView(pluginManager);
        marketView.setOnPluginStateChanged(this::refreshMenuList);
        
        dialogPane.setContent(marketView);
        
        // 添加关闭按钮
        ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().add(closeButton);
        Button closeBtn = (Button) dialogPane.lookupButton(closeButton);
        closeBtn.getStyleClass().add(Styles.BUTTON_OUTLINED);
        
        dialog.show();
    }

    private void refreshMenuList() {
        // 获取当前显示的插件视图
        WorkToolsPlugin currentPlugin = null;
        if (!contentArea.getChildren().isEmpty()) {
            Node currentView = contentArea.getChildren().get(0);
            currentPlugin = pluginManager.getLoadedPlugins().stream()
                    .filter(p -> p.getView() == currentView)
                    .findFirst()
                    .orElse(null);
        }

        // 如果当前显示的插件已被卸载，清空内容区域
        if (currentPlugin == null && !contentArea.getChildren().isEmpty()) {
            contentArea.getChildren().clear();
        }

        // 刷新菜单列表
        configureMenuList();
    }
} 