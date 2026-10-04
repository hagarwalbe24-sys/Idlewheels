package com.idlewheels.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@DiscriminatorValue("CAR")
public class Car extends Vehicle {

    @NotNull
    @Min(1)
    @Max(12)
    @Column(name = "seats")
    private Byte seats;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", length = 10)
    private Transmission transmission;

    protected Car() {
        // Required by JPA.
    }

    public Car(
            Owner owner,
            String make,
            String model,
            Short manufactureYear,
            Byte seats,
            Transmission transmission
    ) {
        super(owner, make, model, manufactureYear);
        setSeats(seats);
        setTransmission(transmission);
    }

    public Byte getSeats() {
        return seats;
    }

    public void setSeats(Byte seats) {
        if (seats == null || seats < 1 || seats > 12) {
            throw new IllegalArgumentException("Car seats must be between 1 and 12.");
        }
        this.seats = seats;
    }

    public Transmission getTransmission() {
        return transmission;
    }

    public void setTransmission(Transmission transmission) {
        if (transmission == null) {
            throw new IllegalArgumentException("Transmission is required.");
        }
        this.transmission = transmission;
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.CAR;
    }
}
