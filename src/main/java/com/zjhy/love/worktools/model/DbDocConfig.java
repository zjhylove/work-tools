package com.zjhy.love.worktools.model;

import java.util.List;

/**
 * 数据库文档配置类
 * 用于保存生成数据库文档所需的配置信息
 *
 * @author zhengjun
 */
public class DbDocConfig {
    /**
     * 数据库连接URL
     * JDBC格式的数据库连接字符串
     */
    private String jdbcUrl;

    /**
     * 数据库用户名
     * 用于数据库认证的用户名
     */
    private String username;

    /**
     * 数据库密码
     * 用于数据库认证的密码
     */
    private String password;

    /**
     * 需要导出的表名列表
     * 指定要生成文档的数据库表，为空则导出所有表
     */
    private List<String> tables;

    /**
     * 文档输出目录
     * 生成的数据库文档保存路径
     */
    private String outputDir;

    // Getters and Setters
    /**
     * 获取数据库连接URL
     * @return 数据库连接URL
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * 设置数据库连接URL
     * @param jdbcUrl 数据库连接URL
     */
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * 获取数据库用户名
     * @return 数据库用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置数据库用户名
     * @param username 数据库用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取数据库密码
     * @return 数据库密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置数据库密码
     * @param password 数据库密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取需要导出的表名列表
     * @return 表名列表
     */
    public List<String> getTables() {
        return tables;
    }

    /**
     * 设置需要导出的表名列表
     * @param tables 表名列表
     */
    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    /**
     * 获取文档输出目录
     * @return 输出目录路径
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * 设置文档输出目录
     * @param outputDir 输出目录路径
     */
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
} 