package com.example.bitbusters.models;

public class ClientMessage {
    private final String text;
    private final String time;
    private final boolean sentByUser;

    public ClientMessage(String text, String time, boolean sentByUser) {
        this.text = text;
        this.time = time;
        this.sentByUser = sentByUser;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }
}
