package com.zjhy.love.worktools.view;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zjhy.love.worktools.common.util.DialogUtil;
import com.zjhy.love.worktools.common.util.FileUtil;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.AuthEntry;
import com.zjhy.love.worktools.service.AuthService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.controlsfx.glyphfont.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AuthView extends BaseView {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthView.class);

    private final AuthService authService = new AuthService();
    private final ObservableList<AuthEntry> authEntries = FXCollections.observableArrayList();
    private final TableView<AuthEntry> authTable = new TableView<>();
    private final Label countdownLabel = new Label();

    public AuthView() {
        // 创建组件
        HBox toolbar = createToolbar();
        configureAuthTable();
        VBox.setVgrow(authTable, Priority.ALWAYS);

        // 使用基类方法添加内容
        addContent(toolbar, authTable);

        // 初始化
        initializeData();

        //倒计时刷新验证码
        startCountdown();
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button addButton = new Button("添加验证器", new Glyph("FontAwesome", "PLUS"));
        addButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        addButton.setOnAction(e -> handleAddAuth());

        Button importButton = new Button("导入", new Glyph("FontAwesome", "DOWNLOAD"));
        importButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        importButton.setOnAction(e -> handleImport());

        Button exportButton = new Button("导出", new Glyph("FontAwesome", "UPLOAD"));
        exportButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        exportButton.setOnAction(e -> handleExport());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        countdownLabel.getStyleClass().add(Styles.TEXT_SUBTLE);

        toolbar.getChildren().addAll(addButton, importButton, exportButton, spacer, countdownLabel);
        return toolbar;
    }

    private void configureAuthTable() {
        authTable.getStyleClass().add("table-striped");
        authTable.setPlaceholder(new Label("暂无数据"));  // 添加空数据提示
        authTable.setFixedCellSize(40);  // 设置固定行高
        authTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);  // 设置列宽调整策略
        ObservableList<TableColumn<AuthEntry, ?>> columns = authTable.getColumns();


        // 账户列
        TableColumn<AuthEntry, String> nameColumn = new TableColumn<>("账户");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        nameColumn.setPrefWidth(200);
        columns.add(nameColumn);

        // 发行方列
        TableColumn<AuthEntry, String> issuerColumn = new TableColumn<>("发行方");
        issuerColumn.setCellValueFactory(data -> data.getValue().issuerProperty());
        issuerColumn.setPrefWidth(150);
        columns.add(issuerColumn);


        // 验证码列
        TableColumn<AuthEntry, String> codeColumn = new TableColumn<>("验证码");
        codeColumn.setCellFactory(col -> createCodeCell());
        codeColumn.setPrefWidth(150);
        columns.add(codeColumn);


        // 操作列
        TableColumn<AuthEntry, Void> actionColumn = new TableColumn<>("操作");
        actionColumn.setCellFactory(col -> createActionCell());
        actionColumn.setPrefWidth(200);
        columns.add(actionColumn);


        authTable.setItems(authEntries);

        // 设置表格容器的内边距
        getContentBox().setPadding(new Insets(25, 0, 38, 0));
    }

    private TableCell<AuthEntry, String> createCodeCell() {
        return new TableCell<>() {
            private final HBox container = new HBox();
            private final Label codeLabel = new Label();

            {
                container.setAlignment(Pos.CENTER);
                container.getStyleClass().add("code-container");
                container.setPadding(new Insets(5, 10, 5, 10));

                codeLabel.getStyleClass().addAll(Styles.TITLE_3, Styles.ACCENT);
                container.getChildren().add(codeLabel);

                // 添加点击复制功能
                container.setOnMouseClicked(event -> {
                    if (!isEmpty()) {
                        copyToClipboard(codeLabel.getText());
                    }
                });

                // 添加鼠标悬停效果
                container.setOnMouseEntered(e -> {
                    container.setStyle("-fx-background-color: rgba(33, 147, 176, 0.1);");
                    setCursor(javafx.scene.Cursor.HAND);
                });
                container.setOnMouseExited(e -> {
                    container.setStyle(null);
                    setCursor(javafx.scene.Cursor.DEFAULT);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AuthEntry entry = getTableRow().getItem();
                    if (entry != null) {
                        String code = authService.generateTOTP(
                                entry.getSecret(),
                                entry.getDigits(),
                                entry.getPeriod()
                        );
                        // 每3位数字添加一个空格，提高可读性
                        String formattedCode = code.replaceAll("(.{3})", "$1 ").trim();
                        codeLabel.setText(formattedCode);
                        setGraphic(container);
                    }
                }
            }
        };
    }

    private TableCell<AuthEntry, Void> createActionCell() {
        return new TableCell<>() {
            private final Button qrButton = new Button("二维码", new Glyph("FontAwesome", "QRCODE"));
            private final Button deleteButton = new Button("删除", new Glyph("FontAwesome", "TRASH"));
            private final HBox container = new HBox(5);

            {
                qrButton.getStyleClass().addAll(Styles.SMALL, Styles.ACCENT);
                deleteButton.getStyleClass().addAll(Styles.SMALL, Styles.DANGER);

                qrButton.setOnAction(event -> {
                    AuthEntry entry = getTableRow().getItem();
                    if (entry != null) {
                        showQRCode(entry);
                    }
                });

                deleteButton.setOnAction(event -> {
                    AuthEntry entry = getTableRow().getItem();
                    if (entry != null) {
                        handleDelete(entry);
                    }
                });

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(qrButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        };
    }

    private void handleAddAuth() {
        Dialog<AuthEntry> dialog = DialogUtil.createCommonDataDialog("添加验证器");
        DialogPane dialogPane = dialog.getDialogPane();

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField issuerField = new TextField();
        TextField secretField = new TextField();
        ComboBox<String> algorithmBox = new ComboBox<>(
                FXCollections.observableArrayList("SHA1", "SHA256", "SHA512")
        );
        Spinner<Integer> digitsSpinner = new Spinner<>(6, 8, 6);
        Spinner<Integer> periodSpinner = new Spinner<>(30, 60, 30);

        grid.addRow(0, new Label("账户名称:"), nameField);
        grid.addRow(1, new Label("发行方:"), issuerField);
        grid.addRow(2, new Label("密钥:"), secretField);
        grid.addRow(3, new Label("算法:"), algorithmBox);
        grid.addRow(4, new Label("验证码位数:"), digitsSpinner);
        grid.addRow(5, new Label("更新间隔(秒):"), periodSpinner);

        // 生成随机密钥按钮
        Button generateButton = new Button("生成密钥", new Glyph("FontAwesome", "RANDOM"));
        generateButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        generateButton.setOnAction(e -> secretField.setText(authService.generateSecret()));
        grid.add(generateButton, 2, 2);

        dialogPane.setContent(grid);

        // 添加按钮
        ButtonType addButton = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        // 设置结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    AuthEntry entry = new AuthEntry();
                    entry.setName(nameField.getText());
                    entry.setIssuer(issuerField.getText());
                    entry.setSecret(secretField.getText());
                    entry.setAlgorithm(algorithmBox.getValue());
                    entry.setDigits(digitsSpinner.getValue());
                    entry.setPeriod(periodSpinner.getValue());
                    return entry;
                } catch (Exception e) {
                    NotificationUtil.showError("输入错误", e.getMessage());
                    return null;
                }
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

    private void showQRCode(AuthEntry entry) {
        Dialog<AuthEntry> dialog = DialogUtil.createCommonDataDialog("二维码");
        DialogPane dialogPane = dialog.getDialogPane();

        // 创建二维码图片视图
        ImageView qrView = new ImageView(authService.generateQRCode(entry));
        qrView.setFitWidth(300);
        qrView.setFitHeight(300);
        qrView.setPreserveRatio(true);

        // 创建卡片容器
        Card card = new Card();
        card.setMaxWidth(320);
        card.setMaxHeight(320);
        card.setBody(qrView);  // 使用 setBody 方法设置内容

        dialogPane.setContent(card);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.show();
    }

    private void handleDelete(AuthEntry entry) {
        if (NotificationUtil.showConfirm(
                "删除验证器",
                "确定要删除验证器 " + entry.getName() + " 吗？"
        )) {
            authEntries.remove(entry);
            saveHistory();
            NotificationUtil.showSuccess("删除成功", "验证器已删除");
        }
    }

    private void handleImport() {
        try {
            List<AuthEntry> entries = FileUtil.importFromJson(
                    new TypeReference<>() {
                    },
                    "导入验证器配置",
                    getScene().getWindow()
            );
            if (entries != null && !entries.isEmpty()) {
                authEntries.addAll(entries);
                saveHistory();
                NotificationUtil.showSuccess("导入成功", "成功导入 " + entries.size() + " 个验证器");
            }
        } catch (Exception e) {
            LOGGER.error("导入验证器失败", e);
            NotificationUtil.showError("导入失败", e.getMessage());
        }
    }

    private void handleExport() {
        if (authEntries.isEmpty()) {
            NotificationUtil.showWarning("导出失败", "没有可导出的验证器");
            return;
        }

        try {
            FileUtil.exportToJson(
                    new ArrayList<>(authEntries),
                    "导出验证器配置",
                    getScene().getWindow()
            );
        } catch (Exception e) {
            LOGGER.error("导出验证器失败", e);
            NotificationUtil.showError("导出失败", e.getMessage());
        }
    }

    private void initializeData() {
        try {
            List<AuthEntry> entries = HistoryUtil.getHistory("auth", new TypeReference<List<AuthEntry>>() {
            });
            if (entries != null) {
                authEntries.setAll(entries);
            }
        } catch (Exception e) {
            LOGGER.error("加载历史配置失败", e);
            NotificationUtil.showError("加载失败", e.getMessage());
        }
    }

    private void saveHistory() {
        try {
            HistoryUtil.saveHistory("auth", new ArrayList<>(authEntries));
        } catch (Exception e) {
            LOGGER.error("保存历史配置失败", e);
            NotificationUtil.showError("保存失败", e.getMessage());
        }
    }

    private void startCountdown() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateCodes())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateCodes() {
        long currentTime = System.currentTimeMillis() / 1000L;
        int remainingSeconds = 30 - (int) (currentTime % 30);
        countdownLabel.setText(String.format("%d 秒后更新", remainingSeconds));
        authTable.refresh();
    }

    private void copyToClipboard(String code) {
        // 复制到剪贴板
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(code);
        clipboard.setContent(content);

        // 直接显示通知，因为已经在构造函数中初始化了通知上下文
        NotificationUtil.showSuccess("验证码已复制到剪贴板");
    }
} 