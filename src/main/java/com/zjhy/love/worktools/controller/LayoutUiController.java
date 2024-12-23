package com.zjhy.love.worktools.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.bootstrapfx.scene.layout.Panel;
import com.zjhy.love.worktools.controller.ApiDocFormController;
import com.zjhy.love.worktools.controller.DbDocFormController;
import com.zjhy.love.worktools.controller.IpForwardController;
import com.zjhy.love.worktools.controller.AuthController;
import com.zjhy.love.worktools.controller.ObjectStorageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.Node;

/**
 * @author zhengjun
 */
public class LayoutUiController {
    
    @FXML
    private ListView<MenuItem> menuListView;
    
    @FXML
    private StackPane contentArea;
    
    @FXML
    public void initialize() {
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
            Node content = null;
            switch (selectedItem.getTitle()) {
                case "API文档":
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/api-doc-form.fxml"));
                    content = loader.load();
                    break;
                case "DB文档":
                    content = new DbDocFormController();
                    break;
                case "IP转发":
                    content = new IpForwardController();
                    break;
                case "身份验证":
                    content = new AuthController();
                    break;
                case "对象存储":
                    content = new ObjectStorageController();
                    break;
            }
            
            if (content != null) {
                panel.setBody(content);
                contentArea.getChildren().setAll(panel);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
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
}