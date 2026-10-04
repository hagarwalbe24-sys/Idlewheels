package com.idlewheels.service;

import com.idlewheels.dto.MessageForm;
import com.idlewheels.exception.ForbiddenException;
import com.idlewheels.exception.ResourceNotFoundException;
import com.idlewheels.model.Listing;
import com.idlewheels.model.ListingStatus;
import com.idlewheels.model.Message;
import com.idlewheels.model.Owner;
import com.idlewheels.model.Renter;
import com.idlewheels.model.User;
import com.idlewheels.model.UserRole;
import com.idlewheels.repository.ListingRepository;
import com.idlewheels.repository.MessageRepository;
import com.idlewheels.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageServiceTest {
    private static final Long LISTING_ID = 7L;
    private static final Long OWNER_ID = 1L;
    private static final Long RENTER_ID = 2L;
    private MessageRepository messageRepository;
    private ListingRepository listingRepository;
    private UserRepository userRepository;
    private MessageService messageService;
    private Listing listing;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        listingRepository = mock(ListingRepository.class);
        userRepository = mock(UserRepository.class);
        messageService = new MessageService(messageRepository, listingRepository, userRepository);
        listing = listing(ListingStatus.ACTIVE, LocalDate.now().plusDays(10));
    }

    @Test
    void openConversationMissingListingIsNotFound() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> messageService.openConversation(RENTER_ID, LISTING_ID, OWNER_ID));
    }

    @Test
    void openConversationMissingUserIsNotFound() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(userRepository.findById(RENTER_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> messageService.openConversation(RENTER_ID, LISTING_ID, OWNER_ID));
    }

    @Test
    void openConversationRejectsSelfMessaging() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        User user = user(RENTER_ID, UserRole.RENTER);
        when(userRepository.findById(RENTER_ID)).thenReturn(Optional.of(user));
        assertThrows(ForbiddenException.class,
                () -> messageService.openConversation(RENTER_ID, LISTING_ID, RENTER_ID));
    }

    @Test
    void openConversationRejectsInactiveListingWithoutHistory() {
        listing = listing(ListingStatus.INACTIVE, LocalDate.now().plusDays(10));
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(RENTER_ID, UserRole.RENTER, OWNER_ID, UserRole.OWNER);
        when(messageRepository.findConversation(LISTING_ID, RENTER_ID, OWNER_ID)).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class,
                () -> messageService.openConversation(RENTER_ID, LISTING_ID, OWNER_ID));
    }

    @Test
    void openConversationOwnerMustHaveRenterAndExistingMessages() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(OWNER_ID, UserRole.OWNER, RENTER_ID, UserRole.RENTER);
        when(messageRepository.findConversation(LISTING_ID, OWNER_ID, RENTER_ID)).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class,
                () -> messageService.openConversation(OWNER_ID, LISTING_ID, RENTER_ID));
    }

    @Test
    void openConversationRenterCannotOpenConversationWithNonOwner() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(RENTER_ID, UserRole.RENTER, 3L, UserRole.OWNER);
        when(messageRepository.findConversation(LISTING_ID, RENTER_ID, 3L)).thenReturn(List.of());
        assertThrows(ForbiddenException.class,
                () -> messageService.openConversation(RENTER_ID, LISTING_ID, 3L));
    }

    @Test
    void openConversationMarksIncomingUnreadMessagesRead() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(RENTER_ID, UserRole.RENTER, OWNER_ID, UserRole.OWNER);
        Message incoming = mock(Message.class);
        User renter = user(RENTER_ID, UserRole.RENTER);
        User owner = user(OWNER_ID, UserRole.OWNER);
        when(incoming.getReceiver()).thenReturn(renter);
        when(incoming.isRead()).thenReturn(false);
        when(incoming.getSender()).thenReturn(owner);
        when(messageRepository.findConversation(LISTING_ID, RENTER_ID, OWNER_ID)).thenReturn(List.of(incoming));

        var conversation = messageService.openConversation(RENTER_ID, LISTING_ID, OWNER_ID);
        assertTrue(conversation.isCanReply());
        verify(incoming).markAsRead();
        verify(messageRepository).saveAll(List.of(incoming));
    }

    @Test
    void openConversationWithNoUnreadMessagesDoesNotSaveAgain() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(RENTER_ID, UserRole.RENTER, OWNER_ID, UserRole.OWNER);
        Message outgoing = mock(Message.class);
        User renter = user(RENTER_ID, UserRole.RENTER);
        User owner = user(OWNER_ID, UserRole.OWNER);
        when(outgoing.getReceiver()).thenReturn(owner);
        when(outgoing.isRead()).thenReturn(true);
        when(outgoing.getSender()).thenReturn(renter);
        when(messageRepository.findConversation(LISTING_ID, RENTER_ID, OWNER_ID)).thenReturn(List.of(outgoing));

        messageService.openConversation(RENTER_ID, LISTING_ID, OWNER_ID);
        verify(messageRepository, never()).saveAll(anyList());
    }

    @Test
    void sendMessageMissingListingIsNotFound() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> messageService.sendMessage(RENTER_ID, LISTING_ID, OWNER_ID, form()));
    }

    @Test
    void sendMessageMissingAccountIsNotFound() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        when(userRepository.findById(RENTER_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> messageService.sendMessage(RENTER_ID, LISTING_ID, OWNER_ID, form()));
    }

    @Test
    void sendMessageRejectsInactiveOrExpiredListing() {
        listing = listing(ListingStatus.INACTIVE, LocalDate.now().plusDays(10));
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(RENTER_ID, UserRole.RENTER, OWNER_ID, UserRole.OWNER);
        assertThrows(ForbiddenException.class,
                () -> messageService.sendMessage(RENTER_ID, LISTING_ID, OWNER_ID, form()));
    }

    @Test
    void sendMessageRejectsSelfMessaging() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        User renter = user(RENTER_ID, UserRole.RENTER);
        when(userRepository.findById(RENTER_ID)).thenReturn(Optional.of(renter));
        assertThrows(ForbiddenException.class,
                () -> messageService.sendMessage(RENTER_ID, LISTING_ID, RENTER_ID, form()));
    }

    @Test
    void renterCannotSendToAnotherRenter() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(RENTER_ID, UserRole.RENTER, 3L, UserRole.RENTER);
        assertThrows(ForbiddenException.class,
                () -> messageService.sendMessage(RENTER_ID, LISTING_ID, 3L, form()));
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void renterCanStartOwnerConversation() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(RENTER_ID, UserRole.RENTER, OWNER_ID, UserRole.OWNER);
        messageService.sendMessage(RENTER_ID, LISTING_ID, OWNER_ID, form());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void ownerCannotReplyForAnotherOwnersListing() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(3L, UserRole.OWNER, RENTER_ID, UserRole.RENTER);
        assertThrows(ForbiddenException.class,
                () -> messageService.sendMessage(3L, LISTING_ID, RENTER_ID, form()));
    }

    @Test
    void ownerCannotStartAConversationWithoutHistory() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(OWNER_ID, UserRole.OWNER, RENTER_ID, UserRole.RENTER);
        when(messageRepository.findConversation(LISTING_ID, OWNER_ID, RENTER_ID)).thenReturn(List.of());
        assertThrows(ForbiddenException.class,
                () -> messageService.sendMessage(OWNER_ID, LISTING_ID, RENTER_ID, form()));
    }

    @Test
    void ownerCanReplyToExistingConversation() {
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
        users(OWNER_ID, UserRole.OWNER, RENTER_ID, UserRole.RENTER);
        when(messageRepository.findConversation(LISTING_ID, OWNER_ID, RENTER_ID)).thenReturn(List.of(mock(Message.class)));
        messageService.sendMessage(OWNER_ID, LISTING_ID, RENTER_ID, form());
        verify(messageRepository).save(any(Message.class));
    }

    private Listing listing(ListingStatus status, LocalDate availableTo) {
        Listing value = mock(Listing.class);
        Owner owner = mock(Owner.class);
        when(owner.getId()).thenReturn(OWNER_ID);
        when(value.getOwner()).thenReturn(owner);
        when(value.getStatus()).thenReturn(status);
        when(value.getAvailableTo()).thenReturn(availableTo);
        when(value.getId()).thenReturn(LISTING_ID);
        when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(value));
        return value;
    }

    private void users(Long firstId, UserRole firstRole, Long secondId, UserRole secondRole) {
        User first = user(firstId, firstRole);
        User second = user(secondId, secondRole);
        when(userRepository.findById(firstId)).thenReturn(Optional.of(first));
        when(userRepository.findById(secondId)).thenReturn(Optional.of(second));
    }

    private User user(Long id, UserRole role) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getRole()).thenReturn(role);
        when(user.getName()).thenReturn(role.name());
        return user;
    }

    private MessageForm form() {
        MessageForm form = new MessageForm();
        form.setContent("Hello, is this available?");
        return form;
    }
}
