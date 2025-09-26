package com.example.taskgame.domain.models;

import com.google.firebase.Timestamp;

public class Message {
    private String sender;
    private String text;
    private Timestamp timestamp;

    public Message() {}

    public String getSender() { return sender; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }
}
