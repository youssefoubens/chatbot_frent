package com.example.chatbot;

import com.example.chatbot.controllers.NavigationController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start( Stage primaryStage) throws IOException {
        // Corrected the path to the FXML file
        NavigationController navigationController = new NavigationController(primaryStage);

        // Set the initial view (e.g., Login)
        navigationController.navigateTo("login");

        // Show the stage
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}
