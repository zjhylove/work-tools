package com.zjhy.love.worktools.model;

/**
 * 表额外信息
 *
 * @author zhengjun
 */
public class ColumnInfo {

    /**
     * 列名称
     */
    private String field;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 长度
     */
    private long fieldLength;

    /**
     * 是否主键
     */
    private String isPrimary;

    /**
     * 是否可空
     */
    private String nullable;

    /**
     * 描述
     */
    private String fieldComment;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public long getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(long fieldLength) {
        this.fieldLength = fieldLength;
    }

    public String getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(String isPrimary) {
        this.isPrimary = isPrimary;
    }

    public String getNullable() {
        return nullable;
    }

    public void setNullable(String nullable) {
        this.nullable = nullable;
    }

    public String getFieldComment() {
        return fieldComment;
    }

    public void setFieldComment(String fieldComment) {
        this.fieldComment = fieldComment;
    }
}
