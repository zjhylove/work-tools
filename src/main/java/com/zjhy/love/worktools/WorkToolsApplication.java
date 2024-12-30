package com.zjhy.love.worktools;

import atlantafx.base.theme.PrimerLight;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.common.util.SystemUtil;
import com.zjhy.love.worktools.view.LayoutView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 使用 AtlantaFX 的新版本启动类
 */
public class WorkToolsApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkToolsApplication.class);

    @Override
    public void start(Stage stage) {
        try {
            // 设置 AtlantaFX 主题
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

            // 加载自定义样式表
            String cssPath = Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm();

            // 创建主视图
            LayoutView layoutView = new LayoutView();

            // 创建场景
            Scene scene = new Scene(layoutView, 1200, 800);

            // 添加样式
            scene.getStylesheets().add(cssPath);

            // 设置标题和图标
            stage.setTitle("✨ Work Tools ⚡");
            stage.getIcons().add(new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/images/tools-icon.png")
                    )
            ));

            // 设置场景
            stage.setScene(scene);

            // 初始化系统托盘
            SystemUtil.initSystemTray(stage);

            // 处理窗口关闭事件
            stage.setOnCloseRequest(event -> {
                String str = HistoryUtil.getHistory("minimizeToTray", String.class);
                if (Boolean.parseBoolean(str)) {
                    event.consume();
                    SystemUtil.minimizeToTray();
                } else {
                    Platform.exit();
                    System.exit(0);
                }
            });

            // 显示窗口
            stage.show();

            // 初始化通知系统
            NotificationUtil.initStage(stage);

            LOGGER.info("应用启动成功");

        } catch (Exception e) {
            LOGGER.error("应用启动失败", e);
            throw new RuntimeException("应用启动失败", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 