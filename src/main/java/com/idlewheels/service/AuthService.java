package com.idlewheels.service;

import com.idlewheels.dto.RegistrationForm;
import com.idlewheels.dto.ProfileForm;
import com.idlewheels.model.Owner;
import com.idlewheels.model.Renter;
import com.idlewheels.model.User;
import com.idlewheels.model.UserRole;
import com.idlewheels.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegistrationForm form) {
        String email = form.getEmail().trim();

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String passwordHash = passwordEncoder.encode(form.getPassword());

        if (form.getRole() == UserRole.OWNER) {
            return userRepository.save(new Owner(
                    form.getName(), email, passwordHash, form.getPhone(), form.getCity()
            ));
        }

        if (form.getRole() == UserRole.RENTER) {
            return userRepository.save(new Renter(
                    form.getName(), email, passwordHash, form.getPhone(), form.getCity()
            ));
        }

        throw new IllegalArgumentException("Choose either Owner or Renter.");
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return Optional.empty();
        }

        return userRepository.findByEmailIgnoreCase(email.trim())
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()));
    }

    @Transactional
    public User updateProfile(Long userId, ProfileForm form) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account was not found."));
        user.setName(form.getName());
        user.setPhone(form.getPhone());
        user.setCity(form.getCity());
        if (form.getNewPassword() != null && !form.getNewPassword().isBlank()) {
            if (form.getCurrentPassword() == null
                    || !passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect.");
            }
            if (form.getNewPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalArgumentException("New password must use at most 72 UTF-8 bytes.");
            }
            user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        }
        return userRepository.save(user);
    }
}
