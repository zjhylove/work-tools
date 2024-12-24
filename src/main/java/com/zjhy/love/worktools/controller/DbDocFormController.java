package com.zjhy.love.worktools.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.DbDocConfig;
import com.zjhy.love.worktools.service.DbDocService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.Arrays;

/**
 * 数据库文档表单控制器
 * 提供数据库文档生成的用户界面和交互功能
 *
 * @author zhengjun
 */
public class DbDocFormController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DbDocFormController.class);
    
    @FXML
    private VBox formContainer;
    
    /** 表单对象 */
    private Form form;
    /** 数据库连接URL属性 */
    private final StringProperty jdbcUrlProperty = new SimpleStringProperty("");
    /** 数据库用户名属性 */
    private final StringProperty usernameProperty = new SimpleStringProperty("");
    /** 数据库密码属性 */
    private final StringProperty passwordProperty = new SimpleStringProperty("");
    /** 表名列表属性 */
    private final StringProperty tablesProperty = new SimpleStringProperty("");
    /** 输出目录属性 */
    private final StringProperty outputDirProperty = new SimpleStringProperty("");
    
    @FXML
    public void initialize() {
        // 创建表单
        form = Form.of(
            Group.of(
                Field.ofStringType(jdbcUrlProperty)
                    .label("数据库地址")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "数据库地址不能为空"
                    )),
                    
                Field.ofStringType(usernameProperty)
                    .label("用户名")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "用户名不能为空"
                    )),
                    
                Field.ofStringType(passwordProperty)
                    .label("密码")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "密码不能为空"
                    )),
                    
                Field.ofStringType(tablesProperty)
                    .label("表名")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "表名不能为空"
                    ))
                    .validate(CustomValidator.forPredicate(
                        value -> value.matches("^[a-zA-Z0-9_]+(,[a-zA-Z0-9_]+)*$"),
                        "多个表名以英文逗号分隔，表名只能包含字母、数字和下划线"
                    )),
                    
                Field.ofStringType(outputDirProperty)
                    .label("输出目录")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "输出目录不能为空"
                    ))
            )
        );
        
        // 获取历史记录
        DbDocConfig history = HistoryUtil.getHistory("dbDoc", DbDocConfig.class);
        if (history != null) {
            jdbcUrlProperty.set(history.getJdbcUrl());
            usernameProperty.set(history.getUsername());
            passwordProperty.set(history.getPassword());
            tablesProperty.set(String.join(",", history.getTables()));
            outputDirProperty.set(history.getOutputDir());
        }
        
        // 渲染表单
        FormRenderer formRenderer = new FormRenderer(form);
        formContainer.getChildren().add(formRenderer);
    }
    
    @FXML
    private void handleSelectDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择输出目录");
        
        File dir = directoryChooser.showDialog(formContainer.getScene().getWindow());
        if (dir != null) {
            outputDirProperty.set(dir.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleExport() {
        if (form.isValid()) {
            form.persist();
            try {
                // 显示处理中提示
                NotificationUtil.showPersist("正在生成数据库文档，请稍候...");
                
                // 获取表单数据
                DbDocConfig config = new DbDocConfig();
                config.setJdbcUrl(jdbcUrlProperty.get());
                config.setUsername(usernameProperty.get());
                config.setPassword(passwordProperty.get());
                config.setTables(Arrays.asList(tablesProperty.get().split(",")));
                config.setOutputDir(outputDirProperty.get());
                
                // 保存历史记录
                HistoryUtil.saveHistory("dbDoc", config);
                
                // 调用服务生成文档
                DbDocService service = new DbDocService();
                String docPath = service.generateDoc(config);
                
                // 隐藏处理中提示
                NotificationUtil.hidePersist();
                // 显示成功提示
                NotificationUtil.showSuccess("文档生成成功！");
                
                // 打开文档所在目录
                Desktop.getDesktop().open(new File(docPath).getParentFile());
                
            } catch (Exception e) {
                LOGGER.error("生成文档失败", e);
                // 隐藏处理中提示
                NotificationUtil.hidePersist();
                // 显示错误提示
                NotificationUtil.showError("生成文档失败", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleReset() {
        jdbcUrlProperty.set("");
        usernameProperty.set("");
        passwordProperty.set("");
        tablesProperty.set("");
        outputDirProperty.set("");
        form.reset();
    }
}
