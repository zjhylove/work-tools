package com.zjhy.love.worktools.model;

import java.util.List;
import java.util.Map;

public class ApiDocConfig {
    private String sourceJarPath;        // 源码jar包路径
    private String serviceName;          // 服务名称
    private List<String> dependencyJars; // 依赖jar包列表
    private Map<String, List<String>> classPathMapping; // 类名和路径映射


    public String getSourceJarPath() {
        return sourceJarPath;
    }

    public void setSourceJarPath(String sourceJarPath) {
        this.sourceJarPath = sourceJarPath;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getDependencyJars() {
        return dependencyJars;
    }

    public void setDependencyJars(List<String> dependencyJars) {
        this.dependencyJars = dependencyJars;
    }

    public Map<String, List<String>> getClassPathMapping() {
        return classPathMapping;
    }

    public void setClassPathMapping(Map<String, List<String>> classPathMapping) {
        this.classPathMapping = classPathMapping;
    }
}