package com.example.chatbot.controllers;

import com.example.chatbot.modules.Chat;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatbotController {

    @FXML
    private TextField messageField; // Input field for user messages

    @FXML
    private ComboBox<String> contextComboBox; // Dropdown for context selection

    @FXML
    private TextField customContextField; // TextField for custom context input



    @FXML
    private VBox messagesContainer; // Container to display chat messages

    @FXML
    private VBox chatTitlesContainer; // Container to display chat titles
    private NavigationController navigationController;

    private static final String API_URL = "http://localhost:8080/chat1"; // API endpoint

    // Executor for running tasks in the background
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
    private Map<String, Chat> chats = new HashMap<>(); // Store all chats
    private Chat currentChat; // Current active chat

    // Initialize the context options
    @FXML
    public void initialize() {
        contextComboBox.getItems().addAll(
                "General Inquiry", // For general questions about ENSET Mohammedia
                "Admissions", // For questions related to admissions
                "Academic Programs", // For questions about programs like engineering, computer science, etc.
                "Exams and Schedules", // For exam-related queries
                "Student Services", // For services like counseling, clubs, etc
                "Research and Projects", // For research-related inquiries
                "Internships and Careers", // For internship and career opportunities
                "Events and Workshops", // For school events, seminars, and workshops
                "Technical Support", // For IT or technical issues
                "Other" // For any other inquiries
        );
        contextComboBox.setValue("General Inquiry"); // Set default context

        // Hide the custom context field initially
        customContextField.setVisible(false);

        // Add listener to handle "Other" selection
        contextComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Other".equals(newVal)) {
                // Show the custom context field and hide the ComboBox
                customContextField.setVisible(true);
                contextComboBox.setVisible(false);
                customContextField.requestFocus(); // Focus on the custom context field
            } else {
                // Show the ComboBox and hide the custom context field
                customContextField.setVisible(false);
                contextComboBox.setVisible(true);
            }
        });
    }

    // Method to send a message
    @FXML
    public void sendMessage() {
        String message = messageField.getText().trim(); // Get and trim the message
        if (message.isEmpty()) return; // Ignore empty messages

        // Get the selected context or custom context
        String context;
        if (contextComboBox.isVisible()) {
            context = contextComboBox.getValue();
        } else {
            context = customContextField.getText().trim();
            if (context.isEmpty()) {
                // If custom context is empty, show an error
                addMessageToChat("Error: Please enter a custom context.", "error-message");
                return;
            }
        }

        // If no chat exists, create a new one
        if (currentChat == null) {
            String chatTitle = extractChatTitle(message);
            currentChat = new Chat(chatTitle);
            chats.put(chatTitle, currentChat);
            addChatTitle(chatTitle);
        }

        // Add user message to the current chat
        currentChat.addMessage("You: " + message);
        addMessageToChat("You: " + message, "user-message");

        // Run the network request in the background
        executor.submit(() -> {
            try {
                // Construct the query parameters
                String requestUrl = String.format("%s?message=%s&context=%s", API_URL,
                        URLEncoder.encode(message, StandardCharsets.UTF_8),
                        URLEncoder.encode(context, StandardCharsets.UTF_8));

                // Create HTTP client and request
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(requestUrl))
                        .GET() // Using GET method since we're passing query parameters
                        .build();

                // Send the request and get the response
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Update the UI if the response is successful
                if (response.statusCode() == 200) {
                    String botMessage = response.body();
                    // Update the chat UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        currentChat.addMessage("Bot: " + botMessage);
                        addMessageToChat("Bot: " + botMessage, "bot-message");
                    });
                } else {
                    // Handle non-200 responses
                    Platform.runLater(() -> {
                        currentChat.addMessage("Error: Failed to get response (Status: " + response.statusCode() + ")");
                        addMessageToChat("Error: Failed to get response (Status: " + response.statusCode() + ")", "error-message");
                    });
                }
            } catch (Exception e) {
                // Handle exceptions like IO or network issues
                Platform.runLater(() -> {
                    currentChat.addMessage("Error: " + e.getMessage());
                    addMessageToChat("Error: " + e.getMessage(), "error-message");
                });
            }
        });

        // Clear the message field after sending the message
        messageField.clear();
    }

    // Helper method to add messages to the chat container
    private void addMessageToChat(String message, String styleClass) {
        Label label = new Label(message);
        label.getStyleClass().add(styleClass); // Add the appropriate style class
        label.setWrapText(true); // Enable text wrapping

        HBox messageContainer = new HBox(label);
        messageContainer.setPadding(new Insets(5));
        if ("user-message".equals(styleClass)) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT); // Align user messages to the right
        } else {
            messageContainer.setAlignment(Pos.CENTER_LEFT); // Align bot and error messages to the left
        }

        messagesContainer.getChildren().add(messageContainer); // Add the message container to the chat
    }


    // Method to handle key events in the message field
    @FXML
    public void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage(); // Send the message when Enter is pressed
            event.consume(); // Prevent default behavior (e.g., new line)
        }
    }

    // Method to create a new chat
    @FXML
    public void createNewChat() {
        initialize();
        // Clear the messages container for a new chat
        messagesContainer.getChildren().clear();
        currentChat = null; // Reset the current chat
    }

    // Method to extract the chat title from the first message
    private String extractChatTitle(String message) {
        // Extract the first few words or a specific context from the message
        String[] words = message.split("\\s+");
        if (words.length > 0) {
            return String.join(" ", words).substring(0, Math.min(20, message.length())) + "..."; // Limit title length
        }
        return "New Chat"; // Default title if no words are found
    }

    // Method to add a chat title to the sidebar
    private void addChatTitle(String title) {
        Label chatTitle = new Label(title);
        chatTitle.getStyleClass().add("chat-title");
        chatTitle.setOnMouseClicked(event -> switchChat(title)); // Switch chat when clicked
        chatTitlesContainer.getChildren().add(chatTitle);
    }

    // Method to switch to a specific chat
    private void switchChat(String title) {
        currentChat = chats.get(title); // Set the current chat
        messagesContainer.getChildren().clear(); // Clear the messages container
        // Load all messages for the selected chat
        for (String message : currentChat.getMessages()) {
            addMessageToChat(message, message.startsWith("You:") ? "user-message" : "bot-message");
        }
    }

    @FXML
    public void logout() {
        navigationController.navigateTo("login");

    }

}