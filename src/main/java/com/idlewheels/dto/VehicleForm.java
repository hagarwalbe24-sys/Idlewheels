package com.idlewheels.dto;

import com.idlewheels.model.Transmission;
import com.idlewheels.model.VehicleType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VehicleForm {

    @NotNull
    private VehicleType vehicleType;

    @NotBlank
    @Size(max = 60)
    private String make;

    @NotBlank
    @Size(max = 60)
    private String model;

    @NotNull
    @Min(1980)
    private Short manufactureYear;

    private Integer seats;
    private Transmission transmission;
    private Integer engineCc;

    @AssertTrue(message = "Vehicle year must be from 1980 through next year.")
    public boolean isYearInAllowedRange() {
        return manufactureYear == null || manufactureYear <= java.time.Year.now().getValue() + 1;
    }

    @AssertTrue(message = "Choose valid seats and transmission for a car, or engine capacity from 50 to 2500 cc for a bike.")
    public boolean isTypeDetailsValid() {
        if (vehicleType == null) return true;
        if (vehicleType == VehicleType.CAR) {
            return seats != null && seats >= 1 && seats <= 12 && transmission != null;
        }
        return engineCc != null && engineCc >= 50 && engineCc <= 2500;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Short getManufactureYear() {
        return manufactureYear;
    }

    public void setManufactureYear(Short manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public Transmission getTransmission() {
        return transmission;
    }

    public void setTransmission(Transmission transmission) {
        this.transmission = transmission;
    }

    public Integer getEngineCc() {
        return engineCc;
    }

    public void setEngineCc(Integer engineCc) {
        this.engineCc = engineCc;
    }
}
