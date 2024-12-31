package com.zjhy.love.worktools.view;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zjhy.love.worktools.common.util.FileUtil;
import com.zjhy.love.worktools.common.util.HistoryUtil;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.ForwardEntry;
import com.zjhy.love.worktools.model.IpForwardConfig;
import com.zjhy.love.worktools.model.NacosConfig;
import com.zjhy.love.worktools.model.NacosServiceItem;
import com.zjhy.love.worktools.service.HttpProxyService;
import com.zjhy.love.worktools.service.NacosService;
import com.zjhy.love.worktools.service.SshService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.stream.Collectors;

/**
 * IP转发视图
 * 包含 SSH 端口转发和 Nacos 服务转发功能
 */
public class IpForwardView extends BaseView {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpForwardView.class);

    // =============== UI 组件 ===============

    // SSH 相关组件
    private final TextField hostField = new TextField();
    private final TextField portField = new TextField("22");
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button connectButton = new Button("连接");

    // Nacos 相关组件
    private final TextField serverAddrField = new TextField();
    private final TextField namespaceField = new TextField();
    private final TextField nacosUserField = new TextField();
    private final PasswordField nacosPasswordField = new PasswordField();
    private final TextField groupField = new TextField("DEFAULT_GROUP");
    private final Button nacosConnectButton = new Button("连接");

    // HTTP 代理相关组件
    private final TextField proxyPortField = new TextField("80");
    private final Button startProxyButton = new Button("启动代理");

    // 数据表格和列表
    private final TableView<ForwardEntry> forwardTable = new TableView<>();
    private final ObservableList<NacosServiceItem> nacosServices = FXCollections.observableArrayList();

    // =============== 服务实例 ===============

    private final SshService sshService = new SshService();
    private final NacosService nacosService = new NacosService();
    private final HttpProxyService httpProxyService = new HttpProxyService();

    // =============== 数据集合 ===============

    private final ObservableList<ForwardEntry> forwardEntries = FXCollections.observableArrayList();
    // =============== UI 构建方法 ===============

    /**
     * 构造函数
     * 初始化界面布局和组件
     */
    public IpForwardView() {
        // 创建选项卡面板
        TabPane tabPane = new TabPane();
        configureTabPane(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // 使用基类方法添加内容
        addContent(tabPane);

        // 加载历史配置
        loadHistory();
    }

    /**
     * 创建 SSH 转发选项卡
     */
    private Tab createSshTab() {
        Tab sshTab = new Tab("SSH端口转发");
        VBox sshContent = new VBox(15);
        sshContent.setPadding(new Insets(20));

        // SSH配置和转发规则
        VBox sshConfig = createSshConfigSection();
        HBox toolbar = createToolbar();
        configureForwardTable();

        sshContent.getChildren().addAll(sshConfig, toolbar, forwardTable);
        sshTab.setContent(sshContent);

        return sshTab;
    }

    /**
     * 创建 Nacos 服务选项卡
     */
    private Tab createNacosTab() {
        Tab nacosTab = new Tab("Nacos服务转发");
        VBox nacosContent = new VBox(15);
        nacosContent.setPadding(new Insets(20));

        // Nacos配置和服务列表
        VBox nacosConfig = createNacosConfigSection();
        nacosContent.getChildren().add(nacosConfig);
        nacosTab.setContent(nacosContent);

        return nacosTab;
    }

    /**
     * 配置转发规则表格
     */
    private void configureForwardTable() {
        // 创建表格列
        TableColumn<ForwardEntry, String> nameColumn = new TableColumn<>("名称");
        TableColumn<ForwardEntry, String> localHostColumn = new TableColumn<>("本地主机");
        TableColumn<ForwardEntry, Integer> localPortColumn = new TableColumn<>("本地端口");
        TableColumn<ForwardEntry, String> remoteHostColumn = new TableColumn<>("远程主机");
        TableColumn<ForwardEntry, Integer> remotePortColumn = new TableColumn<>("远程端口");
        TableColumn<ForwardEntry, Void> actionColumn = new TableColumn<>("操作");

        // 配置列数据
        configureTableColumns(nameColumn, localHostColumn, localPortColumn,
                remoteHostColumn, remotePortColumn, actionColumn);

        // 添加列到表格
        forwardTable.getColumns().addAll(
                nameColumn, localHostColumn, localPortColumn,
                remoteHostColumn, remotePortColumn, actionColumn
        );

        // 设置表格数据源
        forwardTable.setItems(forwardEntries);

        // 配置表格样式
        forwardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(forwardTable, Priority.ALWAYS);
    }

    /**
     * 加载历史配置
     */
    private void loadHistory() {
        try {
            IpForwardConfig config = HistoryUtil.getHistory("ip-forward", IpForwardConfig.class);
            if (config != null) {
                loadSshConfig(config);
                loadNacosConfig(config);
                loadForwardEntries(config);
            }
        } catch (Exception e) {
            LOGGER.error("加载历史配置失败", e);
            NotificationUtil.showError("加载失败", "加载历史配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存当前配置
     */
    private void saveHistory() {
        try {
            IpForwardConfig config = new IpForwardConfig();
            saveSshConfig(config);
            saveNacosConfig(config);
            saveForwardEntries(config);

            HistoryUtil.saveHistory("ip-forward", config);
        } catch (Exception e) {
            LOGGER.error("保存配置失败", e);
            NotificationUtil.showError("保存失败", "保存配置失败: " + e.getMessage());
        }
    }

    /**
     * 创建 SSH 配置区域
     */
    private VBox createSshConfigSection() {
        VBox container = new VBox(10);
        container.getStyleClass().addAll("form-section", "surface-card");

        // 创建表单网格
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        // 配置输入字段
        hostField.setPromptText("SSH服务器地址");
        portField.setPromptText("SSH端口");
        usernameField.setPromptText("用户名");
        passwordField.setPromptText("密码");

        connectButton.getStyleClass().addAll("button-outlined", "accent");
        connectButton.setGraphic(new Glyph("FontAwesome", "PLUG"));
        connectButton.setOnAction(e -> handleConnect());

        grid.addRow(0, new Label("服务器:"), hostField);
        grid.addRow(1, new Label("端口:"), portField);
        grid.addRow(2, new Label("用户名:"), usernameField);
        grid.addRow(3, new Label("密码:"), passwordField);

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(connectButton);
        grid.add(buttonBox, 0, 4, 2, 1);

        container.getChildren().add(grid);
        return container;
    }

    /**
     * 创建工具栏
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button addButton = new Button("添加规则", new Glyph("FontAwesome", "PLUS"));
        addButton.getStyleClass().addAll("button-outlined", "primary");
        addButton.setOnAction(e -> handleAddRule());

        Button importButton = new Button("导入", new Glyph("FontAwesome", "UPLOAD"));
        importButton.getStyleClass().addAll("button-outlined", "accent");
        importButton.setOnAction(e -> handleImportRules());

        Button exportButton = new Button("导出", new Glyph("FontAwesome", "DOWNLOAD"));
        exportButton.getStyleClass().addAll("button-outlined", "accent");
        exportButton.setOnAction(e -> handleExportRules());

        toolbar.getChildren().addAll(addButton, new Separator(Orientation.VERTICAL),
                importButton, exportButton);

        return toolbar;
    }

    /**
     * 创建 Nacos 配置区域
     */
    private VBox createNacosConfigSection() {
        VBox container = new VBox(15);
        container.getStyleClass().addAll("form-section", "surface-card");

        // 创建表单网格
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        // 配置输入字段
        serverAddrField.setPromptText("Nacos服务器地址");
        namespaceField.setPromptText("命名空间");
        nacosUserField.setPromptText("用户名");
        nacosPasswordField.setPromptText("密码");
        groupField.setPromptText("分组");

        nacosConnectButton.getStyleClass().addAll("button-outlined", "accent");
        nacosConnectButton.setGraphic(new Glyph("FontAwesome", "PLUG"));
        nacosConnectButton.setOnAction(e -> handleNacosConnect());

        // 添加到网格
        grid.addRow(0, new Label("服务器:"), serverAddrField);
        grid.addRow(1, new Label("命名空间:"), namespaceField);
        grid.addRow(2, new Label("用户名:"), nacosUserField);
        grid.addRow(3, new Label("密码:"), nacosPasswordField);
        grid.addRow(4, new Label("分组:"), groupField);
        grid.add(nacosConnectButton, 0, 5, 2, 1);

        // 创建服务列表视图
        NacosServiceListView serviceListView = new NacosServiceListView();
        VBox.setVgrow(serviceListView, Priority.ALWAYS);

        // 创建 HTTP 代理配置
        VBox proxyConfig = createHttpProxySection();

        // 添加组件
        container.getChildren().addAll(grid, serviceListView, proxyConfig);
        return container;
    }

    /**
     * 创建 HTTP 代理配置区域
     */
    private VBox createHttpProxySection() {
        VBox container = new VBox(10);
        container.getStyleClass().addAll("form-section", "surface-card");

        // 创建表单网格
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        // 配置输入字段
        proxyPortField.setPromptText("代理端口");
        startProxyButton.getStyleClass().addAll("button-outlined", "accent");
        startProxyButton.setGraphic(new Glyph("FontAwesome", "PLAY"));
        startProxyButton.setOnAction(e -> handleStartProxy());

        grid.addRow(0, new Label("代理端口:"), proxyPortField, startProxyButton);

        container.getChildren().add(grid);
        return container;
    }

    // =============== 事件处理方法 ===============

    /**
     * 处理 SSH 连接/断开
     */
    private void handleConnect() {
        if (sshService.isConnected()) {
            sshService.disconnect();
            connectButton.setText("连接");
            return;
        }

        try {
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());
            String username = usernameField.getText();
            String password = passwordField.getText();

            // 验证输入
            if (StrUtil.hasBlank(host, username, password)) {
                NotificationUtil.showWarning("输入错误", "请填写完整的连接信息");
                return;
            }

            // 连接 SSH
            sshService.connect(host, port, username, password);
            // 连接成功后自动转发已有规则
            autoForwardExistingRules();
            connectButton.setText("断开");
            NotificationUtil.showSuccess("连接成功", "SSH连接已建立");

        } catch (Exception e) {
            LOGGER.error("SSH连接失败", e);
            NotificationUtil.showError("连接失败", e.getMessage());
        }
    }

    /**
     * 自动转发已有规则
     */
    private void autoForwardExistingRules() {
        if (!sshService.isConnected() || forwardEntries.isEmpty()) {
            return;
        }

        // 创建规则列表副本，避免并发修改
        List<ForwardEntry> rulesToForward = new ArrayList<>(forwardEntries);

        // 清空当前规则列表
        forwardEntries.clear();

        // 尝试重新添加每个规则
        for (ForwardEntry entry : rulesToForward) {
            try {
                if (!Objects.equals(entry.getType(), ForwardEntry.TYPE_MANUAL)) {
                    continue;
                }
                sshService.addPortForwarding(
                        entry.getLocalHost(),
                        entry.getLocalPort(),
                        entry.getRemoteHost(),
                        entry.getRemotePort()
                );
                // 转发成功，添加回列表
                forwardEntries.add(entry);
                LOGGER.info("自动转发规则成功: {}", entry.getName());
            } catch (Exception e) {
                LOGGER.error("自动转发规则失败: {}", entry.getName(), e);
                NotificationUtil.showWarning("转发失败",
                        String.format("规则 %s 转发失败: %s",
                                entry.getName(), e.getMessage()));
            }
        }

        // 保存更新后的配置
        saveHistory();
    }

    /**
     * 处理添加转发规则
     */
    private void handleAddRule() {
        if (!sshService.isConnected()) {
            NotificationUtil.showWarning("添加失败", "请先连接SSH服务器");
            return;
        }

        Dialog<ForwardEntry> dialog = new Dialog<>();
        dialog.setTitle("添加转发规则");
        dialog.setHeaderText("请输入转发规则信息");

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField localHostField = new TextField("127.0.0.1");
        TextField localPortField = new TextField();
        TextField remoteHostField = new TextField();
        TextField remotePortField = new TextField();

        grid.addRow(0, new Label("名称:"), nameField);
        grid.addRow(1, new Label("本地主机:"), localHostField);
        grid.addRow(2, new Label("本地端口:"), localPortField);
        grid.addRow(3, new Label("远程主机:"), remoteHostField);
        grid.addRow(4, new Label("远程端口:"), remotePortField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 设置结果转换器
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    ForwardEntry entry = new ForwardEntry();
                    entry.setName(nameField.getText());
                    entry.setLocalHost(localHostField.getText());
                    entry.setLocalPort(Integer.parseInt(localPortField.getText()));
                    entry.setRemoteHost(remoteHostField.getText());
                    entry.setRemotePort(Integer.parseInt(remotePortField.getText()));
                    entry.setType(ForwardEntry.TYPE_MANUAL);
                    return entry;
                } catch (NumberFormatException e) {
                    NotificationUtil.showError("输入错误", "端口必须是数字");
                    return null;
                }
            }
            return null;
        });

        Optional<ForwardEntry> result = dialog.showAndWait();
        result.ifPresent(entry -> {
            try {
                // 检查端口是否可用
                if (isPortInUse(entry.getLocalPort())) {
                    NotificationUtil.showError("添加失败", "本地端口已被占用");
                    return;
                }

                // 添加转发规则
                sshService.addPortForwarding(
                        entry.getLocalHost(),
                        entry.getLocalPort(),
                        entry.getRemoteHost(),
                        entry.getRemotePort()
                );
                forwardEntries.add(entry);
                saveHistory();
                NotificationUtil.showSuccess("添加成功", "转发规则已添加");

            } catch (Exception e) {
                LOGGER.error("添加转发规则失败", e);
                NotificationUtil.showError("添加失败", e.getMessage());
            }
        });
    }

    /**
     * 处理删除规则
     */
    private void handleDeleteRule(ForwardEntry entry) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确定要删除这条转发规则吗？");
        alert.setContentText("规则名称: " + entry.getName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (sshService.isConnected()) {
                    sshService.removePortForwarding(entry.getLocalHost(), entry.getLocalPort());
                }
                forwardEntries.remove(entry);
                saveHistory();
                NotificationUtil.showSuccess("删除成功", "转发规则已删除");
            } catch (Exception e) {
                LOGGER.error("删除转发规则失败", e);
                NotificationUtil.showError("删除失败", e.getMessage());
            }
        }
    }

    // =============== Nacos 事件处理方法 ===============

    /**
     * 处理 Nacos 连接/断开
     */
    private void handleNacosConnect() {
        try {
            if (nacosService.isConnected()) {
                nacosService.shutdown();
                nacosConnectButton.setText("连接");
                nacosServices.clear();
                return;
            }

            String serverAddr = serverAddrField.getText();
            String namespace = namespaceField.getText();
            String username = nacosUserField.getText();
            String password = nacosPasswordField.getText();
            String groupName = groupField.getText();

            // 验证输入
            if (StrUtil.hasBlank(serverAddr, namespace)) {
                NotificationUtil.showWarning("输入错误", "请填写服务器地址和命名空间");
                return;
            }

            // 创建配置
            NacosConfig config = new NacosConfig();
            config.setServerAddr(serverAddr);
            config.setNamespace(namespace);
            config.setUsername(username);
            config.setPassword(password);
            config.setGroupName(groupName);

            // 连接 Nacos
            nacosService.connect(config);

            // 获取并监听服务列表
            refreshNacosServices(groupName);
            startServiceListening(groupName);

            nacosConnectButton.setText("断开");
            saveHistory();
            NotificationUtil.showSuccess("连接成功", "Nacos服务已连接");

        } catch (Exception e) {
            LOGGER.error("Nacos连接失败", e);
            NotificationUtil.showError("连接失败", e.getMessage());
        }
    }

    /**
     * 处理添加 Nacos 服务转发
     */
    private void handleAddNacosService(NacosServiceItem serviceItem) {
        if (!sshService.isConnected()) {
            NotificationUtil.showWarning("添加失败", "请先连接SSH服务器");
            return;
        }

        try {
            List<Instance> serviceInstances = nacosService.getServiceInstances(serviceItem.getServiceName(), serviceItem.getGroupName());
            if (CollectionUtil.isNotEmpty(serviceInstances)) {
                serviceInstances.forEach(instance -> {
                    // 查找可用端口
                    int localPort = findAvailablePort();
                    if (localPort == -1) {
                        NotificationUtil.showError("添加失败", "无法找到可用的本地端口");
                        return;
                    }

                    // 创建转发规则
                    ForwardEntry entry = new ForwardEntry();
                    entry.setName(serviceItem.getServiceName());
                    entry.setLocalHost("127.0.0.1");
                    entry.setLocalPort(localPort);
                    entry.setRemoteHost(instance.getIp());
                    entry.setRemotePort(instance.getPort());
                    entry.setType(ForwardEntry.TYPE_NACOS);

                    // 添加到转发列表
                    try {
                        sshService.addPortForwarding(entry.getLocalHost(), entry.getLocalPort(), entry.getRemoteHost(), entry.getRemotePort());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    forwardEntries.add(entry);
                    serviceItem.setStatus("已转发");

                    // 添加代理映射
                    httpProxyService.registerService(instance.getIp() + ":" + instance.getPort(), serviceItem.getServiceName(), localPort);

                    saveHistory();
                    NotificationUtil.showSuccess("添加成功",
                            String.format("服务 %s 已添加转发，本地端口: %d",
                                    serviceItem.getServiceName(), localPort));
                });
            }
            // 订阅服务实例
            subscribeServiceInstances(serviceItem);
        } catch (Exception e) {
            LOGGER.error("添加服务转发失败", e);
            NotificationUtil.showError("添加失败", e.getMessage());
        }
    }

    /**
     * 处理移除 Nacos 服务转发
     */
    private void handleRemoveNacosService(NacosServiceItem serviceItem) {
        // 查找并删除转发规则
        List<ForwardEntry> ruleToRemoveList = forwardEntries.stream()
                .filter(rule -> rule.getName().equals(serviceItem.getServiceName()))
                .toList();
        if (CollectionUtil.isNotEmpty(ruleToRemoveList)) {
            ruleToRemoveList.forEach(ruleToRemove -> {
                try {
                    // 停止转发
                    if (sshService.isConnected()) {
                        sshService.removePortForwarding(
                                ruleToRemove.getLocalHost(),
                                ruleToRemove.getLocalPort()
                        );
                    }

                    // 删除规则
                    forwardEntries.remove(ruleToRemove);

                    // 取消代理映射
                    httpProxyService.removeServiceMapping(ruleToRemove.getRemoteHost() + ":" + ruleToRemove.getRemotePort());

                    // 取消服务实例订阅
                    nacosService.unsubscribeService(
                            serviceItem.getServiceName(),
                            serviceItem.getGroupName()
                    );

                    // 更新服务状态
                    serviceItem.setStatus("未转发");

                    saveHistory();
                    NotificationUtil.showSuccess("取消成功",
                            "服务 " + serviceItem.getServiceName() + " 的转发已取消");

                } catch (Exception e) {
                    LOGGER.error("取消服务转发失败", e);
                    NotificationUtil.showError("取消失败", e.getMessage());
                }
            });
        }
    }

    // =============== 服务管理方法 ===============

    /**
     * 启动服务监听
     */
    private void startServiceListening(String groupName) {
        try {
            nacosService.subscribeServices(groupName, services -> {
                Platform.runLater(() -> {
                    // 更新服务列表，保持已转发状态
                    Set<String> forwardedServices = nacosServices.stream()
                            .filter(item -> "已转发".equals(item.getStatus()))
                            .map(NacosServiceItem::getServiceName)
                            .collect(Collectors.toSet());

                    nacosServices.setAll(
                            services.stream()
                                    .map(name -> {
                                        NacosServiceItem item = new NacosServiceItem(name, groupName);
                                        item.setServiceName(name);
                                        item.setGroupName(groupName);
                                        item.setStatus(forwardedServices.contains(name) ? "已转发" : "未转发");
                                        return item;
                                    })
                                    .collect(Collectors.toList())
                    );
                });
            });
        } catch (Exception e) {
            LOGGER.error("启动服务监听失败", e);
            NotificationUtil.showError("监听失败", e.getMessage());
        }
    }

    /**
     * 订阅服务实例变化
     */
    private void subscribeServiceInstances(NacosServiceItem serviceItem) {
        try {
            nacosService.subscribeService(
                    serviceItem.getServiceName(),
                    serviceItem.getGroupName(),
                    instances -> {
                        Platform.runLater(() -> {
                            updateForwardRules(serviceItem, instances);
                        });
                    }
            );
        } catch (Exception e) {
            LOGGER.error("订阅服务实例失败", e);
            throw new RuntimeException("订阅服务实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 取消订阅服务
     */
    private void unsubscribeService(NacosServiceItem serviceItem) {
        try {
            nacosService.unsubscribeService(
                    serviceItem.getServiceName(),
                    serviceItem.getGroupName()
            );
        } catch (Exception e) {
            LOGGER.error("取消服务订阅失败", e);
        }
    }

    /**
     * 刷新 Nacos 服务列表
     */
    private void refreshNacosServices(String groupName) {
        try {
            List<String> services = nacosService.getServiceList(groupName);
            nacosServices.setAll(
                    services.stream()
                            .map(name -> {
                                NacosServiceItem item = new NacosServiceItem(name, groupName);
                                item.setServiceName(name);
                                item.setGroupName(groupName);
                                item.setStatus("未转发");
                                return item;
                            })
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            LOGGER.error("刷新服务列表失败", e);
            NotificationUtil.showError("刷新失败", e.getMessage());
        }
    }

    // =============== 工具方法 ===============

    /**
     * 查找可用端口
     * 从 10000 开始查找第一个可用的端口
     */
    private int findAvailablePort() {
        Set<Integer> portSet = forwardEntries.stream().map(ForwardEntry::getLocalPort).collect(Collectors.toSet());
        for (int port = 10000; port < 65535; port++) {
            if (portSet.contains(port)) {
                continue;
            }
            if (!isPortInUse(port)) {
                return port;
            }
        }
        return -1;
    }

    /**
     * 检查端口是否被使用
     */
    private boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 处理规则导入
     */
    private void handleImportRules() {
        try {
            List<ForwardEntry> entries = FileUtil.importFromJson(
                    new TypeReference<>() {
                    },
                    "导入转发规则",
                    getScene().getWindow()
            );

            if (entries != null && !entries.isEmpty()) {
                forwardEntries.addAll(entries);
                saveHistory();
                NotificationUtil.showSuccess("导入成功", "成功导入 " + entries.size() + " 条规则");
            }
        } catch (Exception e) {
            LOGGER.error("导入规则失败", e);
            NotificationUtil.showError("导入失败", e.getMessage());
        }
    }

    /**
     * 处理规则导出
     */
    private void handleExportRules() {
        if (forwardEntries.isEmpty()) {
            NotificationUtil.showWarning("导出失败", "没有可导出的规则");
            return;
        }

        try {
            FileUtil.exportToJson(forwardEntries, "导出SSH转发配置", getScene().getWindow());
        } catch (Exception e) {
            LOGGER.error("导出规则失败", e);
            NotificationUtil.showError("导出失败", e.getMessage());
        }
    }

    /**
     * 服务列表视图
     * 用于显示和管理 Nacos 服务列表
     */
    private class NacosServiceListView extends VBox {
        private final ListView<NacosServiceItem> serviceListView;
        private final FilteredList<NacosServiceItem> filteredServices;
        private final SortedList<NacosServiceItem> sortedServices;
        private String nameFilter = "";

        public NacosServiceListView() {
            setSpacing(10);

            // 初始化列表视图和过滤排序
            filteredServices = new FilteredList<>(nacosServices);
            sortedServices = new SortedList<>(filteredServices);
            serviceListView = new ListView<>(sortedServices);

            // 配置列表视图
            serviceListView.setCellFactory(lv -> new ServiceListCell());
            VBox.setVgrow(serviceListView, Priority.ALWAYS);

            // 添加组件
            getChildren().addAll(
                    createFilterBox(),
                    serviceListView
            );
        }

        private HBox createFilterBox() {
            HBox filterBox = new HBox(10);
            filterBox.setAlignment(Pos.CENTER_LEFT);

            TextField nameFilterField = new TextField();
            nameFilterField.setPromptText("输入服务名称过滤");
            nameFilterField.setPrefWidth(300); // 加宽搜索框
            HBox.setHgrow(nameFilterField, Priority.ALWAYS); // 允许搜索框自适应宽度

            nameFilterField.textProperty().addListener((obs, oldVal, newVal) -> {
                nameFilter = newVal;
                updateFilter();
            });

            filterBox.getChildren().addAll(
                    new Label("搜索:"), nameFilterField
            );

            return filterBox;
        }

        private void updateFilter() {
            filteredServices.setPredicate(item -> {
                if (item == null) return false;

                return nameFilter.isEmpty() ||
                        item.getServiceName().toLowerCase()
                                .contains(nameFilter.toLowerCase());
            });
        }
    }

    /**
     * 服务列表单元格
     * 自定义服务列表项的显示和交互
     */
    private class ServiceListCell extends ListCell<NacosServiceItem> {
        private final HBox container = new HBox(10);
        private final VBox infoBox = new VBox(5);
        private final Label nameLabel = new Label();
        private final Label statusLabel = new Label();
        private final Button addButton = new Button("转发", new Glyph("FontAwesome", "FORWARD"));
        private final Button removeButton = new Button("取消", new Glyph("FontAwesome", "TIMES"));

        public ServiceListCell() {
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(8));

            infoBox.setAlignment(Pos.CENTER_LEFT);
            nameLabel.getStyleClass().add("h6");
            statusLabel.getStyleClass().add("text-muted");

            // 添加按钮容器，右对齐
            HBox buttonBox = new HBox(5);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(buttonBox, Priority.ALWAYS);

            addButton.getStyleClass().addAll("button-outlined", "success");
            addButton.setOnAction(e -> {
                NacosServiceItem item = getItem();
                if (item != null) {
                    handleAddNacosService(item);
                }
            });

            removeButton.getStyleClass().addAll("button-outlined", "danger");
            removeButton.setOnAction(e -> {
                NacosServiceItem item = getItem();
                if (item != null) {
                    handleRemoveNacosService(item);
                }
            });

            buttonBox.getChildren().addAll(addButton, removeButton);
            infoBox.getChildren().addAll(nameLabel, statusLabel);
            container.getChildren().addAll(infoBox, buttonBox);

            // 修改按钮可见性绑定
            addButton.visibleProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> {
                                NacosServiceItem item = getItem();
                                boolean isConnected = sshService.isConnected();
                                boolean hasItem = item != null;
                                boolean notForwarded = hasItem && !"已转发".equals(item.getStatus());

                                LOGGER.debug("Button visibility check: connected={}, hasItem={}, status={}",
                                        isConnected, hasItem, item != null ? item.getStatus() : null);

                                return isConnected && hasItem && notForwarded;
                            },
                            sshService.connectedProperty(),
                            itemProperty(),
                            // 添加对单元格项的监听
                            this.itemProperty().isNotNull().get() ?
                                    getItem().statusProperty() : new SimpleStringProperty()
                    )
            );

            // 添加单元格项变化监听
            itemProperty().addListener((obs, oldItem, newItem) -> {
                if (oldItem != null) {
                    addButton.visibleProperty().unbind();
                }
                if (newItem != null) {
                    addButton.visibleProperty().bind(
                            Bindings.createBooleanBinding(
                                    () -> sshService.isConnected() &&
                                            !"已转发".equals(newItem.getStatus()),
                                    sshService.connectedProperty(),
                                    newItem.statusProperty()
                            )
                    );
                }
            });
        }

        @Override
        protected void updateItem(NacosServiceItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
            } else {
                nameLabel.setText(item.getServiceName());
                statusLabel.setText(item.getStatus());

                LOGGER.debug("Updating cell: service={}, status={}",
                        item.getServiceName(), item.getStatus());

                boolean isForwarded = "已转发".equals(item.getStatus());
                removeButton.setVisible(isForwarded);

                setGraphic(container);
            }
        }
    }

    /**
     * 释放资源
     * 关闭所有服务连接和订阅
     */
    public void dispose() {
        // 取消服务订阅
        unsubscribeAllServices();

        // 关闭服务连接
        closeAllServices();
    }

    /**
     * 取消所有服务订阅
     */
    private void unsubscribeAllServices() {
        nacosServices.stream()
                .filter(item -> "已转发".equals(item.getStatus()))
                .forEach(this::unsubscribeService);
    }

    /**
     * 关闭所有服务连接
     */
    private void closeAllServices() {
        if (sshService.isConnected()) {
            sshService.disconnect();
        }
        if (nacosService.isConnected()) {
            nacosService.shutdown();
        }
        if (httpProxyService.isRunning()) {
            httpProxyService.stop();
        }
    }

    // =============== 配置加载和保存方法 ===============

    /**
     * 加载 SSH 配置
     */
    private void loadSshConfig(IpForwardConfig config) {
        hostField.setText(config.getHost());
        portField.setText(String.valueOf(config.getPort()));
        usernameField.setText(config.getUsername());
        passwordField.setText(config.getPassword());
    }

    /**
     * 加载 Nacos 配置
     */
    private void loadNacosConfig(IpForwardConfig config) {
        NacosConfig nacosConfig = config.getNacosConfig();
        if (nacosConfig != null) {
            serverAddrField.setText(nacosConfig.getServerAddr());
            namespaceField.setText(nacosConfig.getNamespace());
            nacosUserField.setText(nacosConfig.getUsername());
            nacosPasswordField.setText(nacosConfig.getPassword());
            groupField.setText(nacosConfig.getGroupName());
        }
    }

    /**
     * 加载转发规则
     */
    private void loadForwardEntries(IpForwardConfig config) {
        List<ForwardEntry> entries = config.getForwardEntries();
        if (entries != null) {
            forwardEntries.setAll(entries);
        }
    }

    /**
     * 保存 SSH 配置
     */
    private void saveSshConfig(IpForwardConfig config) {
        config.setHost(hostField.getText());
        config.setPort(Integer.parseInt(portField.getText()));
        config.setUsername(usernameField.getText());
        config.setPassword(passwordField.getText());
    }

    /**
     * 保存 Nacos 配置
     */
    private void saveNacosConfig(IpForwardConfig config) {
        NacosConfig nacosConfig = new NacosConfig();
        nacosConfig.setServerAddr(serverAddrField.getText());
        nacosConfig.setNamespace(namespaceField.getText());
        nacosConfig.setUsername(nacosUserField.getText());
        nacosConfig.setPassword(nacosPasswordField.getText());
        nacosConfig.setGroupName(groupField.getText());
        config.setNacosConfig(nacosConfig);
    }

    /**
     * 保存转发规则
     */
    private void saveForwardEntries(IpForwardConfig config) {
        config.setForwardEntries(forwardEntries);
    }

    // =============== 表格列配置方法 ===============

    /**
     * 配置表格列
     */
    private void configureTableColumns(
            TableColumn<ForwardEntry, String> nameColumn,
            TableColumn<ForwardEntry, String> localHostColumn,
            TableColumn<ForwardEntry, Integer> localPortColumn,
            TableColumn<ForwardEntry, String> remoteHostColumn,
            TableColumn<ForwardEntry, Integer> remotePortColumn,
            TableColumn<ForwardEntry, Void> actionColumn) {

        // 名称列
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setPrefWidth(150);

        // 本地主机列
        localHostColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLocalHost()));
        localHostColumn.setPrefWidth(120);

        // 本地端口列
        localPortColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getLocalPort()).asObject());
        localPortColumn.setPrefWidth(100);

        // 远程主机列
        remoteHostColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRemoteHost()));
        remoteHostColumn.setPrefWidth(120);

        // 远程端口列
        remotePortColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getRemotePort()).asObject());
        remotePortColumn.setPrefWidth(100);

        // 操作列
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("删除");

            {
                deleteButton.getStyleClass().addAll("button-small", "danger");
                deleteButton.setOnAction(e -> {
                    ForwardEntry entry = getTableView().getItems().get(getIndex());
                    handleDeleteRule(entry);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
        actionColumn.setPrefWidth(80);
    }

    /**
     * 更新转发规则
     */
    private void updateForwardRules(NacosServiceItem serviceItem, List<Instance> instances) {
        // 查找该服务的转发规则
        List<ForwardEntry> ruleList = forwardEntries.stream()
                .filter(r -> r.getName().equals(serviceItem.getServiceName()))
                .toList();

        if (CollectionUtil.isEmpty(ruleList)) {
            // 如果没有找到规则，说明服务未转发
            return;
        }

        //获取健康的实例
        List<Instance> healthyInstances = instances.stream().filter(Instance::isHealthy).toList();

        //删除这个服务所有的转发信息，以最新更新为准
        forwardEntries.removeAll(ruleList);

        //基于连接已经建立
        if (sshService.isConnected()) {
            //删除原来转发信息
            ruleList.forEach(rule -> {
                try {
                    sshService.removePortForwarding(
                            rule.getLocalHost(),
                            rule.getLocalPort()
                    );
                } catch (Exception e) {
                    LOGGER.error("停止转发规则失败", e);
                }
            });
            //新增转发规则
            healthyInstances.forEach(instance -> {
                ForwardEntry rule = new ForwardEntry(serviceItem.getServiceName(), "127.0.0.1", findAvailablePort(), instance.getIp(), instance.getPort());
                forwardEntries.add(rule);
                try {
                    sshService.addPortForwarding(
                            rule.getLocalHost(),
                            rule.getLocalPort(),
                            rule.getRemoteHost(),
                            rule.getRemotePort()
                    );
                    NotificationUtil.showSuccess("服务实例已更新",
                            String.format("服务 %s 的实例已更新到 %s:%d",
                                    serviceItem.getServiceName(),
                                    instance.getIp(),
                                    instance.getPort()));
                } catch (Exception e) {
                    LOGGER.error("重启转发规则失败", e);
                    NotificationUtil.showError("更新失败",
                            "更新服务实例失败: " + e.getMessage());
                }
            });
        }
        saveHistory();
    }

    /**
     * 处理启动/停止代理
     */
    private void handleStartProxy() {
        try {
            if (httpProxyService.isRunning()) {
                httpProxyService.stop();
                startProxyButton.setText("启动代理");
                startProxyButton.setGraphic(new Glyph("FontAwesome", "PLAY"));
                NotificationUtil.showSuccess("停止成功", "HTTP代理已停止");
                return;
            }

            int port = Integer.parseInt(proxyPortField.getText());
            if (isPortInUse(port)) {
                NotificationUtil.showError("启动失败", "代理端口已被占用");
                return;
            }

            httpProxyService.start(port);
            startProxyButton.setText("停止代理");
            startProxyButton.setGraphic(new Glyph("FontAwesome", "STOP"));
            NotificationUtil.showSuccess("启动成功",
                    String.format("HTTP代理已启动，端口: %d", port));

        } catch (NumberFormatException e) {
            NotificationUtil.showError("输入错误", "代理端口必须是数字");
        } catch (Exception e) {
            LOGGER.error("HTTP代理操作失败", e);
            NotificationUtil.showError("操作失败", e.getMessage());
        }
    }

    private void configureTabPane(TabPane tabPane) {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // SSH端口转发选项卡
        Tab sshTab = createSshTab();

        // Nacos服务转发选项卡
        Tab nacosTab = createNacosTab();

        tabPane.getTabs().addAll(sshTab, nacosTab);

        getContentBox().setPadding(new Insets(25, 0, 15, 0));
    }
}