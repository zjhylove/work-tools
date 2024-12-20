package com.zjhy.love.worktools.model;

import java.util.List;
import java.util.Objects;

/**
 * @author zhengjun
 */
public class NodeInfo {

    /**
     * 节点类型
     */
    private Class<?> nodeType;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点说明信息
     */
    private String nodeDesc;

    /**
     * 响应参数内容
     */
    private List<ApiField> respFieldList;

    public Class<?> getNodeType() {
        return nodeType;
    }

    public void setNodeType(Class<?> nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeDesc() {
        return nodeDesc;
    }

    public void setNodeDesc(String nodeDesc) {
        this.nodeDesc = nodeDesc;
    }

    public List<ApiField> getRespFieldList() {
        return respFieldList;
    }

    public void setRespFieldList(List<ApiField> respFieldList) {
        this.respFieldList = respFieldList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeInfo nodeInfo = (NodeInfo) o;

        if (!Objects.equals(nodeType, nodeInfo.nodeType)) return false;
        if (!Objects.equals(nodeName, nodeInfo.nodeName)) return false;
        if (!Objects.equals(nodeDesc, nodeInfo.nodeDesc)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType,nodeName);
    }
}
