package com.example.chatbot.controllers;

import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.json.JSONObject;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView logoImage;

    private static final String API_URL = "http://localhost:8080/api/users/login";
    private NavigationController navigationController;

    // Setter for NavigationController
    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
    @FXML
    public void initialize() {
        try {
            var imageStream = getClass().getResourceAsStream("/images/logo.png");
            if (imageStream == null) {
                throw new RuntimeException("Logo image not found at /images/logo.png");
            }
            logoImage.setImage(new Image(imageStream));
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }
    }


    public void handleLogin() {
        String email = usernameField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Username and Password must not be empty.");
            return;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/users/login")) // Ensure URL is correct
                    .POST(HttpRequest.BodyPublishers.ofString(
                            String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password)))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String role = jsonResponse.optString("role", "user"); // Default to "user" if role is not provided

                if ("admin".equalsIgnoreCase(role)) {
                    navigationController.navigateTo("admin"); // Navigate to admin page
                } else {
                    navigationController.navigateTo("chatbot"); // Navigate to chatbot interface
                }

            } else {
                showAlert("Login Failed", response.body());
            }
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }


    @FXML
    public void handleSignUp() {
        navigationController.navigateTo("signup"); // Navigate to Sign-Up page

    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }


}
