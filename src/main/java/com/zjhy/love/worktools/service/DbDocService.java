package com.zjhy.love.worktools.service;

import com.zjhy.love.worktools.common.util.OfficeDocUtil;
import com.zjhy.love.worktools.model.DbDocConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据库文档生成服务
 * 负责连接数据库获取表结构信息并生成Word格式的数据库设计文档
 *
 * @author zhengjun
 */
public class DbDocService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DbDocService.class);
    
    /**
     * 生成数据库设计文档
     * 
     * @param config 数据库文档配置信息
     * @return 生成的文档路径
     * @throws Exception 如果生成过程中发生错误
     */
    public String generateDoc(DbDocConfig config) throws Exception {
        // 获取数据库表结构信息
        List<TableInfo> tables = getTableInfo(config);
        
        // 生成文档
        String fileName = "数据库设计文档_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
            ".docx";
        String docPath = Paths.get(config.getOutputDir(), fileName).toString();
        
        // 使用模板生成文档
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("tables", tables);
        dataModel.put("generateTime", LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 渲染模板
        String xmlPath = docPath + ".xml";
        OfficeDocUtil.openOfficeXmlRender("db-doc.ftl", dataModel, xmlPath);
        
        // 转换为Word文档
        OfficeDocUtil.openOfficeXml2Docx(xmlPath, docPath);
        
        // 删除临时XML文件
        new File(xmlPath).delete();
        
        return docPath;
    }
    
    /**
     * 获取数据库表结构信息
     * 
     * @param config 数据库配置信息
     * @return 表结构信息列表
     * @throws Exception 如果获取过程中发生错误
     */
    private List<TableInfo> getTableInfo(DbDocConfig config) throws Exception {
        List<TableInfo> tables = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                config.getJdbcUrl(), 
                config.getUsername(), 
                config.getPassword())) {
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            for (String tableName : config.getTables()) {
                TableInfo table = new TableInfo();
                table.setTableName(tableName);
                
                // 获取表注释
                try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
                    if (rs.next()) {
                        table.setTableComment(rs.getString("REMARKS"));
                    }
                }
                
                // 获取列信息
                List<ColumnInfo> columns = new ArrayList<>();
                try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
                    while (rs.next()) {
                        ColumnInfo column = new ColumnInfo();
                        column.setColumnName(rs.getString("COLUMN_NAME"));
                        column.setDataType(rs.getString("TYPE_NAME"));
                        column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                        column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                        column.setColumnComment(rs.getString("REMARKS"));
                        columns.add(column);
                    }
                }
                
                // 获取主键信息
                try (ResultSet rs = metaData.getPrimaryKeys(null, null, tableName)) {
                    while (rs.next()) {
                        String columnName = rs.getString("COLUMN_NAME");
                        columns.stream()
                            .filter(c -> c.getColumnName().equals(columnName))
                            .findFirst()
                            .ifPresent(c -> c.setPrimaryKey(true));
                    }
                }
                
                table.setColumns(columns);
                tables.add(table);
            }
        }
        
        return tables;
    }
    
    /**
     * 表信息内部类
     * 用于存储单个数据库表的结构信息
     */
    private static class TableInfo {
        private String tableName;
        private String tableComment;
        private List<ColumnInfo> columns;
        
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public String getTableComment() { return tableComment; }
        public void setTableComment(String tableComment) { this.tableComment = tableComment; }
        public List<ColumnInfo> getColumns() { return columns; }
        public void setColumns(List<ColumnInfo> columns) { this.columns = columns; }
    }
    
    /**
     * 列信息内部类
     * 用于存储单个数据库列的详细信息
     */
    private static class ColumnInfo {
        private String columnName;
        private String dataType;
        private int columnSize;
        private boolean nullable;
        private boolean primaryKey;
        private String columnComment;
        
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        public int getColumnSize() { return columnSize; }
        public void setColumnSize(int columnSize) { this.columnSize = columnSize; }
        public boolean isNullable() { return nullable; }
        public void setNullable(boolean nullable) { this.nullable = nullable; }
        public boolean isPrimaryKey() { return primaryKey; }
        public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }
        public String getColumnComment() { return columnComment; }
        public void setColumnComment(String columnComment) { this.columnComment = columnComment; }
    }
} 