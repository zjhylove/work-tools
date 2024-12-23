package com.zjhy.love.worktools.controller;

import com.zjhy.love.worktools.model.ApiDocConfig;
import com.zjhy.love.worktools.model.ApiInfo;
import com.zjhy.love.worktools.service.ApiDocService;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.*;

/**
 * @author zhengjun
 */
public class ApiDocFormController {

    @FXML
    private TextField packagePathField;
    @FXML
    private TextField serviceNameField;
    @FXML
    private TextField jarPathField;
    @FXML
    private TextField classNameField;

    @FXML
    private Label packagePathError;
    @FXML
    private Label serviceNameError;
    @FXML
    private Label jarPathError;
    @FXML
    private Label classNameError;

    @FXML
    public void initialize() {
        // 添加字段验证监听器
        packagePathField.textProperty().addListener((obs, old, newValue) -> validatePackagePath());
        serviceNameField.textProperty().addListener((obs, old, newValue) -> validateServiceName());
        jarPathField.textProperty().addListener((obs, old, newValue) -> validateJarPath());
        classNameField.textProperty().addListener((obs, old, newValue) -> validateClassName());
    }

    @FXML
    private void handleSelectPackage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择包路径JAR文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JAR文件", "*.jar")
        );

        Window window = packagePathField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            packagePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            if (validateAll()) {
                try {
                    // 构建配置
                    ApiDocConfig config = new ApiDocConfig();
                    config.setSourceJarPath(packagePathField.getText().trim());
                    config.setServiceName(serviceNameField.getText().trim());
                    config.setDependencyJars(Arrays.asList(
                            jarPathField.getText().trim().split(",")
                    ));

                    // 解析类名和路径映射
                    Map<String, List<String>> classPathMapping = parseClassPathMapping(
                            classNameField.getText().trim()
                    );
                    config.setClassPathMapping(classPathMapping);

                    // 显示处理中提示
                    NotificationUtil.showPersist("正在生成文档，请稍候...");
                    
                    // 生成文档
                    ApiDocService docService = new ApiDocService();
                    docService.generateApiDoc(config);

                    // 隐藏处理中提示
                    NotificationUtil.hidePersist();
                    // 显示成功提示
                    NotificationUtil.showSuccess("文档生成成功！");

                } catch (Exception e) {
                    e.printStackTrace();
                    // 隐藏处理中提示
                    NotificationUtil.hidePersist();
                    // 显示错误提示
                    NotificationUtil.showError("生成文档失败", e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.showError("验证失败", e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        packagePathField.clear();
        serviceNameField.clear();
        jarPathField.clear();
        classNameField.clear();

        hideAllErrors();
    }

    private boolean validateAll() {
        boolean isValid = validatePackagePath();
        if (!validateServiceName()) {
            isValid = false;
        }
        if (!validateJarPath()) {
            isValid = false;
        }
        if (!validateClassName()) {
            isValid = false;
        }
        return isValid;
    }

    private boolean validatePackagePath() {
        String value = packagePathField.getText().trim();
        if (value.isEmpty()) {
            showError(packagePathError, "包路径不能为空");
            return false;
        }
        if (!value.endsWith(".jar")) {
            showError(packagePathError, "必须是JAR文件");
            return false;
        }
        hideError(packagePathError);
        return true;
    }

    private boolean validateServiceName() {
        String value = serviceNameField.getText().trim();
        if (value.isEmpty()) {
            showError(serviceNameError, "服务名称不能为空");
            return false;
        }
        hideError(serviceNameError);
        return true;
    }

    private boolean validateJarPath() {
        String value = jarPathField.getText().trim();
        if (value.isEmpty()) {
            showError(jarPathError, "JAR包名称不能为空");
            return false;
        }

        // 验证多个jar包名称的格式
        String[] jarNames = value.split(",");
        for (String jarName : jarNames) {
            String trimmedName = jarName.trim();
            if (trimmedName.isEmpty()) {
                showError(jarPathError, "JAR包名称不能为空");
                return false;
            }
            if (trimmedName.contains(".")) {
                showError(jarPathError, "JAR包名称不需要包含扩展名");
                return false;
            }
            // 验证jar包名称格式：只允许字母、数字、中划线和下划线
            if (!trimmedName.matches("^[a-zA-Z0-9_-]+$")) {
                showError(jarPathError, "JAR包名称只能包含字母、数字、中划线和下划线");
                return false;
            }
        }

        hideError(jarPathError);
        return true;
    }

    private boolean validateClassName() {
        String value = classNameField.getText().trim();
        if (value.isEmpty()) {
            showError(classNameError, "类名不能为空");
            return false;
        }

        // 验证格式：com.xxx.a#pathA,pathB,pathC@com.xxx.b@com.xxx.c#pathA,pathB,pathC
        // 包名部分
        String regex = "^[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*" +
                // #路径部分（可选）
                "(#[a-zA-Z0-9]+(,[a-zA-Z0-9]+)*)*" +
                // @类名部分（可选）
                "(@[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*" +
                // 后续类名的路径部分（可选）
                "(#[a-zA-Z0-9]+(,[a-zA-Z0-9]+)*)*)*$";

        if (!value.matches(regex)) {
            showError(classNameError, "类名格式不正确，应为：com.xxx.a#pathA,pathB,pathC@com.xxx.b@com.xxx.c#pathA,pathB,pathC");
            return false;
        }
        hideError(classNameError);
        return true;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }

    private void hideAllErrors() {
        hideError(packagePathError);
        hideError(serviceNameError);
        hideError(jarPathError);
        hideError(classNameError);
    }

    private Map<String, List<String>> parseClassPathMapping(String input) {
        Map<String, List<String>> mapping = new HashMap<>();
        String[] parts = input.split("@");

        for (String part : parts) {
            String[] classAndPaths = part.split("#");
            String className = classAndPaths[0];
            List<String> paths = classAndPaths.length > 1
                    ? Arrays.asList(classAndPaths[1].split(","))
                    : Collections.emptyList();
            mapping.put(className, paths);
        }

        return mapping;
    }
} 