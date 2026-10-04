package com.idlewheels.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @NotBlank
    @Size(max = 120)
    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Size(max = 2000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Size(max = 80)
    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @DecimalMax(value = "1000000.00")
    @Digits(integer = 7, fraction = 2)
    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @NotNull
    @Column(name = "available_from", nullable = false)
    private LocalDate availableFrom;

    @NotNull
    @Column(name = "available_to", nullable = false)
    private LocalDate availableTo;

    @Size(max = 255)
    @Column(name = "photo_key", length = 255)
    private String photoKey;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Listing() {
        // Required by JPA.
    }

    public Listing(
            Vehicle vehicle,
            Owner owner,
            String title,
            String description,
            String city,
            BigDecimal pricePerDay,
            LocalDate availableFrom,
            LocalDate availableTo
    ) {
        setVehicle(vehicle);
        setOwner(owner);
        setTitle(title);
        setDescription(description);
        setCity(city);
        setPricePerDay(pricePerDay);
        setAvailableFrom(availableFrom);
        setAvailableTo(availableTo);
    }

    public Long getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }
        this.vehicle = vehicle;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner is required.");
        }
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank() || title.trim().length() > 120) {
            throw new IllegalArgumentException("Title must be 1 to 120 characters.");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("Description must be at most 2000 characters.");
        }
        this.description = description;
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

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        if (pricePerDay == null || pricePerDay.compareTo(BigDecimal.ZERO) <= 0
                || pricePerDay.compareTo(new BigDecimal("1000000.00")) > 0) {
            throw new IllegalArgumentException("Price per day must be greater than zero and no more than 1,000,000.00.");
        }

        try {
            BigDecimal normalizedPrice = pricePerDay.setScale(2, RoundingMode.UNNECESSARY);
            if (normalizedPrice.precision() > 10) {
                throw new IllegalArgumentException("Price exceeds the supported database precision.");
            }
            this.pricePerDay = normalizedPrice;
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Price per day can have at most two decimal places.");
        }
    }

    public LocalDate getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(LocalDate availableFrom) {
        if (availableFrom == null) {
            throw new IllegalArgumentException("Availability start date is required.");
        }
        if (availableTo != null && availableTo.isBefore(availableFrom)) {
            throw new IllegalArgumentException("Availability end date cannot be before the start date.");
        }
        this.availableFrom = availableFrom;
    }

    public LocalDate getAvailableTo() {
        return availableTo;
    }

    public void setAvailableTo(LocalDate availableTo) {
        if (availableTo == null) {
            throw new IllegalArgumentException("Availability end date is required.");
        }
        if (availableFrom != null && availableTo.isBefore(availableFrom)) {
            throw new IllegalArgumentException("Availability end date cannot be before the start date.");
        }
        this.availableTo = availableTo;
    }

    public String getPhotoKey() {
        return photoKey;
    }

    public void setPhotoKey(String photoKey) {
        if (photoKey != null && photoKey.length() > 255) {
            throw new IllegalArgumentException("Photo key must be at most 255 characters.");
        }
        this.photoKey = photoKey == null || photoKey.isBlank() ? null : photoKey.trim();
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Listing status is required.");
        }
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
