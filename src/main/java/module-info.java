module com.zjhy.love.worktools {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires cn.hutool;
    requires org.apache.commons.collections4;
    requires org.apache.logging.log4j;
    requires freemarker;
    requires spire.doc;
    requires org.apache.poi.ooxml;
    requires slf4j.api;

    opens com.zjhy.love.worktools to javafx.fxml;
    opens com.zjhy.love.worktools.controller to javafx.fxml;
    exports com.zjhy.love.worktools;
    exports com.zjhy.love.worktools.controller;
}