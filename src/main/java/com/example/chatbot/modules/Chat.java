package com.example.chatbot.modules;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Chat {
    private String title;
    private ObservableList<String> messages;

    public Chat(String title) {
        this.title = title;
        this.messages = FXCollections.observableArrayList();
    }

    public String getTitle() {
        return title;
    }

    public ObservableList<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        messages.add(message);
    }
}