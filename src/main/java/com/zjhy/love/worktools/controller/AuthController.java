package com.zjhy.love.worktools.controller;

import com.dlsc.formsfx.model.structure.*;
import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zjhy.love.worktools.common.util.FileUtil;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.AuthEntry;
import com.zjhy.love.worktools.service.AuthService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zhengjun
 */
public class AuthController  {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @FXML
    private TableView<AuthEntry> authTable;
    
    @FXML
    private Label countdownLabel;
    
    private final AuthService authService = new AuthService();
    private final ObservableList<AuthEntry> authEntries = FXCollections.observableArrayList();
    private Timeline timeline;

    @FXML
    public void initialize() {
        initializeAuthTable();
        startCountdown();
        loadHistory();
    }

    private void initializeAuthTable() {
        // 账户列
        TableColumn<AuthEntry, String> nameColumn = new TableColumn<>("账户");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        
        // 发行方列
        TableColumn<AuthEntry, String> issuerColumn = new TableColumn<>("发行方");
        issuerColumn.setCellValueFactory(cellData -> cellData.getValue().issuerProperty());
        
        // 验证码列
        TableColumn<AuthEntry, String> codeColumn = new TableColumn<>("验证码");
        codeColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox container = new HBox(5);  // 5是组件之间的间距
            private final Label codeLabel = new Label();
            private final Button copyButton = new Button("复制");
            
            {
                // 设置复制按钮样式
                copyButton.getStyleClass().addAll("btn", "btn-default", "btn-xs");
                copyButton.setOnAction(event -> {
                    // 获取验证码并复制到剪贴板
                    String code = codeLabel.getText();
                    if (code != null && !code.isEmpty()) {
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(code);
                        clipboard.setContent(content);
                        
                        // 显示复制成功提示
                        NotificationUtil.showSuccess("复制成功", "验证码已复制到剪贴板");
                    }
                });
                
                // 将标签和按钮添加到容器中
                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().addAll(codeLabel, copyButton);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AuthEntry entry = getTableView().getItems().get(getIndex());
                    String code = authService.generateTOTP(
                        entry.getSecret(),
                        entry.getDigits(),
                        entry.getPeriod()
                    );
                    codeLabel.setText(code);
                    setGraphic(container);
                }
            }
        });
        
        // 操作列
        TableColumn<AuthEntry, Void> actionColumn = new TableColumn<>("操作");
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("删除");
            private final Button qrButton = new Button("查看二维码");
            
            {
                deleteButton.getStyleClass().addAll("btn", "btn-danger", "btn-sm");
                qrButton.getStyleClass().addAll("btn", "btn-info", "btn-sm");
                
                deleteButton.setOnAction(event -> {
                    AuthEntry entry = getTableView().getItems().get(getIndex());
                    handleDelete(entry);
                });
                
                qrButton.setOnAction(event -> {
                    AuthEntry entry = getTableView().getItems().get(getIndex());
                    showQRCode(entry);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5);
                    box.getChildren().addAll(qrButton, deleteButton);
                    setGraphic(box);
                }
            }
        });
        
        authTable.getColumns().setAll(nameColumn, issuerColumn, codeColumn, actionColumn);
        authTable.setItems(authEntries);
    }

    private void handleDelete(AuthEntry entry) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除验证器 " + entry.getName() + " 吗？");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                authEntries.remove(entry);
                saveHistory();
            }
        });
    }

    private void showQRCode(AuthEntry entry) {
        try {
            Image qrCode = authService.generateQRCode(entry);
            
            ImageView imageView = new ImageView(qrCode);
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("二维码");
            dialog.setHeaderText(entry.getName());
            dialog.getDialogPane().setContent(imageView);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            dialog.showAndWait();
        } catch (Exception e) {
            LOGGER.error("显示二维码失败", e);
            NotificationUtil.showError("错误", "显示二维码失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddAuth() {
        Dialog<AuthEntry> dialog = new Dialog<>();
        dialog.setTitle("添加验证器");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("bootstrap-dialog");
        
        // 创建表单
        StringField nameField = Field.ofStringType("")
            .label("账户名称")
            .required("请输入账户名称")
            .validate(CustomValidator.forPredicate(
                name -> name != null && !name.trim().isEmpty(),
                "账户名称不能为空"
            ));
            
        StringField issuerField = Field.ofStringType("")
            .label("发行方")
            .required("请输入发行方")
            .validate(CustomValidator.forPredicate(
                issuer -> issuer != null && !issuer.trim().isEmpty(),
                "发行方不能为空"
            ));
            
        StringField secretField = Field.ofStringType("")
            .label("密钥")
            .required("请输入密钥")
            .validate(CustomValidator.forPredicate(
                secret -> {
                    if (secret == null || secret.trim().isEmpty()) {
                        return false;
                    }
                    try {
                        new Base32().decode(secret.toUpperCase());
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                },
                "密钥格式无效，请输入有效的Base32编码"
            ));
            
        StringField algorithmField = Field.ofStringType("SHA1")
            .label("算法")
            .validate(CustomValidator.forPredicate(
                algorithm -> List.of("SHA1", "SHA256", "SHA512").contains(algorithm),
                "算法必须是 SHA1、SHA256 或 SHA512"
            ));
            
        IntegerField digitsField = Field.ofIntegerType(6)
            .label("验证码位数")
            .validate(CustomValidator.forPredicate(
                digits -> digits >= 6 && digits <= 8,
                "验证码位数必须在6-8之间"
            ));

        IntegerField periodField = Field.ofIntegerType(30)
            .label("更新间隔（秒）")
            .validate(CustomValidator.forPredicate(
                period -> period > 0 && period <= 60,
                "更新间隔必须在1-60秒之间"
            ));

        Form form = Form.of(
            Group.of(
                nameField,
                issuerField,
                secretField,
                algorithmField,
                digitsField,
                periodField
            )
        );

        // 创建表单渲染器
        FormRenderer formRenderer = new FormRenderer(form);
        formRenderer.getStyleClass().add("bootstrap-form");

        // 添加按钮
        ButtonType addButton = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        // 设置对话框内容
        VBox content = new VBox(10);
        content.getStyleClass().add("bootstrap-panel");
        content.getChildren().add(formRenderer);
        content.setPadding(new Insets(20));

        // 设置对话框内容
        dialog.getDialogPane().setContent(content);

        // 设置对话框按钮样式
        Node addButtonNode = dialog.getDialogPane().lookupButton(addButton);
        if (addButtonNode instanceof Button) {
            addButtonNode.getStyleClass().addAll("btn", "btn-primary");
        }
        
        Node cancelButtonNode = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButtonNode instanceof Button) {
            cancelButtonNode.getStyleClass().addAll("btn", "btn-default");
        }

        // 设置结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton && form.isValid()) {
                AuthEntry entry = new AuthEntry();
                entry.setName(nameField.getValue());
                entry.setIssuer(issuerField.getValue());
                entry.setSecret(secretField.getValue());
                entry.setAlgorithm(algorithmField.getValue());
                entry.setDigits(digitsField.getValue());
                entry.setPeriod(periodField.getValue());
                return entry;
            }
            return null;
        });

        // 显示对话框并处理结果
        dialog.showAndWait().ifPresent(entry -> {
            authEntries.add(entry);
            saveHistory();
            NotificationUtil.showSuccess("添加成功", "验证器已添加");
        });
    }

    private void loadHistory() {
        try {
            List<AuthEntry> entries = HistoryUtil.getHistory("auth", new TypeReference<List<AuthEntry>>() {});
            if (entries != null) {
                authEntries.setAll(entries);
            }
        } catch (Exception e) {
            LOGGER.error("加载历史配置失败", e);
            NotificationUtil.showError("错误", "加载历史配置失败: " + e.getMessage());
        }
    }

    private void saveHistory() {
        try {
            HistoryUtil.saveHistory("auth", new ArrayList<>(authEntries));
        } catch (Exception e) {
            LOGGER.error("保存历史配置失败", e);
            NotificationUtil.showError("错误", "保存历史配置失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleImport() {
        try {
            List<AuthEntry> entries = FileUtil.importFromJson(
                    new TypeReference<List<AuthEntry>>() {
                    },
                    "导入身份验证器配置",
                    authTable.getScene().getWindow()
            );
            if (entries != null) {
                authEntries.addAll(entries);
                saveHistory();
                NotificationUtil.showSuccess("导入成功", "身份验证器配置已导入");
            }
        } catch (Exception e) {
            LOGGER.error("导入配置失败", e);
            NotificationUtil.showError("导入失败", e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        try {
            FileUtil.exportToJson(
                new ArrayList<>(authEntries),
                "导出身份验证器配置",
                authTable.getScene().getWindow()
            );
        } catch (Exception e) {
            LOGGER.error("导出配置失败", e);
            NotificationUtil.showError("导出失败", e.getMessage());
        }
    }

    private void startCountdown() {
        timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> updateCodes())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateCodes() {
        long currentTime = System.currentTimeMillis() / 1000L;
        int remainingSeconds = 30 - (int)(currentTime % 30);
        countdownLabel.setText(String.format("%d秒后更新", remainingSeconds));
        
        // 刷新表格以触发验证码更新
        authTable.refresh();
    }
}
