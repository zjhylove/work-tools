package com.zjhy.love.worktools;

import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.controller.IpForwardController;
import com.zjhy.love.worktools.controller.LayoutUiController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * @author zhengjun
 */
public class WorkToolsApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkToolsApplication.class);

    private LayoutUiController layoutController;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/layout-view.fxml"));
            if (fxmlLoader.getLocation() == null) {
                throw new IllegalStateException("无法找到FXML文件: /view/layout-view.fxml");
            }
            Scene scene = new Scene(fxmlLoader.load(), 960, 600);
            scene.getStylesheets().addAll(
                    BootstrapFX.bootstrapFXStylesheet(),
                    Objects.requireNonNull(getClass().getResource("/css/application.css")).toExternalForm()
            );

            // 获取布局控制器
            layoutController = fxmlLoader.getController();

            // 设置标题样式
            stage.setTitle("✨ Work Tools ⚡");
            // 设置应用图标
            stage.getIcons().add(new Image(Objects.requireNonNull(WorkToolsApplication.class.getResourceAsStream("/images/tools-icon.png"))));

            // 初始化通知工具
            NotificationUtil.initStage(stage);

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            LOGGER.error("应用启动失败", e);
            throw e;
        }
    }

    @Override
    public void stop() {
        try {
            // 从布局控制器获取IP转发控制器
            IpForwardController controller = layoutController.getIpForwardController();
            if (controller != null) {
                controller.stop();
                LOGGER.info("IP转发服务已停止");
            }
        } catch (Exception e) {
            LOGGER.error("停止IP转发服务失败", e);
        }
        
        // ... 其他清理工作 ...
    }

    public static void main(String[] args) {
        launch();
    }
}