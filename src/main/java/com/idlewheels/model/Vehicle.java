package com.idlewheels.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "vehicle_type",
        discriminatorType = DiscriminatorType.STRING,
        length = 10
)
public abstract class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @NotBlank
    @Size(max = 60)
    @Column(name = "make", nullable = false, length = 60)
    private String make;

    @NotBlank
    @Size(max = 60)
    @Column(name = "model", nullable = false, length = 60)
    private String model;

    @NotNull
    @Min(1980)
    @Column(name = "manufacture_year", nullable = false)
    private Short manufactureYear;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Vehicle() {
        // Required by JPA.
    }

    protected Vehicle(Owner owner, String make, String model, Short manufactureYear) {
        setOwner(owner);
        setMake(make);
        setModel(model);
        setManufactureYear(manufactureYear);
    }

    public Long getId() {
        return id;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Vehicle owner is required.");
        }
        this.owner = owner;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        if (make == null || make.isBlank() || make.trim().length() > 60) {
            throw new IllegalArgumentException("Make must be 1 to 60 characters.");
        }
        this.make = make.trim();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        if (model == null || model.isBlank() || model.trim().length() > 60) {
            throw new IllegalArgumentException("Model must be 1 to 60 characters.");
        }
        this.model = model.trim();
    }

    public Short getManufactureYear() {
        return manufactureYear;
    }

    public void setManufactureYear(Short manufactureYear) {
        if (manufactureYear == null || manufactureYear < 1980
                || manufactureYear > java.time.Year.now().getValue() + 1) {
            throw new IllegalArgumentException("Vehicle year must be from 1980 through next year.");
        }
        this.manufactureYear = manufactureYear;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public abstract VehicleType getVehicleType();
}
