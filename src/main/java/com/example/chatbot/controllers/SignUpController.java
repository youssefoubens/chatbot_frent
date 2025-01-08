package com.example.chatbot.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class SignUpController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField programField;

    @FXML
    private ImageView logoImage;

    private static final String API_URL = "http://localhost:8080/api/users/register";
    private NavigationController navigationController;

    // Initialize method to load the logo image
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

    // Handle the sign-up process
    @FXML
    public void handleSignUp() {
        String name = fullNameField.getText();
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String program = programField.getText(); // Get program

        // Validate input fields
        String role = "Student"; // Default role
        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null || program.isEmpty()) {
            showAlert("Validation Error", "All fields are required!");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showAlert("Validation Error", "Passwords do not match!");
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            showAlert("Validation Error", "Invalid email address!");
            return;
        }

        // Send sign-up request to the backend API
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = String.format("{\"name\":\"%s\", \"email\":\"%s\", \"password\":\"%s\", \"role\":\"%s\", \"program\":\"%s\"}",
                    name, email, password, role, program);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.printf("Response: %s", response.statusCode());

            if (response.statusCode() == 200) { // 201 Created
                showAlert("Success", "Sign Up successful! Welcome, " + name + ".");
                navigationController.navigateTo("login");
            } else {
                showAlert("Sign Up Failed", "Error: " + response.body());
            }
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    // Handle the "Go to Login" action
    @FXML
    public void handleGoToLogin() {
        navigationController.navigateTo("login");
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Helper method to display alerts
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    // Setter for NavigationController
    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
}
