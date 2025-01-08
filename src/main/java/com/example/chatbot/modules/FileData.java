package com.example.chatbot.modules;

public class FileData {
    private String fileName;
    private String category;
    private String type;

    // No-argument constructor (required for JSON deserialization)
    public FileData() {}

    // Constructor with arguments
    public FileData(String fileName, String category, String type) {
        this.fileName = fileName;
        this.category = category;
        this.type = type;
    }

    // Getters and setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
