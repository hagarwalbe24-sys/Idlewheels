package com.idlewheels.service;

import com.idlewheels.dto.ProfileForm;
import com.idlewheels.model.Renter;
import com.idlewheels.model.User;
import com.idlewheels.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UserRepository users;
    private BCryptPasswordEncoder encoder;
    private AuthService service;
    private User user;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder();
        service = new AuthService(users, encoder);
        user = new Renter("A User", "a@example.test", encoder.encode("oldpass1"), null, "Delhi");
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void profileCanChangeContactDetailsWithoutChangingEmailOrRole() {
        ProfileForm form = new ProfileForm();
        form.setName("New Name"); form.setCity("Pune"); form.setPhone("+91 9876543210");

        User updated = service.updateProfile(1L, form);

        assertEquals("New Name", updated.getName());
        assertEquals("Pune", updated.getCity());
        assertEquals("+91 9876543210", updated.getPhone());
        assertEquals("a@example.test", updated.getEmail());
        assertEquals("RENTER", updated.getRole().name());
        verify(users).save(user);
    }

    @Test
    void passwordChangeRequiresCorrectCurrentPassword() {
        ProfileForm form = new ProfileForm();
        form.setName("A User"); form.setCity("Delhi"); form.setCurrentPassword("wrongpass1");
        form.setNewPassword("newpass22");

        assertThrows(IllegalArgumentException.class, () -> service.updateProfile(1L, form));
        assertTrue(encoder.matches("oldpass1", user.getPasswordHash()));
        verify(users, never()).save(any(User.class));
    }

    @Test
    void passwordChangesWhenCurrentPasswordMatches() {
        ProfileForm form = new ProfileForm();
        form.setName("A User"); form.setCity("Delhi"); form.setCurrentPassword("oldpass1");
        form.setNewPassword("newpass22");

        service.updateProfile(1L, form);

        assertTrue(encoder.matches("newpass22", user.getPasswordHash()));
    }
}
