package com.zjhy.love.worktools.controller;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.ForwardEntry;
import com.zjhy.love.worktools.model.IpForwardConfig;
import com.zjhy.love.worktools.model.NacosConfig;
import com.zjhy.love.worktools.service.SshService;
import com.zjhy.love.worktools.service.NacosService;
import com.zjhy.love.worktools.service.HttpProxyService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.zjhy.love.worktools.common.util.RetryUtil;
import com.zjhy.love.worktools.common.util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * IP转发控制器
 */
public class IpForwardController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpForwardController.class);

    @FXML
    private VBox sshFormContainer;
    
    @FXML
    private TableView<ForwardEntry> forwardTable;

    private Form sshForm;
    private final StringProperty hostProperty = new SimpleStringProperty("");
    private final StringProperty portProperty = new SimpleStringProperty("22");
    private final StringProperty usernameProperty = new SimpleStringProperty("");
    private final StringProperty passwordProperty = new SimpleStringProperty("");
    
    private final SshService sshService = new SshService();
    private final ObservableList<ForwardEntry> forwardEntries = FXCollections.observableArrayList();

    private boolean isConnected = false;

    @FXML
    private Label statusLabel;

    @FXML
    private Button connectButton;
    
    @FXML
    private Button disconnectButton;
    
    @FXML
    private Button startForwardButton;

    @FXML
    private TabPane tabPane;
    
    @FXML
    private VBox nacosFormContainer;
    
    @FXML
    private TableView<String> serviceTable;

    private final NacosService nacosService = new NacosService();
    private final HttpProxyService httpProxyService = new HttpProxyService();
    
    private Form nacosForm;
    private final StringProperty serverAddrProperty = new SimpleStringProperty("");
    private final StringProperty namespaceProperty = new SimpleStringProperty("");
    private final StringProperty nacosUsernameProperty = new SimpleStringProperty("");
    private final StringProperty nacosPasswordProperty = new SimpleStringProperty("");
    private final ObservableList<String> serviceNames = FXCollections.observableArrayList();

    @FXML
    private Label nacosStatusLabel;
    
    @FXML
    private Button nacosConnectButton;
    
    @FXML
    private Button nacosDisconnectButton;
    
    @FXML
    private Button startNacosForwardButton;
    
    private boolean isNacosConnected = false;
    private boolean isNacosForwarding = false;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    @FXML
    private TextField serviceSearchField;
    private final ObservableList<String> allServices = FXCollections.observableArrayList();
    
    private java.util.Timer reconnectTimer;
    private boolean autoReconnect = true;

    private final StringProperty groupNameProperty = new SimpleStringProperty("DEFAULT_GROUP");

    @FXML
    public void initialize() {
        initializeSshForm();
        initializeForwardTable();
        loadHistory();
        updateButtonStatus();
        updateStatusLabel();
        initializeNacosForm();
        initializeServiceTable();
        loadNacosHistory();
        initializeServiceSearch();
        startReconnectTimer();
    }

    private void initializeSshForm() {
        sshForm = Form.of(
            Group.of(
                Field.ofStringType(hostProperty)
                    .label("SSH服务器地址")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "SSH服务器地址不能为空"
                    )),
                    
                Field.ofStringType(portProperty)
                    .label("SSH服务器端口")
                    .validate(CustomValidator.forPredicate(
                        value -> value.matches("\\d+"),
                        "端口必须是数字"
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
                    ))
            )
        );

        FormRenderer formRenderer = new FormRenderer(sshForm);
        sshFormContainer.getChildren().add(formRenderer);
    }

    private void initializeForwardTable() {
        // 设置表格可编辑
        forwardTable.setEditable(true);
        
        // 创建列
        TableColumn<ForwardEntry, String> localHostCol = new TableColumn<>("本地地址");
        localHostCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getLocalHost()));
        localHostCol.setCellFactory(TextFieldTableCell.forTableColumn());
        localHostCol.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            if (newValue == null || newValue.trim().isEmpty()) {
                newValue = "127.0.0.1";
            }
            event.getRowValue().setLocalHost(newValue);
        });

        TableColumn<ForwardEntry, String> localPortCol = new TableColumn<>("本地端口");
        localPortCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getLocalPort())));
        localPortCol.setCellFactory(TextFieldTableCell.forTableColumn());
        localPortCol.setOnEditCommit(event -> {
            try {
                int port = Integer.parseInt(event.getNewValue());
                if (port > 0 && port < 65536) {
                    event.getRowValue().setLocalPort(port);
                } else {
                    throw new NumberFormatException("端口范围必须在1-65535之间");
                }
            } catch (NumberFormatException e) {
                NotificationUtil.showError("输入错误", "请输入有效的端口号(1-65535)");
                forwardTable.refresh();
            }
        });

        TableColumn<ForwardEntry, String> remoteHostCol = new TableColumn<>("远程地址");
        remoteHostCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRemoteHost()));
        remoteHostCol.setCellFactory(TextFieldTableCell.forTableColumn());
        remoteHostCol.setOnEditCommit(event -> {
            String newValue = event.getNewValue();
            if (newValue != null && !newValue.trim().isEmpty()) {
                event.getRowValue().setRemoteHost(newValue);
            } else {
                NotificationUtil.showError("输入错误", "远程地址不能为空");
                forwardTable.refresh();
            }
        });

        TableColumn<ForwardEntry, String> remotePortCol = new TableColumn<>("远程端口");
        remotePortCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getRemotePort())));
        remotePortCol.setCellFactory(TextFieldTableCell.forTableColumn());
        remotePortCol.setOnEditCommit(event -> {
            try {
                int port = Integer.parseInt(event.getNewValue());
                if (port > 0 && port < 65536) {
                    event.getRowValue().setRemotePort(port);
                } else {
                    throw new NumberFormatException("端口范围必须在1-65535之间");
                }
            } catch (NumberFormatException e) {
                NotificationUtil.showError("输入错误", "请输入有效的端口号(1-65535)");
                forwardTable.refresh();
            }
        });

        // 添加删除���
        TableColumn<ForwardEntry, Void> deleteCol = new TableColumn<>("操作");
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("删除");
            {
                deleteButton.setOnAction(event -> {
                    ForwardEntry entry = getTableView().getItems().get(getIndex());
                    forwardEntries.remove(entry);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        forwardTable.getColumns().addAll(localHostCol, localPortCol, remoteHostCol, remotePortCol, deleteCol);
        forwardTable.setItems(forwardEntries);

        // 优化表格编辑体验
        forwardTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                ForwardEntry selectedEntry = forwardTable.getSelectionModel().getSelectedItem();
                if (selectedEntry != null) {
                    forwardEntries.remove(selectedEntry);
                }
            }
        });
    }

    private void loadHistory() {
        IpForwardConfig history = HistoryUtil.getHistory("ipForward", IpForwardConfig.class);
        if (history != null) {
            hostProperty.set(history.getHost());
            portProperty.set(String.valueOf(history.getPort()));
            usernameProperty.set(history.getUsername());
            passwordProperty.set(history.getPassword());
            forwardEntries.setAll(history.getForwardEntries());
        }
    }

    private void saveHistory() {
        IpForwardConfig config = new IpForwardConfig();
        config.setHost(hostProperty.get());
        config.setPort(Integer.parseInt(portProperty.get()));
        config.setUsername(usernameProperty.get());
        config.setPassword(passwordProperty.get());
        config.setForwardEntries(new ArrayList<>(forwardEntries));
        HistoryUtil.saveHistory("ipForward", config);
    }

    @FXML
    private void handleConnect() {
        if (sshForm.isValid()) {
            try {
                sshService.connect(
                    hostProperty.get(),
                    Integer.parseInt(portProperty.get()),
                    usernameProperty.get(),
                    passwordProperty.get()
                );
                isConnected = true;
                updateButtonStatus();
                updateStatusLabel();
                NotificationUtil.showSuccess("连接成功", "SSH连接已建立");
                saveHistory();
            } catch (Exception e) {
                LOGGER.error("SSH连接失败", e);
                NotificationUtil.showError("连接失败", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDisconnect() {
        sshService.disconnect();
        isConnected = false;
        updateButtonStatus();
        updateStatusLabel();
        NotificationUtil.showSuccess("断开连接", "SSH连接已断开");
    }

    @FXML
    private void handleAddForward() {
        ForwardEntry entry = new ForwardEntry();
        entry.setLocalPort(1080);  // 设置默认端口
        entry.setRemotePort(1080);
        forwardEntries.add(entry);
        saveHistory();
    }

    @FXML
    private void handleStartForward() {
        if (!sshService.isConnected()) {
            NotificationUtil.showError("错误", "请先建立SSH连接");
            return;
        }

        if (forwardEntries.isEmpty()) {
            NotificationUtil.showError("错误", "请添加转发规则");
            return;
        }

        // 验证所有转发条目
        for (ForwardEntry entry : forwardEntries) {
            if (!validateForwardEntry(entry)) {
                return;
            }
        }

        // 执行转发
        for (ForwardEntry entry : forwardEntries) {
            try {
                sshService.addPortForwarding(
                    entry.getLocalHost(),
                    entry.getLocalPort(),
                    entry.getRemoteHost(),
                    entry.getRemotePort()
                );
            } catch (Exception e) {
                LOGGER.error("添加端口转发失败", e);
                NotificationUtil.showError("转发失败", e.getMessage());
                return;
            }
        }
        
        NotificationUtil.showSuccess("转发成功", "所有端口转发规则已启动");
        saveHistory();
    }

    private boolean validateForwardEntry(ForwardEntry entry) {
        if (entry.getRemoteHost() == null || entry.getRemoteHost().trim().isEmpty()) {
            NotificationUtil.showError("验证失败", "远程地址不能为空");
            return false;
        }
        
        if (entry.getLocalPort() <= 0 || entry.getLocalPort() > 65535) {
            NotificationUtil.showError("验证失败", "本地端口必须在1-65535之间");
            return false;
        }
        
        if (entry.getRemotePort() <= 0 || entry.getRemotePort() > 65535) {
            NotificationUtil.showError("验证失败", "远程端口必须在1-65535之间");
            return false;
        }
        
        return true;
    }

    private void updateButtonStatus() {
        // 更新按钮状态
        connectButton.setDisable(isConnected);
        disconnectButton.setDisable(!isConnected);
        startForwardButton.setDisable(!isConnected);
    }

    private void updateStatusLabel() {
        if (isConnected) {
            statusLabel.setText("已连接到: " + hostProperty.get());
            statusLabel.setTextFill(Color.GREEN);
        } else {
            statusLabel.setText("未连接");
            statusLabel.setTextFill(Color.RED);
        }
    }

    private void initializeNacosForm() {
        nacosForm = Form.of(
            Group.of(
                Field.ofStringType(serverAddrProperty)
                    .label("Nacos地址")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "Nacos地址不能为空"
                    )),
                    
                Field.ofStringType(namespaceProperty)
                    .label("命名空间")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "命名空间不能为空"
                    )),
                    
                Field.ofStringType(groupNameProperty)
                    .label("分组名称")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "分组名称不能为空"
                    )),
                    
                Field.ofStringType(nacosUsernameProperty)
                    .label("用户名")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "用户名不能为空"
                    )),
                    
                Field.ofStringType(nacosPasswordProperty)
                    .label("密码")
                    .validate(CustomValidator.forPredicate(
                        value -> !value.trim().isEmpty(),
                        "密码不能为空"
                    ))
            )
        );

        FormRenderer formRenderer = new FormRenderer(nacosForm);
        nacosFormContainer.getChildren().add(formRenderer);
    }

    private void initializeServiceTable() {
        TableColumn<String, String> serviceNameCol = new TableColumn<>("服务名称");
        serviceNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        
        // 添加删除列
        TableColumn<String, Void> deleteCol = new TableColumn<>("操作");
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("删除");
            {
                deleteButton.setOnAction(event -> {
                    String serviceName = getTableView().getItems().get(getIndex());
                    serviceNames.remove(serviceName);
                    saveNacosHistory();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        serviceTable.getColumns().addAll(serviceNameCol, deleteCol);
        serviceTable.setItems(serviceNames);
        
        // 添加键盘删除支持
        serviceTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                String selectedService = serviceTable.getSelectionModel().getSelectedItem();
                if (selectedService != null) {
                    serviceNames.remove(selectedService);
                    saveNacosHistory();
                }
            }
        });
    }

    private void loadNacosHistory() {
        NacosConfig history = HistoryUtil.getHistory("nacos", NacosConfig.class);
        if (history != null) {
            serverAddrProperty.set(history.getServerAddr());
            namespaceProperty.set(history.getNamespace());
            groupNameProperty.set(history.getGroupName());
            nacosUsernameProperty.set(history.getUsername());
            nacosPasswordProperty.set(history.getPassword());
            serviceNames.setAll(history.getServiceNames());
        }
    }

    private void saveNacosHistory() {
        NacosConfig config = new NacosConfig();
        config.setServerAddr(serverAddrProperty.get());
        config.setNamespace(namespaceProperty.get());
        config.setGroupName(groupNameProperty.get());
        config.setUsername(nacosUsernameProperty.get());
        config.setPassword(nacosPasswordProperty.get());
        config.setServiceNames(new ArrayList<>(serviceNames));
        HistoryUtil.saveHistory("nacos", config);
    }

    @FXML
    private void handleNacosConnect() {
        if (nacosForm.isValid()) {
            try {
                NacosConfig config = new NacosConfig();
                config.setServerAddr(serverAddrProperty.get());
                config.setNamespace(namespaceProperty.get());
                config.setUsername(nacosUsernameProperty.get());
                config.setPassword(nacosPasswordProperty.get());
                
                nacosService.connect(config);
                isNacosConnected = true;
                updateNacosButtonStatus();
                updateNacosStatusLabel();
                saveNacosHistory();
                NotificationUtil.showSuccess("连接成功", "Nacos连接已建立");
            } catch (Exception e) {
                LOGGER.error("Nacos连接失���", e);
                NotificationUtil.showError("连接失败", e.getMessage());
            }
        }
    }

    @FXML
    private void handleNacosDisconnect() {
        nacosService.shutdown();
        httpProxyService.shutdown();
        isNacosConnected = false;
        isNacosForwarding = false;
        updateNacosButtonStatus();
        updateNacosStatusLabel();
        NotificationUtil.showSuccess("断开连接", "Nacos连接已断开");
    }

    @FXML
    private void handleStartNacosForward() {
        if (!isNacosConnected) {
            NotificationUtil.showError("错误", "请先连接Nacos");
            return;
        }

        if (serviceNames.isEmpty()) {
            NotificationUtil.showError("错误", "请添加要转发的服务");
            return;
        }

        if (!sshService.isConnected()) {
            NotificationUtil.showError("错误", "请先建立SSH连接");
            return;
        }

        try {
            // 启动HTTP代理服务器
            httpProxyService.start(80);

            // 为每个服务创建转发
            for (String serviceName : serviceNames) {
                List<Instance> instances = nacosService.getServiceInstances(serviceName, groupNameProperty.get());
                if (!instances.isEmpty()) {
                    Instance instance = instances.get(0);
                    String target = instance.getIp() + ":" + instance.getPort();
                    
                    // 添加服务映射
                    httpProxyService.addServiceMapping(serviceName + ".service", target);
                    
                    // 创建SSH端口转发
                    sshService.addPortForwarding("127.0.0.1", instance.getPort(), instance.getIp(), instance.getPort());
                }
            }
            
            isNacosForwarding = true;
            updateNacosButtonStatus();
            updateNacosStatusLabel();
            NotificationUtil.showSuccess("转发成功", "服务转发已启动");
        } catch (Exception e) {
            LOGGER.error("启动服务转发失败", e);
            NotificationUtil.showError("转发失败", e.getMessage());
            httpProxyService.shutdown();
            isNacosForwarding = false;
            updateNacosButtonStatus();
            updateNacosStatusLabel();
        }
    }

    private void updateNacosButtonStatus() {
        nacosConnectButton.setDisable(isNacosConnected);
        nacosDisconnectButton.setDisable(!isNacosConnected);
        startNacosForwardButton.setDisable(!isNacosConnected || isNacosForwarding);
    }

    private void updateNacosStatusLabel() {
        if (isNacosConnected) {
            if (isNacosForwarding) {
                nacosStatusLabel.setText("已连接并转发中: " + serverAddrProperty.get());
                nacosStatusLabel.setTextFill(Color.GREEN);
            } else {
                nacosStatusLabel.setText("已连接: " + serverAddrProperty.get());
                nacosStatusLabel.setTextFill(Color.BLUE);
            }
        } else {
            nacosStatusLabel.setText("未连接");
            nacosStatusLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    private void handleAddService() {
        if (!isNacosConnected) {
            NotificationUtil.showError("错误", "请先连接Nacos");
            return;
        }

        try {
            // 获取可用服务列表，传入分组名称
            List<String> availableServices = nacosService.getServiceList(groupNameProperty.get());
            
            // 创建服务选择对话框
            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.setTitle("添加服务");
            dialog.setHeaderText("请选择要添加的服务");
            dialog.getItems().addAll(availableServices);
            
            // 添加搜索框
            TextField searchField = new TextField();
            searchField.setPromptText("搜���服务...");
            FilteredList<String> filteredServices = new FilteredList<>(
                FXCollections.observableArrayList(availableServices)
            );
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredServices.setPredicate(service ->
                    newValue == null || newValue.trim().isEmpty() ||
                    service.toLowerCase().contains(newValue.toLowerCase())
                );
            });
            
            ListView<String> serviceListView = new ListView<>(filteredServices);
            dialog.getDialogPane().setContent(new VBox(10, searchField, serviceListView));
            
            // 显示对话框
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(serviceName -> {
                if (!serviceNames.contains(serviceName)) {
                    serviceNames.add(serviceName);
                    saveNacosHistory();
                    
                    // 如果已经在转发中，则为新服务添加转发
                    if (isNacosForwarding) {
                        addServiceForward(serviceName);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("获取服务列表失败", e);
            NotificationUtil.showError("错误", "获取服务列表失败: " + e.getMessage());
        }
    }

    private void addServiceForward(String serviceName) {
        try {
            // 获取服务实例时传入分组名称
            List<Instance> instances = nacosService.getServiceInstances(serviceName, groupNameProperty.get());
            if (!instances.isEmpty()) {
                Instance instance = instances.get(0);
                String target = instance.getIp() + ":" + instance.getPort();
                
                // 添加服务映射
                httpProxyService.addServiceMapping(serviceName + ".service", target);
                
                // 创建SSH端口转发
                sshService.addPortForwarding(
                    "127.0.0.1", 
                    instance.getPort(), 
                    instance.getIp(), 
                    instance.getPort()
                );
                
                NotificationUtil.showSuccess("转发成功", 
                    String.format("服务 %s 已添加到转发列表", serviceName));
            }
        } catch (Exception e) {
            LOGGER.error("添加服务转发失败", e);
            NotificationUtil.showError("转发失败", 
                String.format("服务 %s 转发失败: %s", serviceName, e.getMessage()));
        }
    }

    private void initializeServiceSearch() {
        serviceSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                serviceTable.setItems(serviceNames);
            } else {
                String searchText = newValue.toLowerCase();
                ObservableList<String> filteredList = serviceNames.filtered(
                    service -> service.toLowerCase().contains(searchText)
                );
                serviceTable.setItems(filteredList);
            }
        });
    }

    private void startReconnectTimer() {
        reconnectTimer = new java.util.Timer(true);
        reconnectTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (autoReconnect && !isConnected) {
                    javafx.application.Platform.runLater(() -> {
                        try {
                            RetryUtil.retry(
                                () -> {
                                    handleConnect();
                                    return null;
                                },
                                e -> true,
                                MAX_RETRY_ATTEMPTS,
                                RETRY_DELAY_MS
                            );
                        } catch (Exception e) {
                            LOGGER.error("自动重连失败", e);
                            NotificationUtil.showError("重连失败", 
                                String.format("尝试重连%d次均失败，请检查网络连接", MAX_RETRY_ATTEMPTS));
                            autoReconnect = false;
                        }
                    });
                }
            }
        }, 5000, 5000);
    }

    public void stop() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
        }
        sshService.disconnect();
        nacosService.shutdown();
        httpProxyService.shutdown();
    }

    @FXML
    private void handleExportSshConfig() {
        try {
            IpForwardConfig config = new IpForwardConfig();
            config.setHost(hostProperty.get());
            config.setPort(Integer.parseInt(portProperty.get()));
            config.setUsername(usernameProperty.get());
            config.setPassword(passwordProperty.get());
            config.setForwardEntries(new ArrayList<>(forwardEntries));
            
            FileUtil.exportToJson(config, "导出SSH转发配置", 
                sshFormContainer.getScene().getWindow());
        } catch (Exception e) {
            LOGGER.error("导出配置失败", e);
            NotificationUtil.showError("导出失败", e.getMessage());
        }
    }

    @FXML
    private void handleImportSshConfig() {
        try {
            IpForwardConfig config = FileUtil.importFromJson(
                IpForwardConfig.class,
                "导入SSH转发配置",
                sshFormContainer.getScene().getWindow()
            );
            
            if (config != null) {
                hostProperty.set(config.getHost());
                portProperty.set(String.valueOf(config.getPort()));
                usernameProperty.set(config.getUsername());
                passwordProperty.set(config.getPassword());
                forwardEntries.setAll(config.getForwardEntries());
                saveHistory();
            }
        } catch (Exception e) {
            LOGGER.error("导入配置失败", e);
            NotificationUtil.showError("导入失败", e.getMessage());
        }
    }

    @FXML
    private void handleExportNacosConfig() {
        try {
            NacosConfig config = new NacosConfig();
            config.setServerAddr(serverAddrProperty.get());
            config.setNamespace(namespaceProperty.get());
            config.setGroupName(groupNameProperty.get());
            config.setUsername(nacosUsernameProperty.get());
            config.setPassword(nacosPasswordProperty.get());
            config.setServiceNames(new ArrayList<>(serviceNames));
            
            FileUtil.exportToJson(config, "导出Nacos转发配置", 
                nacosFormContainer.getScene().getWindow());
        } catch (Exception e) {
            LOGGER.error("导出配置失败", e);
            NotificationUtil.showError("导出失败", e.getMessage());
        }
    }

    @FXML
    private void handleImportNacosConfig() {
        try {
            NacosConfig config = FileUtil.importFromJson(
                NacosConfig.class,
                "导入Nacos转发配置",
                nacosFormContainer.getScene().getWindow()
            );
            
            if (config != null) {
                serverAddrProperty.set(config.getServerAddr());
                namespaceProperty.set(config.getNamespace());
                groupNameProperty.set(config.getGroupName());
                nacosUsernameProperty.set(config.getUsername());
                nacosPasswordProperty.set(config.getPassword());
                serviceNames.setAll(config.getServiceNames());
                saveNacosHistory();
            }
        } catch (Exception e) {
            LOGGER.error("导入配置失败", e);
            NotificationUtil.showError("导入失败", e.getMessage());
        }
    }

    @FXML
    private void handleStopAll() {
        // 停止所有服务
        stop();
        
        // 重置状态
        isConnected = false;
        isNacosConnected = false;
        isNacosForwarding = false;
        
        // 更新UI状态
        updateButtonStatus();
        updateStatusLabel();
        updateNacosButtonStatus();
        updateNacosStatusLabel();
        
        NotificationUtil.showSuccess("停止成功", "所有服务已停止");
    }
}
