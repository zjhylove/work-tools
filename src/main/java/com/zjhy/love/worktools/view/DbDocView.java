package com.zjhy.love.worktools.view;

import atlantafx.base.theme.Styles;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.DbDocConfig;
import com.zjhy.love.worktools.service.DbDocService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.controlsfx.glyphfont.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DbDocView extends BaseView {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbDocView.class);
    
    private final TextField jdbcUrlField = new TextField();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField tablesField = new TextField();
    private final TextField outputDirField = new TextField();
    
    public DbDocView() {
        // 创建表单
        VBox form = createForm();
        
        // 创建按钮工具栏
        HBox toolbar = createToolbar();
        
        // 添加到视图
        addContent(form, toolbar);
        
        // 加载历史配置
        loadHistory();
    }
    
    private VBox createForm() {
        VBox form = new VBox(20);
        form.getStyleClass().add("form-section");
        
        // JDBC URL
        HBox jdbcUrlRow = new HBox(10);
        jdbcUrlRow.setAlignment(Pos.CENTER_LEFT);
        
        Label jdbcUrlLabel = new Label("JDBC URL:");
        jdbcUrlLabel.setMinWidth(100);
        
        jdbcUrlField.setPromptText("输入数据库连接URL");
        jdbcUrlField.setPrefWidth(400);
        HBox.setHgrow(jdbcUrlField, Priority.ALWAYS);
        
        jdbcUrlRow.getChildren().addAll(jdbcUrlLabel, jdbcUrlField);
        
        // 用户名
        HBox usernameRow = new HBox(10);
        usernameRow.setAlignment(Pos.CENTER_LEFT);
        
        Label usernameLabel = new Label("用户名:");
        usernameLabel.setMinWidth(100);
        
        usernameField.setPromptText("输入数据库用户名");
        usernameField.setPrefWidth(400);
        HBox.setHgrow(usernameField, Priority.ALWAYS);
        
        usernameRow.getChildren().addAll(usernameLabel, usernameField);
        
        // 密码
        HBox passwordRow = new HBox(10);
        passwordRow.setAlignment(Pos.CENTER_LEFT);
        
        Label passwordLabel = new Label("密码:");
        passwordLabel.setMinWidth(100);
        
        passwordField.setPromptText("输入数据库密码");
        passwordField.setPrefWidth(400);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        
        passwordRow.getChildren().addAll(passwordLabel, passwordField);
        
        // 表名
        HBox tablesRow = new HBox(10);
        tablesRow.setAlignment(Pos.CENTER_LEFT);
        
        Label tablesLabel = new Label("表名:");
        tablesLabel.setMinWidth(100);
        
        tablesField.setPromptText("输入表名，多个用英文逗号分隔（可选）");
        tablesField.setPrefWidth(400);
        HBox.setHgrow(tablesField, Priority.ALWAYS);
        
        tablesRow.getChildren().addAll(tablesLabel, tablesField);
        
        // 输出目录
        HBox outputDirRow = new HBox(10);
        outputDirRow.setAlignment(Pos.CENTER_LEFT);
        
        Label outputDirLabel = new Label("输出目录:");
        outputDirLabel.setMinWidth(100);
        
        outputDirField.setPromptText("选择或输入文档输出目录");
        outputDirField.setPrefWidth(400);
        HBox.setHgrow(outputDirField, Priority.ALWAYS);
        
        Button selectDirButton = new Button("选择", new Glyph("FontAwesome", "FOLDER_OPEN"));
        selectDirButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        selectDirButton.setOnAction(e -> handleSelectDir());
        
        outputDirRow.getChildren().addAll(outputDirLabel, outputDirField, selectDirButton);
        
        // 添加所有行到表单
        form.getChildren().addAll(jdbcUrlRow, usernameRow, passwordRow, tablesRow, outputDirRow);
        
        return form;
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        
        Button generateButton = new Button("生成文档", new Glyph("FontAwesome", "FILE_TEXT"));
        generateButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SUCCESS);
        generateButton.setOnAction(e -> handleGenerate());
        
        Button resetButton = new Button("重置", new Glyph("FontAwesome", "REFRESH"));
        resetButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        resetButton.setOnAction(e -> handleReset());
        
        toolbar.getChildren().addAll(resetButton, generateButton);
        
        return toolbar;
    }
    
    private void handleSelectDir() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("选择输出目录");
        
        File dir = dirChooser.showDialog(getScene().getWindow());
        if (dir != null) {
            outputDirField.setText(dir.getAbsolutePath());
        }
    }
    
    private void handleGenerate() {
        // 验证输入
        if (!validateInput()) {
            return;
        }
        
        try {
            // 显示处理中提示
            NotificationUtil.showPersist("正在生成文档，请稍候...");
            
            // 构建配置
            DbDocConfig config = getDbDocConfig();

            // 保存历史记录
            HistoryUtil.saveHistory("dbDoc", config);
            
            // 生成文档
            DbDocService service = new DbDocService();
            String docPath = service.generateDoc(config);
            
            // 隐藏处理中提示
            NotificationUtil.hidePersist();
            
            // 显示成功提示
            NotificationUtil.showSuccess("文档生成成功！");
            
            // 打开文档所在目录
            Desktop.getDesktop().open(new File(docPath));
            
        } catch (Exception e) {
            LOGGER.error("生成文档失败", e);
            NotificationUtil.hidePersist();
            NotificationUtil.showError("生成文档失败", e.getMessage());
        }
    }

    private DbDocConfig getDbDocConfig() {
        DbDocConfig config = new DbDocConfig();
        config.setJdbcUrl(jdbcUrlField.getText().trim());
        config.setUsername(usernameField.getText().trim());
        config.setPassword(passwordField.getText());

        // 将逗号分隔的表名字符串转换为List
        String tablesStr = tablesField.getText().trim();
        List<String> tables = tablesStr.isEmpty()
            ? Collections.emptyList()
            : Arrays.asList(tablesStr.split(","));
        config.setTables(tables);

        config.setOutputDir(outputDirField.getText().trim());
        return config;
    }

    private void handleReset() {
        jdbcUrlField.clear();
        usernameField.clear();
        passwordField.clear();
        tablesField.clear();
        outputDirField.clear();
    }
    
    private void loadHistory() {
        DbDocConfig history = HistoryUtil.getHistory("dbDoc", DbDocConfig.class);
        if (history != null) {
            jdbcUrlField.setText(history.getJdbcUrl());
            usernameField.setText(history.getUsername());
            passwordField.setText(history.getPassword());
            // 将List转换为逗号分隔的字符串
            tablesField.setText(String.join(",", history.getTables()));
            outputDirField.setText(history.getOutputDir());
        }
    }
    
    private boolean validateInput() {
        String jdbcUrl = jdbcUrlField.getText().trim();
        if (jdbcUrl.isEmpty()) {
            NotificationUtil.showError("验证失败", "JDBC URL不能为空");
            return false;
        }
        
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            NotificationUtil.showError("验证失败", "用户名不能为空");
            return false;
        }
        
        String password = passwordField.getText();
        if (password.isEmpty()) {
            NotificationUtil.showError("验证失败", "密码不能为空");
            return false;
        }
        
        String outputDir = outputDirField.getText().trim();
        if (outputDir.isEmpty()) {
            NotificationUtil.showError("验证失败", "输出目录不能为空");
            return false;
        }
        
        return true;
    }
} 