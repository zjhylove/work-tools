package com.zjhy.love.worktools.common.log;

import com.zjhy.love.worktools.model.LogEntry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * 日志管理器
 * @author zhengjun
 */
public class LogManager {
    
    private static final ConcurrentLinkedDeque<LogEntry> logQueue = new ConcurrentLinkedDeque<>();
    private static final int MAX_LOG_SIZE = 1000; // 最大保存1000条日志
    
    private LogManager() {}
    
    /**
     * 添加日志
     */
    public static void addLog(String level, String message) {
        LogEntry entry = new LogEntry(LocalDateTime.now(), level, message);
        logQueue.offerLast(entry);
        
        // 如果超出最大容量，移除最旧的日志
        while (logQueue.size() > MAX_LOG_SIZE) {
            logQueue.pollFirst();
        }
    }
    
    /**
     * 获取所有日志
     */
    public static List<LogEntry> getAllLogs() {
        return new ArrayList<>(logQueue);
    }
    
    /**
     * 根据级别获取日志
     */
    public static List<LogEntry> getLogsByLevel(String level) {
        if ("ALL".equals(level)) {
            return getAllLogs();
        }
        return logQueue.stream()
                .filter(log -> log.getLevel().equals(level))
                .collect(Collectors.toList());
    }
    
    /**
     * 清空日志
     */
    public static void clearLogs() {
        logQueue.clear();
    }
} 