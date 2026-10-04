package com.idlewheels.service;

import com.idlewheels.dto.ConversationSummary;
import com.idlewheels.dto.ConversationView;
import com.idlewheels.dto.MessageForm;
import com.idlewheels.exception.ForbiddenException;
import com.idlewheels.exception.ResourceNotFoundException;
import com.idlewheels.model.Listing;
import com.idlewheels.model.ListingStatus;
import com.idlewheels.model.Message;
import com.idlewheels.model.User;
import com.idlewheels.model.UserRole;
import com.idlewheels.repository.ListingRepository;
import com.idlewheels.repository.MessageRepository;
import com.idlewheels.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository,
                          ListingRepository listingRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ConversationSummary> inbox(Long currentUserId) {
        List<Message> messages = messageRepository.findAllForUser(currentUserId);
        Map<String, ConversationSummary> conversations = new LinkedHashMap<>();
        for (Message message : messages) {
            boolean sentByCurrentUser = message.getSender().getId().equals(currentUserId);
            User other = sentByCurrentUser ? message.getReceiver() : message.getSender();
            String key = message.getListing().getId() + ":" + other.getId();
            ConversationSummary summary = conversations.get(key);
            if (summary == null) {
                summary = new ConversationSummary(
                        message.getListing().getId(), other.getId(), other.getName(),
                        message.getListing().getTitle(), message.getContent(), message.getSentAt()
                );
                conversations.put(key, summary);
            }
            if (!sentByCurrentUser && !message.isRead()) {
                summary.incrementUnreadCount();
            }
        }
        return new ArrayList<>(conversations.values());
    }

    @Transactional
    public ConversationView openConversation(Long currentUserId, Long listingId, Long otherUserId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("This listing is unavailable."));
        User current = findUser(currentUserId);
        User other = findUser(otherUserId);
        Long ownerId = listing.getOwner().getId();

        if (currentUserId.equals(otherUserId)) {
            throw new ForbiddenException("You cannot message yourself.");
        }
        List<Message> messages = messageRepository.findConversation(listingId, currentUserId, otherUserId);
        boolean expired = listing.getAvailableTo().isBefore(LocalDate.now());
        if ((listing.getStatus() != ListingStatus.ACTIVE || expired) && messages.isEmpty()) {
            throw new ResourceNotFoundException("This conversation is unavailable.");
        }
        if (currentUserId.equals(ownerId)) {
            if (other.getRole() != UserRole.RENTER || messages.isEmpty()) {
                throw new ResourceNotFoundException("This conversation was not found.");
            }
        } else if (current.getRole() != UserRole.RENTER || !otherUserId.equals(ownerId)) {
            throw new ForbiddenException("You can only message the owner of this listing.");
        }

        boolean markedAnyRead = false;
        for (Message message : messages) {
            if (message.getReceiver().getId().equals(currentUserId) && !message.isRead()) {
                message.markAsRead();
                markedAnyRead = true;
            }
        }
        if (markedAnyRead) messageRepository.saveAll(messages);
        boolean canReply = listing.getStatus() == ListingStatus.ACTIVE && !expired;
        return new ConversationView(listing, messages, other.getId(), other.getName(), currentUserId, canReply);
    }

    @Transactional
    public void sendMessage(Long currentUserId, Long listingId, Long otherUserId, MessageForm form) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("This listing is unavailable."));
        User sender = findUser(currentUserId);
        User receiver = findUser(otherUserId);
        Long ownerId = listing.getOwner().getId();

        if (listing.getStatus() != ListingStatus.ACTIVE || listing.getAvailableTo().isBefore(LocalDate.now())) {
            throw new ForbiddenException("This conversation is closed because the listing is inactive or expired.");
        }

        if (currentUserId.equals(otherUserId)) {
            throw new ForbiddenException("You cannot message yourself.");
        }
        if (sender.getRole() == UserRole.RENTER) {
            if (!otherUserId.equals(ownerId)) {
                throw new ForbiddenException("A renter can message only the listing owner.");
            }
            if (listing.getStatus() != ListingStatus.ACTIVE
                    && messageRepository.findConversation(listingId, currentUserId, otherUserId).isEmpty()) {
                throw new ResourceNotFoundException("This listing is unavailable.");
            }
        } else if (sender.getRole() == UserRole.OWNER) {
            if (!currentUserId.equals(ownerId) || receiver.getRole() != UserRole.RENTER
                    || messageRepository.findConversation(listingId, currentUserId, otherUserId).isEmpty()) {
                throw new ForbiddenException("An owner can reply only to an existing conversation for their listing.");
            }
        } else {
            throw new ForbiddenException("Only renters and listing owners can message.");
        }
        messageRepository.save(new Message(listing, sender, receiver, form.getContent()));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User account was not found."));
    }
}
