package com.zjhy.love.worktools.model;

import java.time.LocalDateTime;

public class LogEntry {
    private LocalDateTime time;
    private String level;
    private String message;
    
    public LogEntry(LocalDateTime time, String level, String message) {
        this.time = time;
        this.level = level;
        this.message = message;
    }
    
    public LocalDateTime getTime() {
        return time;
    }
    
    public String getLevel() {
        return level;
    }
    
    public String getMessage() {
        return message;
    }
} 