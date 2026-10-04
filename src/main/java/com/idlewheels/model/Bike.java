package com.idlewheels.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

@Entity
@DiscriminatorValue("BIKE")
public class Bike extends Vehicle {

    @NotNull
    @Min(50)
    @Max(2500)
    @Column(name = "engine_cc")
    private Integer engineCc;

    protected Bike() {
        // Required by JPA.
    }

    public Bike(Owner owner, String make, String model, Short manufactureYear, Integer engineCc) {
        super(owner, make, model, manufactureYear);
        setEngineCc(engineCc);
    }

    public Integer getEngineCc() {
        return engineCc;
    }

    public void setEngineCc(Integer engineCc) {
        if (engineCc == null || engineCc < 50 || engineCc > 2500) {
            throw new IllegalArgumentException("Bike engine capacity must be between 50 and 2500 cc.");
        }
        this.engineCc = engineCc;
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.BIKE;
    }
}
