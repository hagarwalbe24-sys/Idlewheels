package com.idlewheels.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "sent_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime sentAt;

    @NotNull
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    protected Message() {
        // Required by JPA.
    }

    public Message(Listing listing, User sender, User receiver, String content) {
        setListing(listing);
        setSender(sender);
        setReceiver(receiver);
        setContent(content);
    }

    public Long getId() {
        return id;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        if (listing == null) {
            throw new IllegalArgumentException("Listing is required.");
        }
        this.listing = listing;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender is required.");
        }
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver is required.");
        }
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null || content.isBlank() || content.trim().length() > 1000) {
            throw new IllegalArgumentException("Message must be 1 to 1000 characters.");
        }
        this.content = content.trim();
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {
        isRead = true;
    }
}