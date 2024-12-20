package com.zjhy.love.worktools.model;

/**
 * @author zhengjun
 */
public class ApiField {

    /**
     * 属性名称
     */
    private String fieldName;

    /**
     * 属性类型名称
     */
    private String fieldType;

    /**
     * 属性类型(数组、集合时为泛型实际类型)
     */
    private Class<?> clazzType;

    /**
     * 原始属性类型
     */
    private Class<?> originClazzType;

    /**
     * 是否必须
     */
    private String required;

    /**
     * 属性长度
     */
    private String fieldLength = "";

    /**
     * 属性备注
     */
    private String comment;

    /**
     * 示例值
     */
    private String exampleValue;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Class<?> getClazzType() {
        return clazzType;
    }

    public void setClazzType(Class<?> clazzType) {
        this.clazzType = clazzType;
    }

    public Class<?> getOriginClazzType() {
        return originClazzType;
    }

    public void setOriginClazzType(Class<?> originClazzType) {
        this.originClazzType = originClazzType;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public String getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(String fieldLength) {
        this.fieldLength = fieldLength;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExampleValue() {
        return exampleValue;
    }

    public void setExampleValue(String exampleValue) {
        this.exampleValue = exampleValue;
    }
}
