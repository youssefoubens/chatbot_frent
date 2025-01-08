package com.example.chatbot.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NavigationController {

    private Stage primaryStage; // The main stage of the application
    private Map<String, String> views; // Map to store view names and their FXML paths

    public NavigationController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.views = new HashMap<>();
        loadViews(); // Load all views into the map
    }

    // Add views to the map
    private void loadViews() {
        views.put("login", "/fxml/LoginView.fxml");
        views.put("signup", "/fxml/Sign-In.fxml");
        views.put("chatbot", "/fxml/ChatbotView.fxml");
        views.put("admin", "/fxml/admin_dashboard.fxml");

        // Add more views as needed
    }

    // Navigate to a specific view
    public void navigateTo(String viewName) {
        try {
            String fxmlPath = views.get(viewName);
            if (fxmlPath == null) {
                throw new IllegalArgumentException("View not found: " + viewName);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Inject NavigationController into the controller
            Object controller = loader.getController();
            if (controller instanceof LoginController) {
                ((LoginController) controller).setNavigationController(this);
            } else if (controller instanceof SignUpController) {
                ((SignUpController) controller).setNavigationController(this);
            } else if (controller instanceof ChatbotController) {
                ((ChatbotController) controller).setNavigationController(this);

            } else if (controller instanceof AdminController) {
                ((AdminController) controller).setNavigationController(this);
            }

            // Set the new scene
            Scene scene = new Scene(root,1000,500);
            primaryStage.setScene(scene);

            primaryStage.setTitle(viewName.toUpperCase());
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load view: " + viewName);
            e.printStackTrace();
        }
    }
}