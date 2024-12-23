package com.zjhy.love.worktools.controller;

import com.zjhy.love.worktools.common.log.LogManager;
import com.zjhy.love.worktools.common.util.NotificationUtil;
import com.zjhy.love.worktools.model.LogEntry;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogViewController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogViewController.class);
    
    @FXML
    private ComboBox<String> logLevelComboBox;
    
    @FXML
    private TableView<LogEntry> logTable;
    
    @FXML
    private TableColumn<LogEntry, LocalDateTime> timeColumn;
    
    @FXML
    private TableColumn<LogEntry, String> levelColumn;
    
    @FXML
    private TableColumn<LogEntry, String> messageColumn;
    
    private final ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // 初始化日志级别下拉框
        logLevelComboBox.setItems(FXCollections.observableArrayList("ALL", "INFO", "ERROR"));
        logLevelComboBox.setValue("ALL");
        
        // 禁用TableView的列自动调整
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // 禁用行号列
        logTable.setTableMenuButtonVisible(false);
        
        // 设置表格列
        timeColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getTime()));
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
        
        levelColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getLevel()));
        messageColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMessage()));
        
        logTable.setItems(logEntries);
        
        // 初始加载日志
        refreshLogs();
    }
    
    @FXML
    private void handleRefresh() {
        refreshLogs();
    }
    
    @FXML
    private void handleClear() {
        logEntries.clear();
        LogManager.clearLogs();
        NotificationUtil.showSuccess("日志已清空");
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出日志");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("文本文件", "*.txt")
        );
        
        File file = fileChooser.showSaveDialog(logTable.getScene().getWindow());
        if (file != null) {
            try {
                List<String> lines = new ArrayList<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                for (LogEntry entry : logEntries) {
                    lines.add(String.format("%s [%s] %s",
                        formatter.format(entry.getTime()),
                        entry.getLevel(),
                        entry.getMessage()
                    ));
                }
                
                Files.write(file.toPath(), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                NotificationUtil.showSuccess("日志导出成功");
            } catch (IOException e) {
                LOGGER.error("导出日志失败", e);
                NotificationUtil.showError("导出失败", e.getMessage());
            }
        }
    }
    
    private void refreshLogs() {
        logEntries.clear();
        String selectedLevel = logLevelComboBox.getValue();
        
        List<LogEntry> filteredLogs = LogManager.getLogsByLevel(selectedLevel);
        logEntries.addAll(filteredLogs);
    }
} 