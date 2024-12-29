package com.zjhy.love.worktools.view;

import atlantafx.base.theme.*;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.common.util.SystemUtil;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.controlsfx.glyphfont.Glyph;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Scene;

public class LayoutView extends HBox {

    private final StackPane contentArea = new StackPane();
    private final ListView<MenuItem> menuList = new ListView<>();
    
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
        
        // 在组件添加到场景图后初始化通知上下文
        Platform.runLater(() -> {
            Scene scene = getScene();
            if (scene != null && scene.getWindow() instanceof Stage stage) {
                NotificationUtil.initStage(stage);
            }
        });
    }
    
    private void configureMenuList() {
        menuList.getStyleClass().add("dense");
        
        // 创建菜单项
        ObservableList<MenuItem> items = FXCollections.observableArrayList(
            new MenuItem("API文档", "fas-book", "API接口文档导出工具"),
            new MenuItem("DB文档", "fas-database", "数据库文档导出工具"),
            new MenuItem("IP转发", "fas-random", "IP端口转发配置工具"),
            new MenuItem("身份验证", "fas-user-shield", "用户认证与授权工具"),
            new MenuItem("对象存储", "fas-cloud", "对象存储管理工具")
        );
        
        menuList.setItems(items);
        menuList.setCellFactory(view -> new MenuCell());
        
        // 添加选择监听器
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
        
        Button settingsBtn = new Button("", new Glyph("FontAwesome", "GEAR"));
        settingsBtn.getStyleClass().addAll("button-icon", "flat");
        settingsBtn.setOnAction(e -> showSettingsDialog());
        
        Button logBtn = new Button("", new Glyph("FontAwesome", "LIST"));
        logBtn.getStyleClass().addAll("button-icon", "flat");
        logBtn.setOnAction(e -> showLogDialog());
        
        toolbar.getChildren().addAll(settingsBtn, logBtn);
        return toolbar;
    }
    
    private void showLogDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("系统日志");
        dialog.setHeaderText(null);
        
        // 设置对话框大小
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setPrefSize(800, 600);
        dialogPane.getStyleClass().add("surface-card");
        
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
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("设置");
        dialog.setHeaderText(null);
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setPrefSize(500, 400);
        dialogPane.getStyleClass().add("surface-card");
        
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
        minimizeToTrayBox.setSelected(minimizeToTray != null && Boolean.parseBoolean(minimizeToTray));
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
        // 根据选择的菜单项切换内容
        Node content = switch (item.title()) {
            case "API文档" -> new ApiDocView();
            case "DB文档" -> new DbDocView();
            case "IP转发" -> new IpForwardView();
            case "身份验证" -> new AuthView();
            case "对象存储" -> new ObjectStorageView();
            default -> new Placeholder("未实现");
        };
        
        contentArea.getChildren().setAll(content);
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
                setGraphic(container);
            }
        }
    }
    
    // 临时占位组件
    private static class Placeholder extends StackPane {
        public Placeholder(String text) {
            Label label = new Label(text + " (开发中...)");
            label.getStyleClass().addAll("h3", "text-muted");
            getChildren().add(label);
        }
    }
} 