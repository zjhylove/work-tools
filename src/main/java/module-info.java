module com.zjhy.love.worktools {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires cn.hutool;

    exports com.zjhy.love.worktools.controller;
    opens com.zjhy.love.worktools.controller to javafx.fxml;

    exports com.zjhy.love.worktools;
    opens com.zjhy.love.worktools to javafx.fxml;
}