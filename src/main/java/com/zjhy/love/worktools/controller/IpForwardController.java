package com.zjhy.love.worktools.controller;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.zjhy.love.worktools.common.util.FileUtil;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.common.util.RetryUtil;
import com.zjhy.love.worktools.model.ForwardEntry;
import com.zjhy.love.worktools.model.IpForwardConfig;
import com.zjhy.love.worktools.model.NacosConfig;
import com.zjhy.love.worktools.service.HttpProxyService;
import com.zjhy.love.worktools.service.NacosService;
import com.zjhy.love.worktools.service.SshService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * IP转发控制器
 * 负责处理SSH端口转发和Nacos服务转发的UI交互逻辑
 */
public class IpForwardController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpForwardController.class);

    /**
     * SSH配置表单容器
     * 用于放置SSH连接配置的表单
     */
    @FXML
    private VBox sshFormContainer;

    /**
     * 转发规则表格
     * 显示和编辑端口转发规则
     */
    @FXML
    private TableView<ForwardEntry> forwardTable;

    /**
     * SSH配置表单
     * 包含SSH连接的所有配置字段
     */
    private Form sshForm;

    /**
     * SSH服务器地址属性
     * 用于绑定表单输入
     */
    private final StringProperty hostProperty = new SimpleStringProperty("");

    /**
     * SSH服务器端口属性
     * 默认值为22
     */
    private final StringProperty portProperty = new SimpleStringProperty("22");

    /**
     * SSH用户名属性
     * 用于绑定表单输入
     */
    private final StringProperty usernameProperty = new SimpleStringProperty("");

    /**
     * SSH密码属性
     * 用于绑定表单输入
     */
    private final StringProperty passwordProperty = new SimpleStringProperty("");

    /**
     * SSH服务实例
     * 处理SSH连接和端口转发
     */
    private final SshService sshService = new SshService();

    /**
     * 转发规则列表
     * 存储所有的端口转发配置
     */
    private final ObservableList<ForwardEntry> forwardEntries = FXCollections.observableArrayList();

    /**
     * SSH连接状态
     * true表示已连接，false表示未连接
     */
    private boolean isConnected = false;

    /**
     * 连接状态标签
     * 显示当前SSH连接状态
     */
    @FXML
    private Label statusLabel;

    /**
     * 连接按钮
     * 用于建立SSH连接
     */
    @FXML
    private Button connectButton;

    /**
     * 断开连接按钮
     * 用于断开SSH连接
     */
    @FXML
    private Button disconnectButton;

    /**
     * 开始转发按钮
     * 用于启动端口转发
     */
    @FXML
    private Button startForwardButton;

    /**
     * 标签页面板
     * 包含SSH转发和Nacos转发两个标签页
     */
    @FXML
    private TabPane tabPane;

    /**
     * Nacos配置表单容器
     * 用于放置Nacos连接配置的表单
     */
    @FXML
    private VBox nacosFormContainer;

    /**
     * 服务列表表格
     * 显示Nacos中的服务列表
     */
    @FXML
    private TableView<String> serviceTable;

    /**
     * Nacos服务实例
     * 处理与Nacos服务器的交互
     */
    private final NacosService nacosService = new NacosService();

    /**
     * HTTP代理服务实例
     * 处理HTTP请求的转发
     */
    private final HttpProxyService httpProxyService = new HttpProxyService();

    /**
     * Nacos配置表单
     * 包含Nacos连接的所有配置字段
     */
    private Form nacosForm;

    /**
     * Nacos服务器地址属性
     * 用于绑定表单输入
     */
    private final StringProperty serverAddrProperty = new SimpleStringProperty("");

    /**
     * Nacos命名空间属性
     * 用于绑定表单输入
     */
    private final StringProperty namespaceProperty = new SimpleStringProperty("");

    /**
     * Nacos用户名属性
     * 用于绑定表单输入
     */
    private final StringProperty nacosUsernameProperty = new SimpleStringProperty("");

    /**
     * Nacos密码属性
     * 用于绑定表单输入
     */
    private final StringProperty nacosPasswordProperty = new SimpleStringProperty("");

    /**
     * 服务名称列表
     * 存储要转发的Nacos服务
     */
    private final ObservableList<String> serviceNames = FXCollections.observableArrayList();

    /**
     * Nacos连接状态标签
     * 显示当前Nacos连接状态
     */
    @FXML
    private Label nacosStatusLabel;

    /**
     * Nacos连接按钮
     * 用于连接Nacos服务器
     */
    @FXML
    private Button nacosConnectButton;

    /**
     * Nacos断开连接按钮
     * 用于断开Nacos连接
     */
    @FXML
    private Button nacosDisconnectButton;

    /**
     * 开始Nacos转发按钮
     * 用于启动Nacos服务转发
     */
    @FXML
    private Button startNacosForwardButton;

    /**
     * Nacos连接状态
     * true表示已连接，false表示未连接
     */
    private boolean isNacosConnected = false;

    /**
     * Nacos转发状态
     * true表示正在转发，false表示未转发
     */
    private boolean isNacosForwarding = false;

    /**
     * 最大重试次数
     * 自动重连时的最大尝试次数
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * 重试延迟时间
     * 两次重试之间的等待时间（毫秒）
     */
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 服务搜索输入框
     * 用于过滤服务列表
     */
    @FXML
    private TextField serviceSearchField;

    /**
     * 重连定时器
     * 用于自动重连功能
     */
    private java.util.Timer reconnectTimer;

    /**
     * 自动重连标志
     * true表示启用自动重连，false表示禁用
     */
    private boolean autoReconnect = true;

    /**
     * Nacos分组名称属性
     * 用于指定服务所属的分组
     */
    private final StringProperty groupNameProperty = new SimpleStringProperty("DEFAULT_GROUP");

    /**
     * 初始化控制器
     * 设置表单、表格和事件监听器
     */
    @FXML
    public void initialize() {
        // 初始化SSH配置表单
        initializeSshForm();

        // 加载历史配置
        loadHistory();

        // 初始化转发规则表格
        initializeForwardTable();

        // 初始化Nacos配置表单
        initializeNacosForm();

        // 初始化服务搜索功能
        initializeServiceSearch();

        // 加载Nacos历史配置
        loadNacosHistory();

        //初始化nacos服务列表
        initializeServiceTable();

        // 启动自动重连定时器
        //startReconnectTimer();
    }

    /**
     * 初始化SSH配置表单
     * 创建并配置SSH连接所需的输入字段
     */
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

    /**
     * 初始化转发规则表格
     * 设置表格列和编辑功能
     */
    private void initializeForwardTable() {
        // 启用表格编辑
        forwardTable.setEditable(true);

        // 名称列
        TableColumn<ForwardEntry, String> nameCol = new TableColumn<>("名称");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setEditable(true);  // 确保列可编辑
        nameCol.setOnEditCommit(event -> {
            TablePosition<ForwardEntry, String> pos = event.getTablePosition();
            String newValue = event.getNewValue();
            int row = pos.getRow();
            ForwardEntry entry = event.getTableView().getItems().get(row);
            entry.setName(newValue);
            saveHistory();
        });

        // 本地监听地址列
        TableColumn<ForwardEntry, String> localHostCol = new TableColumn<>("本地监听地址");
        localHostCol.setCellValueFactory(cellData -> cellData.getValue().localHostProperty());
        localHostCol.setCellFactory(TextFieldTableCell.forTableColumn());
        localHostCol.setEditable(true);  // 确保列可编辑
        localHostCol.setOnEditCommit(event -> {
            TablePosition<ForwardEntry, String> pos = event.getTablePosition();
            String newValue = event.getNewValue();
            if (newValue == null || newValue.trim().isEmpty()) {
                newValue = "127.0.0.1";
            }
            int row = pos.getRow();
            ForwardEntry entry = event.getTableView().getItems().get(row);
            entry.setLocalHost(newValue);
            saveHistory();
        });

        // 本地监听端口列
        TableColumn<ForwardEntry, String> localPortCol = new TableColumn<>("本地监听端口");
        localPortCol.setCellValueFactory(cellData -> {
            ForwardEntry entry = cellData.getValue();
            return new SimpleStringProperty(String.valueOf(entry.getLocalPort()));
        });
        localPortCol.setCellFactory(TextFieldTableCell.forTableColumn());
        localPortCol.setEditable(true);  // 确保列可编辑
        localPortCol.setOnEditCommit(event -> {
            try {
                TablePosition<ForwardEntry, String> pos = event.getTablePosition();
                String newValue = event.getNewValue();
                int row = pos.getRow();
                ForwardEntry entry = event.getTableView().getItems().get(row);
                int port = Integer.parseInt(newValue);
                if (port > 0 && port < 65536) {
                    entry.setLocalPort(port);
                    saveHistory();
                } else {
                    NotificationUtil.showError("输入错误", "端口范围必须在1-65535之间");
                }
            } catch (NumberFormatException e) {
                NotificationUtil.showError("输入错误", "请输入有效的端口号");
            }
        });

        // 远程目标地址列
        TableColumn<ForwardEntry, String> remoteHostCol = new TableColumn<>("远程目标地址");
        remoteHostCol.setCellValueFactory(cellData -> cellData.getValue().remoteHostProperty());
        remoteHostCol.setCellFactory(TextFieldTableCell.forTableColumn());
        remoteHostCol.setEditable(true);  // 确保列可编辑
        remoteHostCol.setOnEditCommit(event -> {
            TablePosition<ForwardEntry, String> pos = event.getTablePosition();
            String newValue = event.getNewValue();
            int row = pos.getRow();
            ForwardEntry entry = event.getTableView().getItems().get(row);
            if (newValue != null && !newValue.trim().isEmpty()) {
                entry.setRemoteHost(newValue);
                saveHistory();
            } else {
                NotificationUtil.showError("输入错误", "远程地址不能为空");
            }
        });

        // 远程目标端口列
        TableColumn<ForwardEntry, String> remotePortCol = new TableColumn<>("远程目标端口");
        remotePortCol.setCellValueFactory(cellData -> {
            ForwardEntry entry = cellData.getValue();
            return new SimpleStringProperty(String.valueOf(entry.getRemotePort()));
        });
        remotePortCol.setCellFactory(TextFieldTableCell.forTableColumn());
        remotePortCol.setEditable(true);  // 确保列可编辑
        remotePortCol.setOnEditCommit(event -> {
            try {
                TablePosition<ForwardEntry, String> pos = event.getTablePosition();
                String newValue = event.getNewValue();
                int row = pos.getRow();
                ForwardEntry entry = event.getTableView().getItems().get(row);
                int port = Integer.parseInt(newValue);
                if (port > 0 && port < 65536) {
                    entry.setRemotePort(port);
                    saveHistory();
                } else {
                    NotificationUtil.showError("输入错误", "端口范围必须在1-65535之间");
                }
            } catch (NumberFormatException e) {
                NotificationUtil.showError("输入错误", "请输入有效的端口号");
            }
        });

        // 删除列
        TableColumn<ForwardEntry, Void> deleteCol = new TableColumn<>("操作");
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("删除");

            {
                deleteButton.setOnAction(event -> {
                    ForwardEntry entry = getTableView().getItems().get(getIndex());
                    forwardEntries.remove(entry);
                    saveHistory();
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

        // 设置列宽
        nameCol.setPrefWidth(100);
        localHostCol.setPrefWidth(120);
        localPortCol.setPrefWidth(100);
        remoteHostCol.setPrefWidth(120);
        remotePortCol.setPrefWidth(100);
        deleteCol.setPrefWidth(60);

        forwardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // 添加所有列到表格
        forwardTable.getColumns().addAll(nameCol, localHostCol, localPortCol, remoteHostCol, remotePortCol, deleteCol);

        // 绑定数据源
        forwardTable.setItems(forwardEntries);
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

    /**
     * 处理SSH连接
     * 使用表单中的配置信息建立SSH连接
     */
    @FXML
    private void handleConnect() {
        if (sshForm.isValid()) {
            sshForm.persist();
            try {
                // 获取表单配置
                String host = hostProperty.get();
                int port = Integer.parseInt(portProperty.get());
                String username = usernameProperty.get();
                String password = passwordProperty.get();

                // 建立SSH连接
                sshService.connect(host, port, username, password);

                // 更新连接状态
                isConnected = true;
                updateButtonStatus();
                updateStatusLabel();

                // 保存配置到历史记录
                saveHistory();

                // 显示成功提示
                NotificationUtil.showSuccess("连接成功", "SSH连接已建立");
            } catch (Exception e) {
                LOGGER.error("SSH连接失败", e);
                NotificationUtil.showError("连接失败", e.getMessage());
            }
        }
    }

    /**
     * 处理SSH断开连接
     * 关闭SSH连接并更新UI状态
     */
    @FXML
    private void handleDisconnect() {
        sshService.disconnect();
        isConnected = false;
        updateButtonStatus();
        updateStatusLabel();
        NotificationUtil.showSuccess("断开连接", "SSH连接已断开");
    }

    /**
     * 添加端口转发规则
     * 打开对话框让用户输入转发规则
     */
    @FXML
    private void handleAddForward() {
        ForwardEntry entry = new ForwardEntry();
        entry.setName("新转发规则");  // 设默认名称
        entry.setLocalHost("127.0.0.1");
        entry.setLocalPort(0);
        entry.setRemotePort(0);
        forwardEntries.add(entry);
        saveHistory();
    }

    /**
     * 开始端口转发
     * 根���表格中的规则配置SSH端口转发
     */
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

        NotificationUtil.showSuccess("转发成功", "所有端口转发规则启动");
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

    /**
     * 初始化Nacos配置表单
     * 创建并配置Nacos连接所需的输入字段
     */
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

    /**
     * 初始化服务列表表格
     */
    private void initializeServiceTable() {
        // 创建服务名称列
        TableColumn<String, String> serviceNameCol = new TableColumn<>("服务名称");
        serviceNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        serviceNameCol.setPrefWidth(200);

        // 创建本地映射地址列
        TableColumn<String, String> localMappingAddrCol = new TableColumn<>("本地映射地址");
        localMappingAddrCol.setCellValueFactory(cellData -> {
            String serviceName = cellData.getValue();
            String target = null;
            if (isNacosForwarding) {
                List<Instance> instances = null;
                try {
                    instances = nacosService.getServiceInstances(serviceName, groupNameProperty.get());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    instances = Collections.emptyList();
                }
                if (!instances.isEmpty()) {
                    Instance instance = instances.get(0);
                    String remote = instance.getIp() + ":" + instance.getPort();
                    // 从 httpProxyService 获取本地映射地址
                    target = httpProxyService.getServiceMapping(remote);
                }
            }
            return new SimpleStringProperty(Objects.requireNonNullElse(target, ""));
        });

        // 创建删除列
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
        deleteCol.setPrefWidth(60);

        // 设置列
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        serviceTable.getColumns().addAll(serviceNameCol, localMappingAddrCol, deleteCol);

        // 绑定数据源
        serviceTable.setItems(serviceNames);
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

    /**
     * 保存Nacos配置历史
     */
    private void saveNacosHistory() {
        NacosConfig config = new NacosConfig();
        config.setServerAddr(serverAddrProperty.get());
        config.setNamespace(namespaceProperty.get());
        config.setUsername(nacosUsernameProperty.get());
        config.setPassword(nacosPasswordProperty.get());
        config.setGroupName(groupNameProperty.get());
        config.setServiceNames(new ArrayList<>(serviceNames));
        HistoryUtil.saveHistory("nacos", config);
    }

    /**
     * 处理Nacos连接
     * 使用表单中的配置信息连接Nacos服务器
     */
    @FXML
    private void handleNacosConnect() {
        if ((!isNacosConnected) && nacosForm.isValid()) {
            nacosForm.persist();
            try {
                // 创建Nacos配置对象
                NacosConfig config = new NacosConfig();
                config.setServerAddr(serverAddrProperty.get());
                config.setNamespace(namespaceProperty.get());
                config.setUsername(nacosUsernameProperty.get());
                config.setPassword(nacosPasswordProperty.get());

                // 连接Nacos服务器
                nacosService.connect(config);

                // 更新连接状态
                isNacosConnected = true;
                updateNacosButtonStatus();
                updateNacosStatusLabel();

                // 保存配置到历史记录
                saveNacosHistory();

                // 显示成功提示
                NotificationUtil.showSuccess("连接成功", "Nacos连接已建立");
            } catch (Exception e) {
                LOGGER.error("Nacos连接失败", e);
                NotificationUtil.showError("连接失败", e.getMessage());
            }
        }
    }

    /**
     * 处理Nacos断开连接
     * 关闭Nacos连接并更新UI状态
     */
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

    /**
     * 开始Nacos服务转发
     * 为选中的服务配置端口转发和HTTP代理
     */
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
                    // 查找可用的本地端口（从10000到65535之间）
                    int localPort = findAvailablePort(10000, 65535);
                    if (localPort == -1) {
                        throw new RuntimeException("无法找到可用的本地端口");
                    }
                    LOGGER.debug("启动服务转发 - 服务: {}, 本地端口: {}, 目标: {}", serviceName, localPort, target);
                    // 添加服务映射
                    httpProxyService.addServiceMapping(target, "127.0.0.1:" + localPort);
                    // 创建SSH端口转发
                    sshService.addPortForwarding("127.0.0.1", localPort, instance.getIp(), instance.getPort());
                }
            }
            isNacosForwarding = true;
            updateNacosButtonStatus();
            updateNacosStatusLabel();
            //刷新服务列表，展示映射端口
            serviceTable.refresh();
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

    /**
     * 添加Nacos服务
     */
    @FXML
    private void handleAddService() {
        if (!isNacosConnected) {
            NotificationUtil.showError("错误", "请先连接Nacos");
            return;
        }

        try {
            // 获取可用服务列表
            List<String> availableServices = nacosService.getServiceList(groupNameProperty.get());

            // 创建自定义对话框
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("添加服务");
            dialog.setHeaderText("请选择要添加的服务");

            // 添加确定和取消按钮
            ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

            // 创建对话框内容
            VBox content = new VBox(10);
            content.setPadding(new Insets(10));

            // 添加搜索框
            TextField searchField = new TextField();
            searchField.setPromptText("搜索服务...");

            // 创建服务列表视图
            ListView<String> serviceListView = new ListView<>();
            serviceListView.getItems().addAll(availableServices);
            serviceListView.setPrefHeight(300);

            // 配置搜索功能
            FilteredList<String> filteredServices = new FilteredList<>(
                    FXCollections.observableArrayList(availableServices)
            );
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredServices.setPredicate(service ->
                        newValue == null || newValue.trim().isEmpty() ||
                                service.toLowerCase().contains(newValue.toLowerCase())
                );
            });
            serviceListView.setItems(filteredServices);

            // 设置对话框内容
            content.getChildren().addAll(searchField, serviceListView);
            dialog.getDialogPane().setContent(content);

            // 配置结果转换器
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    return serviceListView.getSelectionModel().getSelectedItem();
                }
                return null;
            });

            // 禁用确定按钮，直到选择了服务
            Node confirmButton = dialog.getDialogPane().lookupButton(confirmButtonType);
            confirmButton.setDisable(true);

            // 当选择服务时启用确定按钮
            serviceListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> confirmButton.setDisable(newValue == null));

            // 显示对话框���处理结果
            dialog.showAndWait().ifPresent(serviceName -> {
                if (!serviceNames.contains(serviceName)) {
                    LOGGER.debug("添加服务: {}", serviceName);
                    // 添加到列表
                    serviceNames.add(serviceName);
                    // 保存历史记录
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

    /**
     * 获取可用的随机端口
     * 在指定范围内查找未被占用的端口
     *
     * @param startPort 起始端口号
     * @param endPort   结束端口号
     * @return 可用的端口号，如果没有可用端口则返回-1
     */
    private int findAvailablePort(int startPort, int endPort) {
        for (int port = startPort; port <= endPort; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                socket.close();
                return port;
            } catch (IOException ignored) {
                // 端口被占用，继续尝试下一个端口
            }
        }
        return -1;
    }

    /**
     * 添加服务转发
     * 为指定服务配置端口转发和HTTP代理
     *
     * @param serviceName 服务名称
     */
    private void addServiceForward(String serviceName) {
        try {
            // 获取服务实例
            List<Instance> instances = nacosService.getServiceInstances(serviceName, groupNameProperty.get());
            if (!instances.isEmpty()) {
                // 获取第一个实例
                Instance instance = instances.get(0);
                int remotePort = instance.getPort();

                // 查找可用的本地端口（从10000到65535之间）
                int localPort = findAvailablePort(10000, 65535);
                if (localPort == -1) {
                    throw new RuntimeException("无法找到可用的本地端口");
                }

                String target = instance.getIp() + ":" + remotePort;
                LOGGER.debug("添加服务转发 - 服务: {}, 本地端口: {}, 目标: {}", serviceName, localPort, target);

                // 添加HTTP代理映射
                httpProxyService.addServiceMapping(target, "127.0.0.1:" + localPort);

                // 创建SSH端口转发
                sshService.addPortForwarding(
                        "127.0.0.1",
                        localPort,      // 使用找到的可用端口
                        instance.getIp(),
                        remotePort
                );

                // 显示成功提示
                NotificationUtil.showSuccess("转发成功",
                        String.format("服务 %s 已添加到转发列表\n本地端口: %d -> 远程端口: %d",
                                serviceName, localPort, remotePort));
            } else {
                throw new RuntimeException("服务没有可用的实例");
            }
        } catch (Exception e) {
            LOGGER.error("添加服务转发失败", e);
            NotificationUtil.showError("转发失败",
                    String.format("服务 %s 转发失败: %s", serviceName, e.getMessage()));
        }
    }

    /**
     * 初始化服务搜索功能
     * 配置搜索框的过滤功能
     */
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

    /**
     * 启动自动重连定时器
     * 在连接断开时自动尝试重新连接
     */
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

    /**
     * 停止所有服务
     * 关闭所有连接和转发
     */
    public void stop() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
        }
        sshService.disconnect();
        nacosService.shutdown();
        httpProxyService.shutdown();
    }

    /**
     * 导出SSH配置
     * 将当前SSH配置保存到JSON文件
     */
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

    /**
     * 导入SSH配置
     * 从JSON文件加载SSH配置
     */
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

    /**
     * 导出Nacos配置
     * 将当前Nacos配置保存到JSON文件
     */
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

    /**
     * 导入Nacos配置
     * 从JSON文件加载Nacos配置
     */
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

    /**
     * 停止所有服务
     * 关闭所有连接和转发，并重置UI状态
     */
    @FXML
    private void handleStopAll() {
        // 停止所有服务
        stop();

        // 重置状态标志
        isConnected = false;
        isNacosConnected = false;
        isNacosForwarding = false;

        // 更新UI状态
        updateButtonStatus();
        updateStatusLabel();
        updateNacosButtonStatus();
        updateNacosStatusLabel();

        // 显示成功提示
        NotificationUtil.showSuccess("停止成功", "所有服务已停止");
    }
}
