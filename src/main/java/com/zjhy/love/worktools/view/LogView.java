package com.zjhy.love.worktools.view;

import atlantafx.base.theme.Styles;
import com.zjhy.love.worktools.common.log.LogManager;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.LogEntry;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.Glyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogView extends BaseView {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogView.class);
    
    private final TableView<LogEntry> logTable = new TableView<>();
    private final ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
    private final ComboBox<String> logLevelComboBox = new ComboBox<>();
    
    public LogView() {
        // 创建工具栏
        HBox toolbar = createToolbar();
        
        // 配置日志表格
        configureLogTable();
        VBox.setVgrow(logTable, Priority.ALWAYS);
        
        // 添加到视图
        addContent(toolbar, logTable);
        
        // 初始加载日志
        refreshLogs();
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        // 日志级别选择
        Label levelLabel = new Label("日志级别:");
        logLevelComboBox.setItems(FXCollections.observableArrayList("ALL", "INFO", "ERROR"));
        logLevelComboBox.setValue("ALL");
        logLevelComboBox.getStyleClass().add(Styles.FLAT);
        logLevelComboBox.setOnAction(e -> refreshLogs());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // 工具按钮
        Button refreshButton = new Button("刷新", new Glyph("FontAwesome", "REFRESH"));
        refreshButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        refreshButton.setOnAction(e -> refreshLogs());
        
        Button clearButton = new Button("清空", new Glyph("FontAwesome", "TRASH"));
        clearButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        clearButton.setOnAction(e -> handleClear());
        
        toolbar.getChildren().addAll(
            levelLabel, logLevelComboBox, spacer,
            refreshButton, clearButton
        );
        
        return toolbar;
    }
    
    private void configureLogTable() {
        logTable.getStyleClass().add("table-striped");
        logTable.setPlaceholder(new Label("暂无日志"));
        logTable.setFixedCellSize(40);
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // 时间列
        TableColumn<LogEntry, LocalDateTime> timeColumn = new TableColumn<>("时间");
        timeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTime()));
        timeColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        timeColumn.setPrefWidth(180);
        
        // 级别列
        TableColumn<LogEntry, String> levelColumn = new TableColumn<>("级别");
        levelColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLevel()));
        levelColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("log-info", "log-warn", "log-error");
                } else {
                    setText(item);
                    getStyleClass().removeAll("log-info", "log-warn", "log-error");
                    getStyleClass().add("log-" + item.toLowerCase());
                }
            }
        });
        levelColumn.setPrefWidth(80);
        
        // 消息列
        TableColumn<LogEntry, LogEntry> messageColumn = new TableColumn<>("消息");
        messageColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        messageColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox container = new HBox(5);
            private final Label messageLabel = new Label();
            private final Button detailsButton = new Button("查看详情", new Glyph("FontAwesome", "INFO_CIRCLE"));
            
            {
                container.setAlignment(Pos.CENTER_LEFT);
                detailsButton.getStyleClass().addAll(Styles.SMALL, Styles.ACCENT);
                detailsButton.setVisible(false);
                container.getChildren().addAll(messageLabel, detailsButton);
                
                detailsButton.setOnAction(e -> {
                    LogEntry entry = getTableRow().getItem();
                    if (entry != null && entry.getThrowable() != null) {
                        showErrorDetails(entry);
                    }
                });
            }
            
            @Override
            protected void updateItem(LogEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setGraphic(null);
                } else {
                    messageLabel.setText(entry.getMessage());
                    
                    // 根据日志级别设置样式和显示详情按钮
                    switch (entry.getLevel()) {
                        case "INFO" -> {
                            messageLabel.getStyleClass().setAll("text-normal");
                            detailsButton.setVisible(false);
                        }
                        case "WARN" -> {
                            messageLabel.getStyleClass().setAll("text-warning");
                            detailsButton.setVisible(false);
                        }
                        case "ERROR" -> {
                            messageLabel.getStyleClass().setAll("text-danger");
                            detailsButton.setVisible(entry.getThrowable() != null);
                        }
                    }
                    
                    setGraphic(container);
                }
            }
        });
        messageColumn.setPrefWidth(500);
        
        logTable.getColumns().setAll(timeColumn, levelColumn, messageColumn);
        logTable.setItems(logEntries);
    }
    
    private void refreshLogs() {
        logEntries.clear();
        String selectedLevel = logLevelComboBox.getValue();
        List<LogEntry> filteredLogs = LogManager.getLogsByLevel(selectedLevel);
        logEntries.addAll(filteredLogs);
    }
    
    private void handleClear() {
        logEntries.clear();
        LogManager.clearLogs();
        NotificationUtil.showSuccess("日志已清空");
    }
    
    private void showErrorDetails(LogEntry entry) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("错误详情");
        dialog.setHeaderText(null);
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("surface-card");
        dialogPane.setPrefWidth(800);
        dialogPane.setPrefHeight(600);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        // 错误消息
        Label messageLabel = new Label("错误信息：");
        messageLabel.getStyleClass().add("text-bold");
        TextArea messageArea = new TextArea(entry.getMessage());
        messageArea.setEditable(false);
        messageArea.setPrefRowCount(2);
        
        // 堆栈信息
        Label stackLabel = new Label("堆栈信息：");
        stackLabel.getStyleClass().add("text-bold");
        TextArea stackArea = new TextArea();
        stackArea.setEditable(false);
        stackArea.setWrapText(true);
        VBox.setVgrow(stackArea, Priority.ALWAYS);
        
        // 获取堆栈信息
        if (entry.getThrowable() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            entry.getThrowable().printStackTrace(pw);
            stackArea.setText(sw.toString());
        }
        
        content.getChildren().addAll(messageLabel, messageArea, stackLabel, stackArea);
        dialogPane.setContent(content);
        
        // 添加复制按钮
        ButtonType copyButton = new ButtonType("复制堆栈", ButtonBar.ButtonData.LEFT);
        ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(copyButton, closeButton);
        
        // 设置按钮样式
        Button copy = (Button) dialogPane.lookupButton(copyButton);
        copy.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        
        Button close = (Button) dialogPane.lookupButton(closeButton);
        close.getStyleClass().add(Styles.BUTTON_OUTLINED);
        
        // 处理复制按钮事件
        copy.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content1 = new ClipboardContent();
            content1.putString(stackArea.getText());
            clipboard.setContent(content1);
            NotificationUtil.showSuccess("堆栈信息已复制到剪贴板");
        });
        
        dialog.show();
    }
} 