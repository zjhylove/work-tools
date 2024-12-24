package com.zjhy.love.worktools.common.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局通知工具类
 *
 * @author zhengjun
 */
public class NotificationUtil {

    private static Stage primaryStage;
    private static Popup persistentPopup;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationUtil.class);

    // 定义通知框样式常量
    private static final String NOTIFICATION_STYLE = """
            -fx-background-color: rgba(33, 147, 176, 0.95);
            -fx-padding: 15 30;
            -fx-border-radius: 0 0 5 5;
            -fx-background-radius: 0 0 5 5;
            -fx-border-color: #1a7590;
            -fx-border-width: 0 1 1 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);
            """;

    private static final String LABEL_STYLE = """
            -fx-font-size: 14px;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            """;

    private NotificationUtil() {
    }

    public static void initStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * 显示成功通知
     */
    public static void showSuccess(String message) {
        Platform.runLater(() -> {
            Notifications notifications = Notifications.create()
                    .title("成功")
                    .text(message)
                    .position(Pos.TOP_CENTER)
                    .hideAfter(Duration.seconds(3))
                    .darkStyle();  // 使用深色样式

            if (primaryStage != null) {
                notifications.owner(primaryStage);
            }

            notifications.showInformation();
        });
    }

    /**
     * 显示错误通知
     */
    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Notifications notifications = Notifications.create()
                    .title(title)
                    .text(message)
                    .position(Pos.TOP_CENTER)
                    .hideAfter(Duration.seconds(5))
                    .darkStyle();  // 使用深色样式

            if (primaryStage != null) {
                notifications.owner(primaryStage);
            }

            notifications.showError();
        });
    }

    /**
     * 显示持续通知
     */
    public static void showPersist(String message) {
        Platform.runLater(() -> {
            if (persistentPopup != null) {
                persistentPopup.hide();
            }

            VBox content = new VBox(10);
            content.setAlignment(Pos.CENTER);
            content.setStyle(NOTIFICATION_STYLE);

            Label label = new Label(message);
            label.setStyle(LABEL_STYLE);
            content.getChildren().add(label);

            persistentPopup = new Popup();
            persistentPopup.getContent().add(content);
            persistentPopup.setAutoHide(false);

            if (primaryStage != null && primaryStage.isShowing()) {
                content.applyCss();
                content.layout();
                double centerX = primaryStage.getX() + primaryStage.getWidth() / 2 - content.prefWidth(-1) / 2;

                persistentPopup.show(primaryStage);
                persistentPopup.setX(centerX);
                persistentPopup.setY(primaryStage.getY());

                primaryStage.xProperty().addListener((obs, old, newVal) -> {
                    if (persistentPopup.isShowing()) {
                        persistentPopup.setX(newVal.doubleValue() + primaryStage.getWidth() / 2 - content.getWidth() / 2);
                    }
                });

                primaryStage.yProperty().addListener((obs, old, newVal) -> {
                    if (persistentPopup.isShowing()) {
                        persistentPopup.setY(newVal.doubleValue());
                    }
                });
            }
        });
    }

    /**
     * 隐藏持续通知
     */
    public static void hidePersist() {
        Platform.runLater(() -> {
            if (persistentPopup != null) {
                persistentPopup.hide();
                persistentPopup = null;
            }
        });
    }
} 