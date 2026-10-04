package com.idlewheels.service;

import com.idlewheels.dto.VehicleForm;
import com.idlewheels.exception.ResourceNotFoundException;
import com.idlewheels.model.Car;
import com.idlewheels.model.Owner;
import com.idlewheels.model.Transmission;
import com.idlewheels.model.Vehicle;
import com.idlewheels.model.VehicleType;
import com.idlewheels.repository.ListingRepository;
import com.idlewheels.repository.OwnerRepository;
import com.idlewheels.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VehicleServiceTest {
    private OwnerRepository owners;
    private VehicleRepository vehicles;
    private ListingRepository listings;
    private VehicleService service;
    private Owner owner;
    private Car car;

    @BeforeEach
    void setUp() {
        owners = mock(OwnerRepository.class);
        vehicles = mock(VehicleRepository.class);
        listings = mock(ListingRepository.class);
        service = new VehicleService(owners, vehicles, listings);
        owner = mock(Owner.class);
        when(owner.getId()).thenReturn(10L);
        when(owners.findById(10L)).thenReturn(Optional.of(owner));
        car = new Car(owner, "Toyota", "Corolla", (short) 2022, (byte) 5, Transmission.AUTOMATIC);
        when(vehicles.findById(20L)).thenReturn(Optional.of(car));
    }

    @Test
    void addCarBuildsAndSavesVehicle() {
        when(vehicles.save(any(Vehicle.class))).thenAnswer(call -> call.getArgument(0));
        Vehicle saved = service.addVehicle(10L, carForm());
        assertInstanceOf(Car.class, saved);
        assertEquals("Toyota", saved.getMake());
        verify(vehicles).save(any(Car.class));
    }

    @Test
    void addRejectsInvalidCarDetailsBeforeSaving() {
        VehicleForm form = carForm();
        form.setSeats(0);
        assertThrows(IllegalArgumentException.class, () -> service.addVehicle(10L, form));
        verify(vehicles, never()).save(any());
    }

    @Test
    void otherOwnersCannotReadOrUpdateVehicle() {
        when(vehicles.findById(20L)).thenReturn(Optional.of(car));
        assertThrows(ResourceNotFoundException.class, () -> service.findOwnedVehicle(99L, 20L));
        assertThrows(ResourceNotFoundException.class, () -> service.updateVehicle(99L, 20L, carForm()));
    }

    @Test
    void vehicleTypeCannotChangeAfterCreation() {
        VehicleForm form = carForm();
        form.setVehicleType(VehicleType.BIKE);
        form.setEngineCc(150);
        assertThrows(IllegalArgumentException.class, () -> service.updateVehicle(10L, 20L, form));
        verify(vehicles, never()).save(any());
    }

    @Test
    void vehicleWithListingsCannotBeDeleted() {
        when(listings.existsByVehicle_Id(20L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.deleteVehicle(10L, 20L));
        verify(vehicles, never()).delete(any(Vehicle.class));
    }

    private VehicleForm carForm() {
        VehicleForm form = new VehicleForm();
        form.setVehicleType(VehicleType.CAR);
        form.setMake("Toyota");
        form.setModel("Corolla");
        form.setManufactureYear((short) 2022);
        form.setSeats(5);
        form.setTransmission(Transmission.AUTOMATIC);
        return form;
    }
}
