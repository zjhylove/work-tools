package com.zjhy.love.worktools.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import com.zjhy.love.worktools.common.util.OfficeDocUtil;
import com.zjhy.love.worktools.model.ColumnInfo;
import com.zjhy.love.worktools.model.DbDocConfig;
import com.zjhy.love.worktools.model.TableInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库文档生成服务
 * 负责连接数据库获取表结构信息并生成Word格式的数据库设计文档
 *
 * @author zhengjun
 */
public class DbDocService {

    private static final Logger LOGGER = LogManager.getLogger(DbDocService.class);


    /**
     * 查询表对应的列sql
     */
    private static final String SELECT_TABLE_COLUMN_SQL = "SELECT " +
            "t.COLUMN_NAME, " +
            "t.DATA_TYPE, " +
            "IFNULL( t.CHARACTER_MAXIMUM_LENGTH, IFNULL( t.NUMERIC_PRECISION, t.DATETIME_PRECISION ) ) COLUMN_LENGTH, " +
            "CASE " +
            "t.COLUMN_KEY  " +
            "WHEN 'PRI' THEN " +
            "'是' ELSE ''  " +
            "END, " +
            "CASE " +
            "t.IS_NULLABLE  " +
            "WHEN 'NO' THEN " +
            "'否' ELSE '是'  " +
            "END, " +
            "t.COLUMN_COMMENT  " +
            "FROM " +
            "information_schema.COLUMNS t  " +
            "WHERE " +
            "t.TABLE_NAME = ?  " +
            "AND t.TABLE_SCHEMA = ?  " +
            "ORDER BY " +
            "t.ORDINAL_POSITION ASC;";

    /**
     * 查询表概述sql
     */
    private static final String SELECT_TABLE_COMMENT_SQL = "select t.TABLE_COMMENT  " +
            "from information_schema.TABLES t " +
            "where t.TABLE_NAME = ? " +
            "  and t.TABLE_SCHEMA = ? ";

    /**
     * 查询索引信息
     */
    private static final String SELECT_TABLE_INDEX_SQL = "SELECT INDEX_NAME,group_concat(column_name order by seq_in_index) INDEX_FIELD " +
            "FROM information_schema.STATISTICS " +
            "WHERE TABLE_NAME = ? " +
            "AND TABLE_SCHEMA = ? GROUP BY index_name ORDER BY CHAR_LENGTH(index_name);";


    /**
     * 生成数据库设计文档
     *
     * @param config 数据库文档配置信息
     * @return 生成的文档路径
     * @throws Exception 如果生成过程中发生错误
     */
    public String generateDoc(DbDocConfig config) throws Exception {
        //导出word
        findTableInfo(config).forEach(t -> {
            String filePrefix = config.getOutputDir() + File.separator + t.getTableName();
            String renderXmlFile = filePrefix + ".xml";
            String docxFile = filePrefix + ".docx";
            try {
                OfficeDocUtil.openOfficeXmlRender("db-doc-template.ftl", t, renderXmlFile);
                OfficeDocUtil.openOfficeXml2Docx(renderXmlFile, docxFile);
            } catch (Exception e) {
                LOGGER.error(() -> "表【" + t + "】导出文档失败", e);
                ExceptionUtil.wrapAndThrow(e);
            } finally {
                FileUtil.del(renderXmlFile);
            }
        });
        return config.getOutputDir();
    }


    /**
     * 查询表信息
     *
     * @return 表信息
     * @throws SQLException 异常
     */
    public List<TableInfo> findTableInfo(DbDocConfig config) throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getUsername(),
                config.getPassword())) {
            String database = database(config.getJdbcUrl());
            return parseTableInfo(conn, database, config.getTables());
        }
    }

    /**
     * 构建表信息
     *
     * @param exportTables 导出的表列表
     * @return 解析后的表信息
     */
    private List<TableInfo> parseTableInfo(Connection connection, String database, List<String> exportTables) throws SQLException {
        try (PreparedStatement ps1 = connection.prepareStatement(SELECT_TABLE_COLUMN_SQL);
             PreparedStatement ps2 = connection.prepareStatement(SELECT_TABLE_COMMENT_SQL);
             PreparedStatement ps3 = connection.prepareStatement(SELECT_TABLE_INDEX_SQL)) {
            return exportTables.stream().map(t -> {
                TableInfo table = new TableInfo();
                //名称
                table.setTableName(t);
                //列信息
                try {
                    List<ColumnInfo> columnInfoList = findColumnInfo(ps1, database, t);
                    table.setFieldList(columnInfoList);
                } catch (SQLException e) {
                    LOGGER.error(e);
                    throw ExceptionUtil.wrapRuntime(e);
                }
                //表概述
                try {
                    table.setTableComment(findTableComment(ps2, database, t));
                } catch (SQLException e) {
                    LOGGER.error(e);
                    throw ExceptionUtil.wrapRuntime(e);
                }
                //索引信息
                try {
                    table.setIndexNameList(findTableIndex(ps3, database, t));
                } catch (SQLException e) {
                    LOGGER.error(e);
                    throw ExceptionUtil.wrapRuntime(e);
                }
                return table;
            }).collect(Collectors.toList());
        }
    }

    /**
     * 查询表对应的列信息
     *
     * @param ps       预编译对象
     * @param database 数据库
     * @param table    实际表名称
     * @return 列信息
     * @throws SQLException sql 异常
     */
    private List<ColumnInfo> findColumnInfo(PreparedStatement ps, String database, String table) throws SQLException {
        ps.setString(1, table);
        ps.setString(2, database);
        ResultSet resultSet = ps.executeQuery();
        List<ColumnInfo> columnList = new ArrayList<>();
        while (resultSet.next()) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setField(resultSet.getObject(1, String.class));
            columnInfo.setFieldType(resultSet.getObject(2, String.class));
            columnInfo.setFieldLength(resultSet.getObject(3, long.class));
            columnInfo.setIsPrimary(resultSet.getObject(4, String.class));
            columnInfo.setNullable(resultSet.getObject(5, String.class));
            columnInfo.setFieldComment(resultSet.getObject(6, String.class));
            columnList.add(columnInfo);
        }
        return columnList;
    }

    /**
     * 查询表对应的列信息
     *
     * @param ps       预编译对象
     * @param database 数据库
     * @param table    实际表名称
     * @return 列信息
     * @throws SQLException sql 异常
     */
    private String findTableComment(PreparedStatement ps, String database, String table) throws SQLException {
        ps.setString(1, table);
        ps.setString(2, database);
        ResultSet resultSet = ps.executeQuery();
        resultSet.next();
        return resultSet.getObject(1, String.class);
    }

    /**
     * 查询表对应的索引信息
     *
     * @param ps       预编译对象
     * @param database 数据库
     * @param table    实际表名称
     * @return 索引信息
     * @throws SQLException sql 异常
     */
    private List<String> findTableIndex(PreparedStatement ps, String database, String table) throws SQLException {
        ps.setString(1, table);
        ps.setString(2, database);
        ResultSet resultSet = ps.executeQuery();
        List<String> indexList = new ArrayList<>();
        while (resultSet.next()) {
            String indexName = resultSet.getObject(1, String.class);
            String indexField = resultSet.getObject(2, String.class);
            indexList.add(indexName + "(" + indexField + ")");
        }
        return indexList;
    }

    private String database(String JdbcUrl) {
        return JdbcUrl.substring(JdbcUrl.lastIndexOf("/") + 1);
    }
}
