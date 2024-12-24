module com.zjhy.love.worktools {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    
    // hutool 依赖
    requires cn.hutool;
    
    requires org.apache.commons.collections4;
    requires freemarker;
    requires spire.doc;
    requires org.apache.poi.ooxml;
    requires org.slf4j;
    requires org.controlsfx.controls;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j;
    requires java.sql;
    requires java.desktop;

    // 导出和打开我们自己的包
    opens com.zjhy.love.worktools to javafx.fxml;
    opens com.zjhy.love.worktools.controller to javafx.fxml;
    opens com.zjhy.love.worktools.common.util to javafx.fxml;
    opens com.zjhy.love.worktools.common.log to org.apache.logging.log4j.core;
    opens com.zjhy.love.worktools.model to com.dlsc.formsfx;

    exports com.zjhy.love.worktools;
    exports com.zjhy.love.worktools.controller;
    exports com.zjhy.love.worktools.common.util;
    exports com.zjhy.love.worktools.common.log;
    exports com.zjhy.love.worktools.model;
    exports com.zjhy.love.worktools.service;
}