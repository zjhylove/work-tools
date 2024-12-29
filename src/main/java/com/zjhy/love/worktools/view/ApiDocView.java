package com.zjhy.love.worktools.view;

import atlantafx.base.theme.Styles;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.ApiDocConfig;
import com.zjhy.love.worktools.service.ApiDocService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.controlsfx.glyphfont.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;

public class ApiDocView extends BaseView {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDocView.class);
    
    private final TextField jarPathField = new TextField();
    private final TextField serviceNameField = new TextField();
    private final TextField dependencyJarsField = new TextField();
    private final TextField classNameField = new TextField();
    
    public ApiDocView() {
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
        
        // JAR包路径
        HBox jarPathRow = new HBox(10);
        jarPathRow.setAlignment(Pos.CENTER_LEFT);
        
        Label jarPathLabel = new Label("JAR包路径:");
        jarPathLabel.setMinWidth(100);
        
        jarPathField.setPromptText("选择或输入JAR文件路径");
        jarPathField.setPrefWidth(400);
        HBox.setHgrow(jarPathField, Priority.ALWAYS);
        
        Button selectJarButton = new Button("选择", new Glyph("FontAwesome", "FOLDER_OPEN"));
        selectJarButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        selectJarButton.setOnAction(e -> handleSelectJar());
        
        jarPathRow.getChildren().addAll(jarPathLabel, jarPathField, selectJarButton);
        
        // 服务名称
        HBox serviceNameRow = new HBox(10);
        serviceNameRow.setAlignment(Pos.CENTER_LEFT);
        
        Label serviceNameLabel = new Label("服务名称:");
        serviceNameLabel.setMinWidth(100);
        
        serviceNameField.setPromptText("输入服务名称");
        serviceNameField.setPrefWidth(400);
        HBox.setHgrow(serviceNameField, Priority.ALWAYS);
        
        serviceNameRow.getChildren().addAll(serviceNameLabel, serviceNameField);
        
        // 依赖JAR包
        HBox dependencyRow = new HBox(10);
        dependencyRow.setAlignment(Pos.CENTER_LEFT);
        
        Label dependencyLabel = new Label("依赖JAR包:");
        dependencyLabel.setMinWidth(100);
        
        dependencyJarsField.setPromptText("输入依赖JAR包名称，多个用英文逗号分隔");
        dependencyJarsField.setPrefWidth(400);
        HBox.setHgrow(dependencyJarsField, Priority.ALWAYS);
        
        dependencyRow.getChildren().addAll(dependencyLabel, dependencyJarsField);
        
        // 类名
        HBox classNameRow = new HBox(10);
        classNameRow.setAlignment(Pos.CENTER_LEFT);
        
        Label classNameLabel = new Label("类名:");
        classNameLabel.setMinWidth(100);
        
        classNameField.setPromptText("输入类名，格式：com.xxx.a#pathA,pathB@com.xxx.b#pathC");
        classNameField.setPrefWidth(400);
        HBox.setHgrow(classNameField, Priority.ALWAYS);
        
        classNameRow.getChildren().addAll(classNameLabel, classNameField);
        
        // 添加所有行到表单
        form.getChildren().addAll(jarPathRow, serviceNameRow, dependencyRow, classNameRow);
        
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
    
    private void handleSelectJar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择JAR文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JAR文件", "*.jar")
        );
        
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            jarPathField.setText(file.getAbsolutePath());
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
            ApiDocConfig config = new ApiDocConfig();
            config.setSourceJarPath(jarPathField.getText());
            config.setServiceName(serviceNameField.getText());
            config.setDependencyJars(java.util.Arrays.asList(dependencyJarsField.getText().split(",")));
            config.setClassPathMapping(parseClassPathMapping(classNameField.getText()));
            
            // 保存历史记录
            HistoryUtil.saveHistory("apiDoc", config);
            
            // 生成文档
            ApiDocService service = new ApiDocService();
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
    
    private void handleReset() {
        jarPathField.clear();
        serviceNameField.clear();
        dependencyJarsField.clear();
        classNameField.clear();
    }
    
    private void loadHistory() {
        ApiDocConfig history = HistoryUtil.getHistory("apiDoc", ApiDocConfig.class);
        if (history != null) {
            jarPathField.setText(history.getSourceJarPath());
            serviceNameField.setText(history.getServiceName());
            dependencyJarsField.setText(String.join(",", history.getDependencyJars()));
            
            // 将Map转换为字符串格式
            StringBuilder classPathStr = new StringBuilder();
            history.getClassPathMapping().forEach((className, paths) -> {
                if (classPathStr.length() > 0) {
                    classPathStr.append("@");
                }
                classPathStr.append(className);
                if (!paths.isEmpty()) {
                    classPathStr.append("#").append(String.join(",", paths));
                }
            });
            classNameField.setText(classPathStr.toString());
        }
    }
    
    private boolean validateInput() {
        String jarPath = jarPathField.getText().trim();
        if (jarPath.isEmpty() || !jarPath.toLowerCase().endsWith(".jar")) {
            NotificationUtil.showError("验证失败", "JAR包路径不能为空且必须是JAR文件");
            return false;
        }
        
        String serviceName = serviceNameField.getText().trim();
        if (serviceName.isEmpty() || !serviceName.matches("^[a-zA-Z][a-zA-Z0-9-_]*$")) {
            NotificationUtil.showError("验证失败", "服务名称不能为空且必须以字母开头，只能包含字母、数字、中划线和下划线");
            return false;
        }
        
        String[] jarNames = dependencyJarsField.getText().split(",");
        for (String jarName : jarNames) {
            String trimmedName = jarName.trim();
            if (trimmedName.isEmpty() || !trimmedName.matches("^[a-zA-Z][a-zA-Z0-9_-]*$")) {
                NotificationUtil.showError("验证失败", "依赖JAR包名称格式不正确");
                return false;
            }
        }
        
        String className = classNameField.getText().trim();
        if (!className.matches(
            "^[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*" +
            "(#[a-zA-Z0-9]+(,[a-zA-Z0-9]+)*)*" +
            "(@[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*" +
            "(#[a-zA-Z0-9]+(,[a-zA-Z0-9]+)*)*)*$"
        )) {
            NotificationUtil.showError("验证失败", "类名格式不正确");
            return false;
        }
        
        return true;
    }
    
    private java.util.Map<String, java.util.List<String>> parseClassPathMapping(String input) {
        java.util.Map<String, java.util.List<String>> mapping = new java.util.HashMap<>();
        String[] parts = input.split("@");
        for (String part : parts) {
            String[] classAndPaths = part.split("#");
            String className = classAndPaths[0];
            java.util.List<String> paths = classAndPaths.length > 1
                ? java.util.Arrays.asList(classAndPaths[1].split(","))
                : java.util.Collections.emptyList();
            mapping.put(className, paths);
        }
        return mapping;
    }
} 