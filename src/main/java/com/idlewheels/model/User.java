package com.idlewheels.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "role",
        discriminatorType = DiscriminatorType.STRING,
        length = 10
)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Size(max = 100)
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Size(max = 15)
    @Column(name = "phone", length = 15)
    private String phone;

    @NotBlank
    @Size(max = 80)
    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected User() {
        // Required by JPA.
    }

    protected User(
            String name,
            String email,
            String passwordHash,
            String phone,
            String city
    ) {
        setName(name);
        setEmail(email);
        setPasswordHash(passwordHash);
        setPhone(phone);
        setCity(city);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank() || name.trim().length() > 100) {
            throw new IllegalArgumentException("Name must be 1 to 100 characters.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null
                || email.isBlank()
                || email.trim().length() > 150
                || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        this.email = email.trim();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank() || passwordHash.length() > 100) {
            throw new IllegalArgumentException("Password hash must be 1 to 100 characters.");
        }
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            this.phone = null;
            return;
        }
        String normalizedPhone = phone.trim();
        long digitCount = normalizedPhone.chars().filter(Character::isDigit).count();
        if (normalizedPhone.length() > 15 || !normalizedPhone.matches("[0-9+ -]+")
                || digitCount < 7 || digitCount > 15) {
            throw new IllegalArgumentException("Use digits, spaces, +, or - and enter 7–15 digits.");
        }
        this.phone = normalizedPhone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if (city == null || city.isBlank() || city.trim().length() > 80) {
            throw new IllegalArgumentException("City must be 1 to 80 characters.");
        }
        this.city = city.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public abstract UserRole getRole();
}
