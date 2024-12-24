package com.zjhy.love.worktools.controller;

import com.zjhy.love.worktools.common.util.NotificationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author zhengjun
 */
public class LayoutUiController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LayoutUiController.class);
    
    @FXML
    private ListView<MenuItem> menuListView;
    
    @FXML
    private StackPane contentArea;
    
    private Stage logStage;
    
    private IpForwardController ipForwardController;
    
    @FXML
    public void initialize() {
        LOGGER.info("初始化菜单信息");
        // 初始化菜单列表
        ObservableList<MenuItem> menuItems = FXCollections.observableArrayList(
            new MenuItem("API文档", "fas-book", "API接口文档管理与查看工具"),
            new MenuItem("DB文档", "fas-database", "数据库表结构文档管理工具"),
            new MenuItem("IP转发", "fas-random", "IP端口转发配置工具"),
            new MenuItem("身份验证", "fas-user-shield", "用户认证与授权管理工具"),
            new MenuItem("对象存储", "fas-cloud", "对象存储服务管理工具")
        );
        
        menuListView.setItems(menuItems);
        menuListView.setCellFactory(param -> new MenuItemCell());
        
        // 添加菜单选择事件监听
        menuListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleMenuSelection(newValue);
            }
        });

        // 默认选择第一个菜单项
        Platform.runLater(() -> {
            menuListView.getSelectionModel().select(0);
        });
    }
    
    private void handleMenuSelection(MenuItem selectedItem) {
        Panel panel = new Panel();
        panel.getStyleClass().add("panel-primary");
        Label title = new Label(selectedItem.getTitle());
        title.getStyleClass().add("h3");
        panel.setHeading(title);
        
        try {
            Node content = switch (selectedItem.getTitle()) {
                case "API文档" -> {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/api-doc-form.fxml"));
                    yield loader.load();
                }
                case "DB文档" -> {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/db-doc-form.fxml"));
                    yield loader.load();
                }
                case "IP转发" -> {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ip-forward.fxml"));
                    yield loader.load();
                }
                case "身份验证" -> new AuthController();
                case "对象存储" -> new ObjectStorageController();
                default -> null;
            };

            if (content != null) {
                panel.setBody(content);
                contentArea.getChildren().setAll(panel);
            }
            
        } catch (IOException e) {
            LOGGER.error("加载菜单出错",e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("加载表单失败：" + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * 菜单项数据类
     */
    public static class MenuItem {
        private final String title;
        private final String icon;
        private final String description;
        
        public MenuItem(String title, String icon, String description) {
            this.title = title;
            this.icon = icon;
            this.description = description;
        }
        
        public String getTitle() { return title; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
    }

    /**
     * 自定义菜单项单元格
     */
    private static class MenuItemCell extends ListCell<MenuItem> {
        private final HBox content;
        private final Label title;
        private final Label description;
        
        public MenuItemCell() {
            content = new HBox(10);
            title = new Label();
            title.getStyleClass().add("h5");
            description = new Label();
            description.getStyleClass().addAll("text-muted", "small");
            description.setWrapText(true);
            
            VBox vbox = new VBox(5);
            vbox.getChildren().addAll(title, description);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            
            content.getChildren().addAll(vbox);
            content.setPadding(new Insets(5, 10, 5, 10));
        }
        
        @Override
        protected void updateItem(MenuItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setGraphic(null);
            } else {
                title.setText(item.getTitle());
                description.setText(item.getDescription());
                setGraphic(content);
            }
        }
    }
    
    @FXML
    private void handleNew() {
        // 处理新建操作
    }
    
    @FXML
    private void handleSave() {
        // 处理保存操作
    }
    
    @FXML
    private void handleSettings() {
        // 处理设置操作
    }
    
    @FXML
    private void handleShowLogs() {
        if (logStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/log-view.fxml"));
                Scene scene = new Scene(loader.load(), 800, 600);
                scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
                
                logStage = new Stage();
                logStage.setTitle("日志查看");
                logStage.setScene(scene);
                logStage.initModality(Modality.NONE);
                logStage.initOwner(contentArea.getScene().getWindow());
                
                // 当日志窗口关闭时，重置logStage
                logStage.setOnHidden(event -> logStage = null);
            } catch (IOException e) {
                LOGGER.error("打开日志窗口失败", e);
                NotificationUtil.showError("错误", "打开日志窗口失败: " + e.getMessage());
                return;
            }
        }
        
        logStage.show();
        logStage.toFront();
    }

    /**
     * 获取视图加载器
     */
    private FXMLLoader getViewLoader(String viewName) {
        try {
            String fxmlPath = "/view/" + viewName + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            // 预加载FXML以获取控制器
            loader.load();
            return loader;
        } catch (IOException e) {
            LOGGER.error("加载视图失败: " + viewName, e);
            NotificationUtil.showError("错误", "加载视图失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取IP转发控制器
     */
    public IpForwardController getIpForwardController() {
        if (ipForwardController == null) {
            FXMLLoader loader = getViewLoader("ip-forward");
            if (loader != null) {
                ipForwardController = loader.getController();
            }
        }
        return ipForwardController;
    }
}