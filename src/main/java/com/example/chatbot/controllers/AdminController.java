package com.example.chatbot.controllers;

import com.example.chatbot.modules.FileData;
import com.example.chatbot.services.FileUploader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {

    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private TableView<FileData> fileTable;
    @FXML
    private TableColumn<FileData, String> fileNameColumn;
    @FXML
    private TableColumn<FileData, String> categoryColumn;
    @FXML
    private TableColumn<FileData, String> deleteColumn;
    @FXML
    private TextField titleField;
    @FXML
    private TextField programField;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> contextComboBox;
    private List<FileData> allFiles;
    private File selectedFile;
    private NavigationController navigationController;
    private static final String BASE_URL = "http://localhost:8080/documents";


    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }
    @FXML
    public void initialize() {
        categoryComboBox.getItems().addAll("others", "td", "tp", "exams", "cours");
        contextComboBox.getItems().addAll(
                "General Inquiry",  // For general questions about ENSET Mohammedia
                "Admissions",  // For questions related to admissions
                "Academic Programs",  // For questions about programs like engineering, computer science, etc.
                "Exams and Schedules",  // For exam-related queries
                "Student Services",  // For services like counseling, clubs, etc
                "Research and Projects",  // For research-related inquiries
                "Internships and Careers",  // For internship and career opportunities
                "Events and Workshops",  // For school events, seminars, and workshops
                "Technical Support",  // For IT or technical issues
                "Other"  // For any other inquiries
        );
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
//        deleteColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        listAllFiles();
    }

    public void clearfields() {
        // Clear the text fields
        titleField.clear();
        programField.clear();
        searchField.clear();

        // Reset the combo boxes
        categoryComboBox.getSelectionModel().clearSelection();
        contextComboBox.getSelectionModel().clearSelection();

        // Deselect any selected file
        selectedFile = null;

        // Clear file selection from the table view

    }

    @FXML
    public void listAllFiles() {
        // Fetch all file paths as strings
        List<String> filePaths = makeGetRequest("/all");
        if (filePaths != null) {
            // Create FileData objects from the file paths
            allFiles = filePaths.stream()
                    .map(filePath -> {
                        String fileName = new File(filePath).getName();
                        String category = getCategoryFromPath(filePath);
                        return new FileData(fileName, category, getFileType(fileName));
                    })
                    .toList();

            // Set the table with FileData
            fileTable.getItems().setAll(allFiles);
        }
    }

    private String getCategoryFromPath(String filePath) {
        // Example of extracting category based on the path
        if (filePath.contains("cours")) {
            return "cours";
        } else if (filePath.contains("exams")) {
            return "exams";
        } else if (filePath.contains("td")) {
            return "td";
        } else if (filePath.contains("tp")) {
            return "tp";
        }
        return "others";  // Default category
    }

    private String getFileType(String fileName) {
        // Simple logic to determine file type based on the extension (you can modify this)
        if (fileName.endsWith(".pdf")) {
            return "PDF";
        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg")) {
            return "Image";
        }
        return "Unknown";
    }

    @FXML
    public void listFiles() {
        String category = categoryComboBox.getValue();
        if (category == null || category.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select a category.");
            return;
        }

        // Filter files by category
        List<FileData> filteredFiles = allFiles.stream()
                .filter(file -> file.getCategory().equalsIgnoreCase(category))
                .toList();

        fileTable.getItems().setAll(filteredFiles);
    }

    @FXML
    public void filterFiles() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            fileTable.getItems().setAll(allFiles);
            return;
        }

        // Filter files based on search text and selected category
        List<FileData> filteredFiles = allFiles.stream()
                .filter(file -> file.getFileName().toLowerCase().contains(searchText)
                        || file.getCategory().toLowerCase().contains(searchText))
                .toList();

        fileTable.getItems().setAll(filteredFiles);
    }

    @FXML
    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(new Stage());
    }

    @FXML
    public void uploadDocument() {
        if (selectedFile == null) {
            showAlert(Alert.AlertType.ERROR, "No file selected.");
            return;
        }

        String title = titleField.getText();
        String program = programField.getText();
        String context = contextComboBox.getValue();
        String category = categoryComboBox.getValue();

        if (isAnyFieldEmpty(title, program, context, category)) {
            showAlert(Alert.AlertType.ERROR, "All fields must be filled in.");
            return;
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("title", title);
        metadata.put("program", program);
        metadata.put("context", context);
        metadata.put("category", category);
        metadata.put("filename", selectedFile.getName());

        try {
            FileUploader.uploadFile(selectedFile, metadata);
            showAlert(Alert.AlertType.INFORMATION, "Document uploaded successfully.");
            listAllFiles();
            clearfields();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error uploading document: " + e.getMessage());
        }
    }

    @FXML
    public void deleteFile() {
        FileData selectedFileData = fileTable.getSelectionModel().getSelectedItem();
        if (selectedFileData != null) {
            makeDeleteRequest("/delete?category=" + selectedFileData.getCategory() +
                    "&fileName=" + selectedFileData.getFileName());
            listFiles();
        }
    }

    private List<String> makeGetRequest(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseJson(response.body());
            } else {
                showAlert(Alert.AlertType.ERROR, "Error fetching files.");
                return null;
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
            return null;
        }
    }

    private void makeDeleteRequest(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + url))
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                showAlert(Alert.AlertType.INFORMATION, "File deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error deleting file: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<String> parseJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            System.err.println("Failed to parse JSON: " + json); // Log raw JSON for debugging
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.show();
    }

    private boolean isAnyFieldEmpty(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @FXML
    public void logout() {
        // Close the current stage
        navigationController.navigateTo("login");
    }

}
