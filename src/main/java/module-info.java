module com.example.textprocessingsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.logging;
    requires annotations;
    requires api;
    requires service;
    requires okhttp3;
    requires org.json;

    opens com.example.textprocessingsystem to javafx.fxml;
    exports com.example.textprocessingsystem;
    exports com.example.textprocessingsystem.controller;
    opens com.example.textprocessingsystem.controller to javafx.fxml;
}