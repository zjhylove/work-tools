package com.zjhy.love.worktools.common.util;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignI;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 通知工具类
 * 提供统一的通知消息显示功能，支持成功、警告、错误等不同类型的通知
 * 支持通知的动画效果、自动消失和持久化显示
 *
 * @author zhengjun
 */
public class NotificationUtil {
    /**
     * 日志记录器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationUtil.class);

    /**
     * 通知面板
     * 用于存放和管理所有通知组件
     */
    private static StackPane notificationPane;

    /**
     * 活动通知列表
     * 存储当前显示的所有通知
     */
    private static final List<Notification> activeNotifications = new ArrayList<>();

    /**
     * 持久化通知的样式类名
     */
    private static final String PERSIST_STYLE = "notification-persist";


    private NotificationUtil() {}
    
    /**
     * 初始化通知工具
     * 设置主舞台引用，用于确定通知显示位置
     *
     * @param stage 主舞台实例
     */
    public static void initStage(Stage stage) {

        // 创建通知容器
        notificationPane = new StackPane();
        notificationPane.setPickOnBounds(false);
        notificationPane.setPrefWidth(350);
        notificationPane.setMouseTransparent(true);
        
        // 获取主窗口的根节点
        Scene scene = stage.getScene();
        Node originalRoot = scene.getRoot();
        
        // 创建新的根容器
        StackPane newRoot = new StackPane();
        newRoot.getChildren().addAll(originalRoot, notificationPane);
        
        // 设置新的根节点
        scene.setRoot(newRoot);
    }
    
    /**
     * 显示通知
     * 创建并显示指定类型的通知消息
     *
     * @param title 通知标题
     * @param message 通知内容
     * @param type 通知类型
     */
    private static void showNotification(String title, String message, NotificationType type) {
        if (notificationPane == null) return;
        
        Platform.runLater(() -> {
            // 创建通知
            Notification notification = new Notification(message);
            notification.setGraphic(createIcon(type));
            notification.getStyleClass().addAll("surface-card", type.styleClass);
            
            // 设置大小
            notification.setPrefWidth(350);
            notification.setPrefHeight(Region.USE_PREF_SIZE);
            notification.setMaxHeight(Region.USE_PREF_SIZE);
            
            // 设置位置
            StackPane.setAlignment(notification, Pos.TOP_RIGHT);
            StackPane.setMargin(notification, new Insets(10 + activeNotifications.size() * 90, 10, 0, 0));
            
            // 添加关闭事件
            notification.setOnClose(e -> hideNotification(notification));
            
            // 显示通知
            showNotification(notification);
            
            // 设置自动隐藏
            if (type.duration != null) {
                Timeline timeline = new Timeline(
                    new KeyFrame(type.duration, e -> hideNotification(notification))
                );
                timeline.play();
            }
        });
    }
    
    /**
     * 显示单个通知
     * 将通知添加到活动列表并播放显示动画
     *
     * @param notification 要显示的通知实例
     */
    private static void showNotification(Notification notification) {
        // 添加到活动通知列表
        activeNotifications.add(notification);
        
        // 添加到通知面板并播放动画
        notificationPane.getChildren().add(notification);
        playShowAnimation(notification);
    }
    
    /**
     * 隐藏通知
     * 播放隐藏动画并从活动列表中移除
     *
     * @param notification 要隐藏的通知实例
     */
    private static void hideNotification(Notification notification) {
        // 播放隐藏动画
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(250),
                new KeyValue(notification.translateYProperty(), -notification.getHeight()),
                new KeyValue(notification.opacityProperty(), 0)
            )
        );
        
        timeline.setOnFinished(e -> {
            notificationPane.getChildren().remove(notification);
            activeNotifications.remove(notification);
            
            // 调整其他通知的位置
            for (int i = 0; i < activeNotifications.size(); i++) {
                Notification n = activeNotifications.get(i);
                StackPane.setMargin(n, new Insets(10 + i * 90, 10, 0, 0));
            }
        });
        
        timeline.play();
    }
    
    /**
     * 播放显示动画
     * 通知出现时的渐入动画效果
     *
     * @param notification 要播放动画的通知实例
     */
    private static void playShowAnimation(Notification notification) {
        notification.setTranslateY(-notification.getHeight());
        notification.setOpacity(0);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(250),
                new KeyValue(notification.translateYProperty(), 0),
                new KeyValue(notification.opacityProperty(), 1)
            )
        );
        timeline.play();
    }
    
    /**
     * 创建通知图标
     * 根据通知类型创建对应的图标
     *
     * @param type 通知类型
     * @return 对应类型的图标实例
     */
    private static Node createIcon(NotificationType type) {
        FontIcon icon = switch (type) {
            case SUCCESS -> new FontIcon(MaterialDesignC.CHECK_CIRCLE);
            case ERROR -> new FontIcon(MaterialDesignA.ALERT_CIRCLE);
            case WARNING -> new FontIcon(MaterialDesignA.ALERT);
        };
        
        icon.setIconSize(24);
        icon.getStyleClass().add(type.styleClass);
        return icon;
    }
    
    /**
     * 显示确认对话框
     * 显示带有确认和取消按钮的对话框
     *
     * @param title 对话框标题
     * @param message 对话框内容
     * @return true表示用户点击确认，false表示用户点击取消或关闭对话框
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = createAlert(title, message);

        // 设置按钮样式
        ButtonType okButton = ButtonType.OK;
        ButtonType cancelButton = ButtonType.CANCEL;
        alert.getButtonTypes().setAll(okButton, cancelButton);

        alert.getDialogPane().lookupButton(okButton)
            .getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        alert.getDialogPane().lookupButton(cancelButton)
            .getStyleClass().add(Styles.BUTTON_OUTLINED);

        // 显示对话框并等待结果
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * 显示自定义按钮文本的确认对话框
     *
     * @param title 对话框标题
     * @param message 对话框内容
     * @param okText 确认按钮文本
     * @param cancelText 取消按钮文本
     * @return true表示用户点击确认按钮，false表示用户点击取消按钮或关闭对话框
     */
    public static boolean showConfirm(String title, String message, String okText, String cancelText) {
        Alert alert = createAlert(title, message);

        // 创建自定义按钮
        ButtonType okButton = new ButtonType(okText);
        ButtonType cancelButton = new ButtonType(cancelText);
        alert.getButtonTypes().setAll(okButton, cancelButton);
        
        // 设置按钮样式
        alert.getDialogPane().lookupButton(okButton)
            .getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        alert.getDialogPane().lookupButton(cancelButton)
            .getStyleClass().add(Styles.BUTTON_OUTLINED);
        
        // 显示对话框并等待结果
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == okButton;
    }

    /**
     * 创建统一样式的Alert对话框
     */
    private static Alert createAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 设置对话框样式
        alert.getDialogPane().getStyleClass().add("surface-card");
        
        // 设置对话框图标
        FontIcon icon = new FontIcon(MaterialDesignH.HELP_CIRCLE);
        icon.setIconSize(32);
        icon.getStyleClass().add(Styles.ACCENT);
        alert.setGraphic(icon);
        
        return alert;
    }

    /**
     * 通知类型枚举
     * 定义不同类型通知的显示时长和样式
     */
    private enum NotificationType {
        SUCCESS(Duration.seconds(3), Styles.SUCCESS),
        ERROR(Duration.seconds(5), Styles.DANGER),
        WARNING(Duration.seconds(4), Styles.WARNING);
        
        final Duration duration;
        final String styleClass;
        
        NotificationType(Duration duration, String styleClass) {
            this.duration = duration;
            this.styleClass = styleClass;
        }
    }
    
    /**
     * 显示成功通知
     *
     * @param message 通知内容
     */
    public static void showSuccess(String message) {
        showNotification("成功", message, NotificationType.SUCCESS);
    }

    /**
     * 显示成功通知
     *
     * @param message 通知内容
     */
    public static void showSuccess(String title, String message) {
        showNotification(title, message, NotificationType.SUCCESS);
    }
    
    /**
     * 显示警告通知
     *
     * @param title 通知标题
     * @param message 通知内容
     */
    public static void showWarning(String title, String message) {
        showNotification(title, message, NotificationType.WARNING);
    }

    /**
     * 显示警告通知
     *
     * @param message 通知内容
     */
    public static void showWarning( String message) {
        showNotification("警告", message, NotificationType.WARNING);
    }
    
    /**
     * 显示错误通知
     *
     * @param title 通知标题
     * @param message 通知内容
     */
    public static void showError(String title, String message) {
        showNotification(title, message, NotificationType.ERROR);
    }
    
    /**
     * 显示错误通知（使用默认标题）
     *
     * @param message 通知内容
     */
    public static void showError(String message) {
        showError("错误", message);
    }
    
    /**
     * 显示持久化通知
     * 显示不会自动消失的通知，通常用于显示处理中的状态
     *
     * @param message 通知内容
     */
    public static void showPersist(String message) {
        showPersistNotification("处理中", message);
    }
    
    /**
     * 隐藏所有持久化通知
     * 清除所有带有持久化样式的通知
     */
    public static void hidePersist() {
        Platform.runLater(() -> {
            List<Notification> toRemove = new ArrayList<>();
            for (Notification notification : activeNotifications) {
                if (notification.getStyleClass().contains(PERSIST_STYLE)) {
                    hideNotification(notification);
                    toRemove.add(notification);
                }
            }
            activeNotifications.removeAll(toRemove);
        });
    }
    
    /**
     * 显示持久化通知
     * 创建并显示一个带有加载图标的持久化通知
     *
     * @param title 通知标题
     * @param message 通知内容
     */
    private static void showPersistNotification(String title, String message) {
        if (notificationPane == null) return;
        
        Platform.runLater(() -> {
            // 创建通知
            Notification notification = new Notification(message);
            
            // 设置加载图标
            FontIcon icon = new FontIcon(MaterialDesignM.MOTION_PLAY);
            icon.setIconSize(24);
            icon.getStyleClass().add(Styles.ACCENT);
            
            // 添加旋转动画
            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), icon);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(Animation.INDEFINITE);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.play();
            
            notification.setGraphic(icon);
            notification.getStyleClass().addAll("surface-card", PERSIST_STYLE);
            
            // 设置大小和位置
            notification.setPrefWidth(350);
            notification.setPrefHeight(Region.USE_PREF_SIZE);
            notification.setMaxHeight(Region.USE_PREF_SIZE);
            StackPane.setAlignment(notification, Pos.TOP_RIGHT);
            StackPane.setMargin(notification, new Insets(10 + activeNotifications.size() * 90, 10, 0, 0));
            
            // 显示通知
            showNotification(notification);
        });
    }
} 