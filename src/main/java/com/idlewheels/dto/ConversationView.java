package com.idlewheels.dto;

import com.idlewheels.model.Listing;
import java.util.ArrayList;
import java.util.List;

public class ConversationView {
    private final Listing listing;
    private final List<ConversationMessageView> messages;
    private final Long otherUserId;
    private final String otherUserName;
    private final Long currentUserId;
    private final boolean canReply;

    public ConversationView(Listing listing, List<com.idlewheels.model.Message> messages, Long otherUserId,
                           String otherUserName, Long currentUserId, boolean canReply) {
        this.listing = listing;
        this.messages = new ArrayList<>();
        for (com.idlewheels.model.Message message : messages) {
            this.messages.add(new ConversationMessageView(message, message.getSender().getId().equals(currentUserId)));
        }
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.currentUserId = currentUserId;
        this.canReply = canReply;
    }

    public Listing getListing() { return listing; }
    public List<ConversationMessageView> getMessages() { return messages; }
    public Long getOtherUserId() { return otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public Long getCurrentUserId() { return currentUserId; }
    public boolean isCanReply() { return canReply; }
}
