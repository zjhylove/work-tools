package com.zjhy.love.worktools.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.ObjectStorageConfig;
import com.zjhy.love.worktools.model.StorageObject;
import com.zjhy.love.worktools.service.AliyunOssService;
import com.zjhy.love.worktools.service.ObjectStorageService;
import com.zjhy.love.worktools.service.TencentCosService;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class ObjectStorageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectStorageController.class);

    @FXML
    private ComboBox<String> providerComboBox;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private TableView<StorageObject> objectTable;
    
    @FXML
    private ProgressBar progressBar;
    
    private ObjectStorageService storageService;
    private ObjectStorageConfig config;
    private final DoubleProperty uploadProgress = new SimpleDoubleProperty(0);
    private final ObservableList<StorageObject> objects = FXCollections.observableArrayList();

    private static final String TOOL_NAME = "object-storage";

    @FXML
    public void initialize() {
        initializeTable();
        initializeProviderComboBox();
        bindProgress();
        
        // 恢复历史配置
        restoreConfig();
    }

    private void initializeTable() {
        objectTable.getStyleClass().addAll("table", "table-hover", "table-striped");
        objectTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // 对象键列
        TableColumn<StorageObject, String> keyColumn = new TableColumn<>("对象名称");
        keyColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().key()));
        keyColumn.setCellFactory(col -> createCenteredCell());
        
        // 大小列
        TableColumn<StorageObject, String> sizeColumn = new TableColumn<>("大小");
        sizeColumn.setCellValueFactory(data -> new SimpleStringProperty(formatSize(data.getValue().size())));
        sizeColumn.setCellFactory(col -> createCenteredCell());
        
        // 修改时间列
        TableColumn<StorageObject, String> timeColumn = new TableColumn<>("修改时间");
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().lastModified()));
        timeColumn.setCellFactory(col -> createCenteredCell());
        
        // 操作列
        TableColumn<StorageObject, Void> actionColumn = new TableColumn<>("操作");
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button downloadBtn = new Button("下载");
            private final Button deleteBtn = new Button("删除");
            private final Button urlBtn = new Button("获取链接");
            private final HBox box = new HBox(5, downloadBtn, deleteBtn, urlBtn);
            
            {
                downloadBtn.getStyleClass().addAll("btn", "btn-primary", "btn-xs");
                deleteBtn.getStyleClass().addAll("btn", "btn-danger", "btn-xs");
                urlBtn.getStyleClass().addAll("btn", "btn-info", "btn-xs");
                box.setAlignment(Pos.CENTER);
                
                downloadBtn.setOnAction(e -> handleDownload(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
                urlBtn.setOnAction(e -> handleGetUrl(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        objectTable.getColumns().setAll(keyColumn, sizeColumn, timeColumn, actionColumn);
        objectTable.setItems(objects);

        // 设置列宽比例
        keyColumn.prefWidthProperty().bind(objectTable.widthProperty().multiply(0.4));
        sizeColumn.prefWidthProperty().bind(objectTable.widthProperty().multiply(0.15));
        timeColumn.prefWidthProperty().bind(objectTable.widthProperty().multiply(0.25));
        actionColumn.prefWidthProperty().bind(objectTable.widthProperty().multiply(0.2));
    }

    private void initializeProviderComboBox() {
        providerComboBox.setItems(FXCollections.observableArrayList("阿里云OSS", "腾讯云COS"));
        
        // 添加修改配置的按钮
        Button editConfigBtn = new Button("修改配置");
        editConfigBtn.getStyleClass().addAll("btn", "btn-default", "btn-xs");
        editConfigBtn.setOnAction(e -> {
            if (providerComboBox.getValue() != null) {
                showConfigDialog(providerComboBox.getValue());
            } else {
                NotificationUtil.showError("错误", "请先选择存储服务提供商");
            }
        });
        
        // 在 JavaFX 应用程序线程上更新 UI
        Platform.runLater(() -> {
            // 获取父容器
            HBox parent = (HBox) providerComboBox.getParent();
            if (parent != null) {
                // 获取 ComboBox 的位置
                int index = parent.getChildren().indexOf(providerComboBox);
                if (index >= 0) {
                    // 移除原有的 ComboBox
                    parent.getChildren().remove(providerComboBox);
                    // 创建新的容器并添加组件
                    HBox providerBox = new HBox(5);
                    providerBox.setAlignment(Pos.CENTER_LEFT);
                    providerBox.getChildren().addAll(providerComboBox, editConfigBtn);
                    // 添加新的容器
                    parent.getChildren().add(index, providerBox);
                }
            }
            // 监听选择变化
            providerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
                if (val != null && !val.equals(old) && old == null) {  // 只在首次选择时弹出配置框
                    showConfigDialog(val);
                }
            });
        });
    }

    private void bindProgress() {
        progressBar.progressProperty().bind(uploadProgress);
    }

    private void showConfigDialog(String provider) {
        Dialog<ObjectStorageConfig> dialog = new Dialog<>();
        dialog.setTitle("配置" + provider);
        dialog.setHeaderText(null);
        
        // 设置对话框样式
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().addAll(objectTable.getScene().getStylesheets());
        //dialogPane.getStyleClass().addAll("panel", "panel-body");
        
        // 创建表单字段，并设置初始值
        String initialAccessKey = (config != null && provider.equals(config.getProvider())) ? 
            config.getAccessKeyId() : "";
        String initialSecret = (config != null && provider.equals(config.getProvider())) ? 
            config.getAccessKeySecret() : "";
        String initialEndpoint = (config != null && provider.equals(config.getProvider())) ? 
            config.getEndpoint() : "";
        String initialBucket = (config != null && provider.equals(config.getProvider())) ? 
            config.getBucket() : "";

        StringField accessKeyField = Field.ofStringType(initialAccessKey)
            .label("AccessID")
            .required("请输入AccessKey ID");
            
        StringField secretField = Field.ofStringType(initialSecret)
            .label("AccessSecret")
            .required("请输入AccessKey Secret");
            
        StringField endpointField = Field.ofStringType(initialEndpoint)
            .label("Endpoint")
            .required("请输入Endpoint")
            .validate(CustomValidator.forPredicate(
                endpoint -> endpoint.startsWith("http://") || endpoint.startsWith("https://"),
                "Endpoint 必须以 http:// 或 https:// 开头"
            ));
            
        StringField bucketField = Field.ofStringType(initialBucket)
            .label("Bucket")
            .required("请输入Bucket")
            .validate(CustomValidator.forPredicate(
                bucket -> bucket.matches("^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$"),
                "Bucket 名称必须符合规范：\n" +
                "1) 只能包括小写字母，数字和短横线（-）\n" +
                "2) 必须以小写字母或者数字开头和结尾\n" +
                "3) 长度必须在 3-63 字节之间"
            ));
        
        // 创建字段列表
        List<Field<?>> fields = new ArrayList<>();
        fields.add(accessKeyField);
        fields.add(secretField);
        fields.add(endpointField);
        fields.add(bucketField);

        // 腾讯云需要额外的Region字段
        StringField regionField = null;
        if ("腾讯云COS".equals(provider)) {
            String initialRegion = (config != null && provider.equals(config.getProvider())) ? 
                config.getRegion() : "";
            regionField = Field.ofStringType(initialRegion)
                .label("Region")
                .required("请输入Region");
            fields.add(regionField);
        }

        // 创建表单
        Form form = Form.of(
            Group.of(fields.toArray(new Field[0]))
        );

        // 创建表单渲染器
        FormRenderer formRenderer = new FormRenderer(form);
        formRenderer.setPadding(new Insets(5));

        // 设置对话框内容
        VBox content = new VBox(5);
        content.getStyleClass().addAll("panel", "panel-body");
        content.getChildren().add(formRenderer);
        dialogPane.setContent(content);

        // 添加按钮
        ButtonType saveButton = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        // 设置按钮样式
        Node saveButtonNode = dialogPane.lookupButton(saveButton);
        if (saveButtonNode instanceof Button btn) {
            btn.getStyleClass().setAll("btn", "btn-primary");
        }
        
        Node cancelButtonNode = dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelButtonNode instanceof Button btn) {
            btn.getStyleClass().setAll("btn", "btn-default");
        }

        // 设置按钮栏样式
        dialogPane.lookup(".button-bar").getStyleClass().addAll("panel");

        // 设置结果转换器
        final StringField finalRegionField = regionField;
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton && form.isValid()) {
                form.persist();
                try {
                    ObjectStorageConfig config = new ObjectStorageConfig();
                    config.setProvider(provider);
                    config.setAccessKeyId(accessKeyField.getValue());
                    config.setAccessKeySecret(secretField.getValue());
                    config.setEndpoint(endpointField.getValue());
                    config.setBucket(bucketField.getValue());
                    if (finalRegionField != null) {
                        config.setRegion(finalRegionField.getValue());
                    }
                    return config;
                } catch (Exception e) {
                    NotificationUtil.showError("配置错误", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // 设置对话框最小宽度
        dialog.getDialogPane().setMinWidth(600);

        // 显示对话框并处理结果
        dialog.showAndWait().ifPresent(cfg -> {
            config = cfg;
            // 保存配置到历史记录
            HistoryUtil.saveHistory(TOOL_NAME, config);
            initializeStorageService();
            refreshObjects();
        });
    }

    @FXML
    private void handleUpload() {
        if (storageService == null) {
            NotificationUtil.showError("错误", "请先配置存储服务");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(objectTable.getScene().getWindow());
        
        if (file != null) {
            uploadProgress.set(0);
            new Thread(() -> {
                try {
                    storageService.uploadFile(file.getName(), file, uploadProgress);
                    refreshObjects();
                    NotificationUtil.showSuccess("上传成功", "文件已上传");
                } catch (Exception e) {
                    NotificationUtil.showError("上传失败", e.getMessage());
                }
            }).start();
        }
    }

    private void handleDownload(StorageObject obj) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(obj.key());
        File file = fileChooser.showSaveDialog(objectTable.getScene().getWindow());
        
        if (file != null) {
            uploadProgress.set(0);
            new Thread(() -> {
                try {
                    storageService.downloadFile(obj.key(), file, uploadProgress);
                    NotificationUtil.showSuccess("下载成功", "文件已下载");
                } catch (Exception e) {
                    NotificationUtil.showError("下载失败", e.getMessage());
                }
            }).start();
        }
    }

    private void handleDelete(StorageObject obj) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setContentText("确定要删除 " + obj.key() + " 吗？");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    storageService.deleteObject(obj.key());
                    refreshObjects();
                    NotificationUtil.showSuccess("删除成功", "对象已删除");
                } catch (Exception e) {
                    NotificationUtil.showError("删除失败", e.getMessage());
                }
            }
        });
    }

    private void handleGetUrl(StorageObject obj) {
        String url = storageService.getObjectUrl(obj.key());
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(url);
        clipboard.setContent(content);
        NotificationUtil.showSuccess("获取成功", "URL已复制到剪贴板");
    }

    @FXML
    private void refreshObjects() {
        if (storageService != null) {
            try {
                String prefix = searchField.getText();
                List<StorageObject> result = storageService.listObjects(prefix, 1000);
                objects.setAll(result);
            } catch (Exception e) {
                NotificationUtil.showError("列举对象失败", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSearch() {
        refreshObjects();
    }

    private void initializeStorageService() {
        if (storageService != null) {
            storageService.shutdown();
        }

        try {
            if ("阿里云OSS".equals(providerComboBox.getValue())) {
                storageService = new AliyunOssService();
            } else {
                storageService = new TencentCosService();
            }
            storageService.init(config);
        } catch (Exception e) {
            NotificationUtil.showError("初始化失败", e.getMessage());
            storageService = null;
        }
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    // 创建居中对齐的单元格
    private TableCell<StorageObject, String> createCenteredCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            }
        };
    }

    private void restoreConfig() {
        ObjectStorageConfig savedConfig = HistoryUtil.getHistory(TOOL_NAME, ObjectStorageConfig.class);
        if (savedConfig != null) {
            config = savedConfig;
            providerComboBox.setValue(config.getProvider());
            initializeStorageService();
            //refreshObjects();
        }
    }
}
