package com.idlewheels.dto;

import com.idlewheels.model.Message;

import java.time.format.DateTimeFormatter;

public class ConversationMessageView {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private final Message message;
    private final boolean sentByCurrentUser;

    public ConversationMessageView(Message message, boolean sentByCurrentUser) {
        this.message = message;
        this.sentByCurrentUser = sentByCurrentUser;
    }

    public Message getMessage() { return message; }
    public boolean isSentByCurrentUser() { return sentByCurrentUser; }
    public String getSentAtFormatted() {
        return message.getSentAt() == null ? "Just now" : message.getSentAt().format(DISPLAY_TIME);
    }
}
