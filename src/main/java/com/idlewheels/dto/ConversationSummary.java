package com.idlewheels.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConversationSummary {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private final Long listingId;
    private final Long otherUserId;
    private final String otherUserName;
    private final String listingTitle;
    private final String latestMessage;
    private final LocalDateTime latestSentAt;
    private int unreadCount;

    public ConversationSummary(Long listingId, Long otherUserId, String otherUserName,
                               String listingTitle, String latestMessage, LocalDateTime latestSentAt) {
        this.listingId = listingId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.listingTitle = listingTitle;
        this.latestMessage = latestMessage == null ? "" : latestMessage.length() <= 120
                ? latestMessage : latestMessage.substring(0, 117) + "…";
        this.latestSentAt = latestSentAt;
    }

    public Long getListingId() { return listingId; }
    public Long getOtherUserId() { return otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public String getListingTitle() { return listingTitle; }
    public String getLatestMessage() { return latestMessage; }
    public LocalDateTime getLatestSentAt() { return latestSentAt; }
    public String getLatestSentAtFormatted() { return latestSentAt == null ? "Time unavailable" : latestSentAt.format(DISPLAY_TIME); }
    public int getUnreadCount() { return unreadCount; }
    public void incrementUnreadCount() { unreadCount++; }
}
