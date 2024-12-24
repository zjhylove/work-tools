package com.zjhy.love.worktools.model;

import java.util.List;

/**
 * 数据库文档配置类
 * 用于保存生成数据库文档所需的配置信息
 *
 * @author zhengjun
 */
public class DbDocConfig {
    /** 数据库连接URL */
    private String jdbcUrl;
    /** 数据库用户名 */
    private String username;
    /** 数据库密码 */
    private String password;
    /** 需要导出的表名列表 */
    private List<String> tables;
    /** 文档输出目录 */
    private String outputDir;

    // Getters and Setters
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
} 