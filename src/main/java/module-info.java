module com.example.chatbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;


    exports com.example.chatbot;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpmime;
    requires java.desktop;
    requires org.json;
    // Export the controllers package to javafx.fxml
    exports com.example.chatbot.controllers to javafx.fxml;
    // If you are using other modules, export those as needed
    opens com.example.chatbot to javafx.fxml;
    opens com.example.chatbot.controllers;
    opens com.example.chatbot.modules;

}