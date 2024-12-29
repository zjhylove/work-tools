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
    private LocalDateTime time;
    
    /**
     * 日志级别（如：INFO、ERROR、WARN等）
     */
    private String level;
    
    /**
     * 日志消息内容
     */
    private String message;
    
    /**
     * 异常对象
     */
    private Throwable throwable;
    
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
     * 创建日志条目
     *
     * @param time    日志记录时间
     * @param level   日志级别
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public LogEntry(LocalDateTime time, String level, String message, Throwable throwable) {
        this.time = time;
        this.level = level;
        this.message = message;
        this.throwable = throwable;
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
     * 设置日志时间
     *
     * @param time 日志记录时间
     */
    public void setTime(LocalDateTime time) {
        this.time = time;
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
     * 设置日志级别
     *
     * @param level 日志级别
     */
    public void setLevel(String level) {
        this.level = level;
    }
    
    /**
     * 获取日志消息
     *
     * @return 日志消息内容
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 设置日志消息
     *
     * @param message 日志消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * 获取异常对象
     *
     * @return 异常对象
     */
    public Throwable getThrowable() {
        return throwable;
    }
    
    /**
     * 设置异常对象
     *
     * @param throwable 异常对象
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
} 