package com.zjhy.love.worktools.model;

import java.util.List;
import java.util.Set;

/**
 * @author zhengjun
 */
public class ApiInfo {

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 服务
     */
    private String d;

    /**
     * 业务
     */
    private String c;

    /**
     * 方法
     */
    private String m;

    /**
     * 版本
     */
    private String v;

    /**
     * 请求参数信息
     */
    private List<ApiField> reqFieldList;

    /**
     * 请求示例
     */
    private String reqExample;

    /**
     * 响应节点信息
     */
    private Set<NodeInfo> nodeList;

    /**
     * 响应示例
     */
    private String respExample;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getM() {
        return m;
    }

    public void setM(String m) {
        this.m = m;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public List<ApiField> getReqFieldList() {
        return reqFieldList;
    }

    public void setReqFieldList(List<ApiField> reqFieldList) {
        this.reqFieldList = reqFieldList;
    }

    public String getReqExample() {
        return reqExample;
    }

    public void setReqExample(String reqExample) {
        this.reqExample = reqExample;
    }

    public Set<NodeInfo> getNodeList() {
        return nodeList;
    }

    public void setNodeList(Set<NodeInfo> nodeList) {
        this.nodeList = nodeList;
    }

    public String getRespExample() {
        return respExample;
    }

    public void setRespExample(String respExample) {
        this.respExample = respExample;
    }
}
