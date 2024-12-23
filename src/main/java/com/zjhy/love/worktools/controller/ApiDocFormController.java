package com.zjhy.love.worktools.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.ApiDocConfig;
import com.zjhy.love.worktools.service.ApiDocService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.*;

/**
 * API文档表单控制器
 *
 * @author zhengjun
 */
public class ApiDocFormController {

    @FXML
    private VBox formContainer;

    private Form form;
    private final StringProperty packagePathProperty = new SimpleStringProperty("");
    private final StringProperty serviceNameProperty = new SimpleStringProperty("");
    private final StringProperty jarPathProperty = new SimpleStringProperty("");
    private final StringProperty classNameProperty = new SimpleStringProperty("");

    @FXML
    public void initialize() {
        // 创建表单模型
        form = Form.of(
                Group.of(
                        Field.ofStringType(packagePathProperty)
                                .label("包路径")
                                .validate(CustomValidator.forPredicate(
                                        value -> !value.trim().isEmpty(),
                                        "包路径不能为空"
                                ))
                                .validate(CustomValidator.forPredicate(
                                        value -> value.toLowerCase().endsWith(".jar"),
                                        "包路径必须是JAR文件"
                                )),

                        Field.ofStringType(serviceNameProperty)
                                .label("服务名称")
                                .validate(CustomValidator.forPredicate(
                                        value -> !value.trim().isEmpty(),
                                        "服务名称不能为空"
                                ))
                                .validate(CustomValidator.forPredicate(
                                        value -> value.matches("^[a-zA-Z][a-zA-Z0-9-_]*$"),
                                        "服务名称只能包含字母、数字、中划线和下划线，且必须以字母开头"
                                )),

                        Field.ofStringType(jarPathProperty)
                                .label("依赖JAR包名称")
                                .validate(CustomValidator.forPredicate(
                                        value -> !value.trim().isEmpty(),
                                        "依赖JAR包名称不能为空"
                                ))
                                .validate(CustomValidator.forPredicate(
                                        value -> {
                                            String[] jarNames = value.split(",");
                                            for (String jarName : jarNames) {
                                                String trimmedName = jarName.trim();
                                                if (trimmedName.isEmpty()) {
                                                    return false;
                                                }
                                                if (!trimmedName.matches("^[a-zA-Z][a-zA-Z0-9_-]*$")) {
                                                    return false;
                                                }
                                            }
                                            return true;
                                        },
                                        "多个JAR包名称以英文逗号分隔，每个名称必须以字母开头，只能包含字母、数字、中划线和下划线，不需要包含.jar后缀"
                                )),

                        Field.ofStringType(classNameProperty)
                                .label("类名")
                                .validate(CustomValidator.forPredicate(
                                        value -> !value.trim().isEmpty(),
                                        "类名不能为空"
                                ))
                                .validate(CustomValidator.forPredicate(
                                        value -> value.matches(
                                                "^[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*" +
                                                        "(#[a-zA-Z0-9]+(,[a-zA-Z0-9]+)*)*" +
                                                        "(@[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*" +
                                                        "(#[a-zA-Z0-9]+(,[a-zA-Z0-9]+)*)*)*$"
                                        ),
                                        "类名格式不正确，应为：com.xxx.a#pathA,pathB,pathC@com.xxx.b@com.xxx.c#pathA,pathB,pathC"
                                ))
                )
        );

        // 渲染表单
        FormRenderer formRenderer = new FormRenderer(form);
        formContainer.getChildren().add(formRenderer);
    }

    @FXML
    private void handleSelectPackage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择包路径JAR文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JAR文件", "*.jar")
        );

        Window window = formContainer.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            packagePathProperty.set(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmit() {
        if (form.isValid()) {
            form.persist();
            try {
                // 显示处理中提示
                NotificationUtil.showPersist("正在生成文档，请稍候...");

                // 构建配置
                ApiDocConfig config = new ApiDocConfig();
                config.setSourceJarPath(packagePathProperty.get());
                config.setServiceName(serviceNameProperty.get());
                config.setDependencyJars(Arrays.asList(jarPathProperty.get().split(",")));
                config.setClassPathMapping(parseClassPathMapping(classNameProperty.get()));

                // 调用服务生成文档
                ApiDocService service = new ApiDocService();
                service.generateDoc(config);

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
    }

    @FXML
    private void handleReset() {
        packagePathProperty.set("");
        serviceNameProperty.set("");
        jarPathProperty.set("");
        classNameProperty.set("");
        form.reset();
    }

    private Map<String, List<String>> parseClassPathMapping(String input) {
        Map<String, List<String>> mapping = new HashMap<>(4);
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