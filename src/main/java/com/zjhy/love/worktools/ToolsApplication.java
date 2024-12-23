package com.zjhy.love.worktools;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.util.Objects;

/**
 * @author zhengjun
 */
public class ToolsApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ToolsApplication.class.getResource("/view/layout-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        // 添加BootstrapFX样式表
        scene.getStylesheets().addAll(
            BootstrapFX.bootstrapFXStylesheet(),
            Objects.requireNonNull(getClass().getResource("/css/application.css")).toExternalForm()
        );
        
        // 设置标题样式
        stage.setTitle("✨ Work Tools ⚡");
        
        // 设置应用图标
        stage.getIcons().add(new Image(Objects.requireNonNull(ToolsApplication.class.getResourceAsStream("/images/tools-icon.png"))));
        
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}