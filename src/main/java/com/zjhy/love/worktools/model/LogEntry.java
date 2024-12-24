package com.zjhy.love.worktools.model;

import java.time.LocalDateTime;

/**
 * 日志条目实体类
 * 用于存储单条日志的信息，包括时间、级别和消息内容
 *
 * @author zhengjun
 */
public class LogEntry {
    /**
     * 日志时间
     */
    private final LocalDateTime time;
    
    /**
     * 日志级别（如：INFO、ERROR、WARN等）
     */
    private final String level;
    
    /**
     * 日志消息内容
     */
    private final String message;
    
    /**
     * 创建日志条目
     *
     * @param time    日志记录时间
     * @param level   日志级别
     * @param message 日志消息
     */
    public LogEntry(LocalDateTime time, String level, String message) {
        this.time = time;
        this.level = level;
        this.message = message;
    }
    
    /**
     * 获取日志时间
     *
     * @return 日志记录时间
     */
    public LocalDateTime getTime() {
        return time;
    }
    
    /**
     * 获取日志级别
     *
     * @return 日志级别
     */
    public String getLevel() {
        return level;
    }
    
    /**
     * 获取日志消息
     *
     * @return 日志消息内容
     */
    public String getMessage() {
        return message;
    }
} 