package com.zjhy.love.worktools.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 表信息
 *
 * @author zhengjun
 */
public class TableInfo {

    /**
     * 逻辑表
     */
    private String tableName;

    /**
     * 表概述
     */
    private String tableComment = "";

    /**
     * 索引名称
     */
    private List<String> indexNameList = new ArrayList<>();

    /**
     * 列信息
     */
    private List<ColumnInfo> fieldList = new ArrayList<>();


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public List<String> getIndexNameList() {
        return indexNameList;
    }

    public void setIndexNameList(List<String> indexNameList) {
        this.indexNameList = indexNameList;
    }

    public List<ColumnInfo> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<ColumnInfo> fieldList) {
        this.fieldList = fieldList;
    }
}
