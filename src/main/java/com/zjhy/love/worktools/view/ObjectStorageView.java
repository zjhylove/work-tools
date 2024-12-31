package com.zjhy.love.worktools.view;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.zjhy.love.worktools.common.ShutdownHook;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.ObjectStorageConfig;
import com.zjhy.love.worktools.model.StorageObject;
import com.zjhy.love.worktools.service.AliyunOssService;
import com.zjhy.love.worktools.service.ObjectStorageService;
import com.zjhy.love.worktools.service.TencentCosService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.controlsfx.glyphfont.Glyph;

import java.io.File;
import java.util.List;

public class ObjectStorageView extends BaseView implements ShutdownHook {

    private static final String ALIYUN_OSS = "阿里云 OSS";
    private static final String TENCENT_COS = "腾讯云 COS";

    private final ComboBox<String> providerComboBox = new ComboBox<>();
    private final TextField accessKeyField = new TextField();
    private final PasswordField secretKeyField = new PasswordField();
    private final TextField endpointField = new TextField();
    private final TextField bucketField = new TextField();
    private final TextField regionField = new TextField();
    private final TextField searchField = new TextField();
    private final TableView<StorageObject> objectTable = new TableView<>();
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label();
    private final ObservableList<StorageObject> objects = FXCollections.observableArrayList();
    private ObjectStorageService storageService;
    // 添加配置缓存
    private ObjectStorageConfig aliyunConfig;
    private ObjectStorageConfig tencentConfig;

    public ObjectStorageView() {
        // 初始化服务商选项
        providerComboBox.getItems().addAll(ALIYUN_OSS, TENCENT_COS);
        // 默认选择阿里云 OSS
        providerComboBox.setValue(ALIYUN_OSS);

        // 创建组件
        VBox configSection = createConfigSection();
        HBox toolbar = createToolbar();
        configureObjectTable();
        VBox.setVgrow(objectTable, Priority.ALWAYS);
        HBox statusBar = createStatusBar();

        // 使用基类方法添加内容
        addContent(configSection, toolbar, objectTable, statusBar);

        // 初始化数据
        initializeData();
    }

    private VBox createConfigSection() {
        VBox container = new VBox(10);
        container.getStyleClass().add("form-section");

        // 创建表单网格
        GridPane grid = new GridPane();
        grid.setHgap(20);  // 增加水平间距
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        // 配置列宽
        ColumnConstraints labelColumn1 = new ColumnConstraints();
        labelColumn1.setHalignment(HPos.RIGHT);
        labelColumn1.setPrefWidth(80);

        ColumnConstraints fieldColumn1 = new ColumnConstraints();
        fieldColumn1.setHgrow(Priority.SOMETIMES);
        fieldColumn1.setPrefWidth(200);

        ColumnConstraints labelColumn2 = new ColumnConstraints();
        labelColumn2.setHalignment(HPos.RIGHT);
        labelColumn2.setPrefWidth(80);

        ColumnConstraints fieldColumn2 = new ColumnConstraints();
        fieldColumn2.setHgrow(Priority.SOMETIMES);
        fieldColumn2.setPrefWidth(200);

        grid.getColumnConstraints().addAll(labelColumn1, fieldColumn1, labelColumn2, fieldColumn2);

        // 配置输入字段
        providerComboBox.setPromptText("选择存储服务");
        providerComboBox.setOnAction(e -> handleProviderChange());

        accessKeyField.setPromptText("Access Key ID");
        secretKeyField.setPromptText("Access Key Secret");
        endpointField.setPromptText("Endpoint");
        bucketField.setPromptText("Bucket");
        regionField.setPromptText("Region (腾讯云需要)");

        // 添加到网格 - 两列布局
        int row = 0;
        grid.addRow(row++,
                new Label("服务商:"), providerComboBox,
                new Label("Endpoint:"), endpointField
        );
        grid.addRow(row++,
                new Label("Access Key:"), accessKeyField,
                new Label("Secret Key:"), secretKeyField
        );
        grid.addRow(row++,
                new Label("Bucket:"), bucketField,
                new Label("Region:"), regionField
        );

        // 连接按钮
        Button connectButton = new Button("连接", new Glyph("FontAwesome", "LINK"));
        connectButton.getStyleClass().addAll("button-outlined", "accent");
        connectButton.setOnAction(e -> handleConnect());

        // 按钮容器 - 改为左对齐
        HBox buttonBox = new HBox(connectButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        container.getChildren().addAll(grid, buttonBox);
        return container;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);  // 恢复左对齐

        searchField.setPromptText("搜索对象");
        searchField.setPrefWidth(300);
        searchField.setOnAction(e -> handleSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchButton = new Button("搜索", new Glyph("FontAwesome", "SEARCH"));
        searchButton.getStyleClass().add("button-outlined");
        searchButton.setOnAction(e -> handleSearch());

        Button uploadButton = new Button("上传", new Glyph("FontAwesome", "UPLOAD"));
        uploadButton.getStyleClass().addAll("button-outlined", "success");
        uploadButton.setOnAction(e -> handleUpload());

        Button refreshButton = new Button("刷新", new Glyph("FontAwesome", "REFRESH"));
        refreshButton.getStyleClass().add("button-outlined");
        refreshButton.setOnAction(e -> handleSearch());

        toolbar.getChildren().addAll(searchField, searchButton, uploadButton, refreshButton);
        return toolbar;
    }

    private void configureObjectTable() {
        objectTable.getStyleClass().add("table-striped");
        objectTable.setPlaceholder(new Label("暂无数据"));  // 添加空数据提示
        objectTable.setFixedCellSize(40);  // 设置固定行高
        objectTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);  // 设置列宽调整策略
        ObservableList<TableColumn<StorageObject, ?>> columns = objectTable.getColumns();

        // 对象名称列
        TableColumn<StorageObject, String> keyColumn = new TableColumn<>("对象名称");
        keyColumn.setCellValueFactory(data -> data.getValue().keyProperty());
        keyColumn.setPrefWidth(300);
        columns.add(keyColumn);

        // 大小列
        TableColumn<StorageObject, String> sizeColumn = new TableColumn<>("大小");
        sizeColumn.setCellValueFactory(data -> data.getValue().sizeProperty());
        sizeColumn.setPrefWidth(100);
        columns.add(sizeColumn);

        // 修改时间列
        TableColumn<StorageObject, String> lastModifiedColumn = new TableColumn<>("修改时间");
        lastModifiedColumn.setCellValueFactory(data -> data.getValue().lastModifiedProperty());
        lastModifiedColumn.setPrefWidth(150);
        columns.add(lastModifiedColumn);

        // 操作列
        TableColumn<StorageObject, Void> actionColumn = new TableColumn<>("操作");
        actionColumn.setCellFactory(col -> new ActionTableCell());
        actionColumn.setPrefWidth(200);
        columns.add(actionColumn);

        //设置数据源
        objectTable.setItems(objects);

        // 设置表格容器的内边距
        getContentBox().setPadding(new Insets(25, 0, 5, 0));
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);

        statusLabel.getStyleClass().add("text-muted");
        progressBar.setPrefWidth(200);
        progressBar.setVisible(false);

        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        statusBar.getChildren().addAll(statusLabel, progressBar);
        return statusBar;
    }

    private void handleProviderChange() {
        String provider = providerComboBox.getValue();
        regionField.setDisable(!TENCENT_COS.equals(provider));

        // 保存当前配置
        if (storageService != null) {
            saveCurrentConfig();
            storageService.shutdown();
            storageService = null;
            objects.clear();
        }

        // 切换配置
        if (ALIYUN_OSS.equals(provider)) {
            if (aliyunConfig != null) {
                restoreConfig(aliyunConfig);
            } else {
                clearConfig();
            }
        } else if (TENCENT_COS.equals(provider)) {
            if (tencentConfig != null) {
                restoreConfig(tencentConfig);
            } else {
                clearConfig();
            }
        }
    }

    private void saveCurrentConfig() {
        ObjectStorageConfig config = createObjectStorageConfig();
        if (ALIYUN_OSS.equals(config.getProvider())) {
            aliyunConfig = config;
        } else if (TENCENT_COS.equals(config.getProvider())) {
            tencentConfig = config;
        }
    }

    private void restoreConfig(ObjectStorageConfig config) {
        accessKeyField.setText(config.getAccessKeyId());
        secretKeyField.setText(config.getAccessKeySecret());
        endpointField.setText(config.getEndpoint());
        bucketField.setText(config.getBucket());
        regionField.setText(config.getRegion());
    }

    private void clearConfig() {
        accessKeyField.clear();
        secretKeyField.clear();
        endpointField.clear();
        bucketField.clear();
        regionField.clear();
    }

    private void initializeData() {
        try {
            ObjectStorageConfig config = HistoryUtil.getHistory("object-storage", ObjectStorageConfig.class);
            if (config != null) {
                // 根据历史配置初始化对应服务商的配置
                if (ALIYUN_OSS.equals(config.getProvider())) {
                    aliyunConfig = config;
                } else if (TENCENT_COS.equals(config.getProvider())) {
                    tencentConfig = config;
                }

                // 设置当前选中的服务商并恢复配置
                providerComboBox.setValue(config.getProvider());
                restoreConfig(config);
                handleProviderChange();
            }
        } catch (Exception e) {
            NotificationUtil.showError("加载配置失败", e.getMessage());
        }
    }

    private void handleConnect() {
        try {
            // 如果已连接，则断开连接
            if (storageService != null) {
                storageService.shutdown();
                storageService = null;
                objects.clear();
                statusLabel.setText("已断开连接");
                return;
            }

            // 校验存储服务商选择
            String provider = providerComboBox.getValue();
            if (provider == null || provider.isEmpty()) {
                NotificationUtil.showWarning("请选择存储服务商");
                return;
            }

            // 校验基本配置
            if (isEmptyOrBlank(accessKeyField.getText())) {
                NotificationUtil.showWarning("请输入 Access Key");
                accessKeyField.requestFocus();
                return;
            }

            if (isEmptyOrBlank(secretKeyField.getText())) {
                NotificationUtil.showWarning("请输入 Secret Key");
                secretKeyField.requestFocus();
                return;
            }

            if (isEmptyOrBlank(endpointField.getText())) {
                NotificationUtil.showWarning("请输入 Endpoint");
                endpointField.requestFocus();
                return;
            }

            if (isEmptyOrBlank(bucketField.getText())) {
                NotificationUtil.showWarning("请输入 Bucket");
                bucketField.requestFocus();
                return;
            }

            // 根据不同服务商进行特定校验
            if (ALIYUN_OSS.equals(provider)) {
                // 校验阿里云 OSS 配置
                if (!endpointField.getText().contains("aliyuncs.com")) {
                    NotificationUtil.showWarning("阿里云 OSS Endpoint 格式不正确，应包含 aliyuncs.com");
                    endpointField.requestFocus();
                    return;
                }
            } else if (TENCENT_COS.equals(provider)) {
                // 校验腾讯云 COS 配置
                if (isEmptyOrBlank(regionField.getText())) {
                    NotificationUtil.showWarning("腾讯云 COS 必须指定 Region");
                    regionField.requestFocus();
                    return;
                }

                if (!endpointField.getText().contains("myqcloud.com")) {
                    NotificationUtil.showWarning("腾讯云 COS Endpoint 格式不正确，应包含 myqcloud.com");
                    endpointField.requestFocus();
                    return;
                }
            }

            // 创建配置对象
            ObjectStorageConfig config = new ObjectStorageConfig();
            config.setProvider(provider);
            config.setAccessKeyId(accessKeyField.getText().trim());
            config.setAccessKeySecret(secretKeyField.getText().trim());
            config.setEndpoint(endpointField.getText().trim());
            config.setBucket(bucketField.getText().trim());
            if (TENCENT_COS.equals(provider)) {
                config.setRegion(regionField.getText().trim());
            }

            // 创建存储服务实例
            storageService = ALIYUN_OSS.equals(provider) ?
                    new AliyunOssService() : new TencentCosService();

            // 初始化连接
            storageService.init(config);
            handleSearch();
            saveHistory();
            statusLabel.setText("已连接");
            NotificationUtil.showSuccess("连接成功", "已成功连接到存储服务");

        } catch (Exception e) {
            NotificationUtil.showError("连接失败", e.getMessage());
        }
    }

    // 添加辅助方法检查字符串是否为空或只包含空白字符
    private boolean isEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void handleSearch() {
        if (storageService == null) {
            NotificationUtil.showError("错误", "请先连接存储服务");
            return;
        }

        try {
            String prefix = searchField.getText();
            List<StorageObject> result = storageService.listObjects(prefix, 1000);
            objects.setAll(result);
            statusLabel.setText(String.format("共 %d 个对象", result.size()));
        } catch (Exception e) {
            NotificationUtil.showError("搜索失败", e.getMessage());
        }
    }

    private void handleUpload() {
        if (storageService == null) {
            NotificationUtil.showError("错误", "请先连接存储服务");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择要上传的文件");
        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            // 创建文件名修改对话框
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("上传文件");
            dialog.setHeaderText(null);

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStyleClass().add("surface-card");

            // 创建表单
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            TextField keyField = new TextField(file.getName());
            keyField.setPromptText("对象名称");
            keyField.setPrefWidth(300);

            grid.addRow(0, new Label("原文件名:"), new Label(file.getName()));
            grid.addRow(1, new Label("对象名称:"), keyField);

            dialogPane.setContent(grid);

            ButtonType uploadButton = new ButtonType("上传", ButtonBar.ButtonData.OK_DONE);
            dialogPane.getButtonTypes().addAll(uploadButton, ButtonType.CANCEL);

            // 设置按钮样式
            styleDialogButtons(dialogPane);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == uploadButton) {
                    return keyField.getText();
                }
                return null;
            });

            // 显示对话框并处理结果
            dialog.showAndWait().ifPresent(key -> {
                try {
                    progressBar.setVisible(true);
                    SimpleDoubleProperty progress = new SimpleDoubleProperty();
                    progress.addListener((obs, oldVal, newVal) -> {
                        progressBar.setProgress(newVal.doubleValue());
                        if (newVal.doubleValue() >= 1.0) {
                            progressBar.setVisible(false);
                        }
                    });

                    storageService.uploadFile(key, file, progress);
                    handleSearch();
                    NotificationUtil.showSuccess("上传成功", "文件已成功上传");
                } catch (Exception e) {
                    progressBar.setVisible(false);
                    NotificationUtil.showError("上传失败", e.getMessage());
                }
            });
        }
    }

    private void handleDownload(StorageObject obj) {
        if (storageService == null) {
            NotificationUtil.showError("错误", "请先连接存储服务");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择保存位置");
        fileChooser.setInitialFileName(FileNameUtil.getName(obj.getKey()));
        File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try {
                progressBar.setVisible(true);
                SimpleDoubleProperty progress = new SimpleDoubleProperty();
                progress.addListener((obs, oldVal, newVal) -> {
                    progressBar.setProgress(newVal.doubleValue());
                    if (newVal.doubleValue() >= 1.0) {
                        progressBar.setVisible(false);
                    }
                });

                storageService.downloadFile(obj.getKey(), file, progress);
                NotificationUtil.showSuccess("下载成功", "文件已成功下载");
            } catch (Exception e) {
                progressBar.setVisible(false);
                NotificationUtil.showError("下载失败", e.getMessage());
            }
        }
    }

    private void handleDelete(StorageObject obj) {
        if (storageService == null) {
            NotificationUtil.showError("错误", "请先连接存储服务");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除对象 " + obj.getKey() + " 吗？");

        styleAlert(alert);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    storageService.deleteObject(obj.getKey());
                    objects.remove(obj);
                    NotificationUtil.showSuccess("删除成功", "对象已成功删除");
                } catch (Exception e) {
                    NotificationUtil.showError("删除失败", e.getMessage());
                }
            }
        });
    }

    private void saveHistory() {
        try {
            ObjectStorageConfig config = createObjectStorageConfig();
            HistoryUtil.saveHistory("object-storage", config);
        } catch (Exception e) {
            NotificationUtil.showError("保存配置失败", e.getMessage());
        }
    }

    private ObjectStorageConfig createObjectStorageConfig() {
        ObjectStorageConfig config = new ObjectStorageConfig();
        config.setProvider(providerComboBox.getValue());
        config.setAccessKeyId(accessKeyField.getText());
        config.setAccessKeySecret(secretKeyField.getText());
        config.setEndpoint(endpointField.getText());
        config.setBucket(bucketField.getText());
        config.setRegion(regionField.getText());
        return config;
    }

    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("surface-card");
        dialogPane.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            if (buttonType == ButtonType.OK) {
                button.getStyleClass().addAll("button-outlined", "danger");
            } else {
                button.getStyleClass().add("button-outlined");
            }
        });
    }

    /**
     * 设置对话框按钮的样式
     */
    private void styleDialogButtons(DialogPane dialogPane) {
        dialogPane.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                button.getStyleClass().addAll("button-outlined", "success");
            } else {
                button.getStyleClass().add("button-outlined");
            }
        });
    }

    @Override
    public void shutdown() {
        if (CollectionUtil.isNotEmpty(objects)) {
            objects.clear();
        }
        if (storageService != null) {
            storageService.shutdown();
        }
    }

    // 操作列单元格
    private class ActionTableCell extends TableCell<StorageObject, Void> {
        private final HBox container;

        public ActionTableCell() {
            Button downloadButton = new Button("下载", new Glyph("FontAwesome", "DOWNLOAD"));
            downloadButton.getStyleClass().addAll("button-small", "accent");

            Button deleteButton = new Button("删除", new Glyph("FontAwesome", "TRASH"));
            deleteButton.getStyleClass().addAll("button-small", "danger");

            container = new HBox(5, downloadButton, deleteButton);
            container.setAlignment(Pos.CENTER);

            downloadButton.setOnAction(e -> {
                StorageObject obj = getTableRow().getItem();
                if (obj != null) {
                    handleDownload(obj);
                }
            });

            deleteButton.setOnAction(e -> {
                StorageObject obj = getTableRow().getItem();
                if (obj != null) {
                    handleDelete(obj);
                }
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : container);
        }
    }
} 