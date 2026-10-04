package com.idlewheels.service;

import com.idlewheels.dto.VehicleForm;
import com.idlewheels.exception.ResourceNotFoundException;
import com.idlewheels.model.Bike;
import com.idlewheels.model.Car;
import com.idlewheels.model.Owner;
import com.idlewheels.model.Vehicle;
import com.idlewheels.model.VehicleType;
import com.idlewheels.repository.ListingRepository;
import com.idlewheels.repository.OwnerRepository;
import com.idlewheels.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
public class VehicleService {
    private final OwnerRepository ownerRepository;
    private final VehicleRepository vehicleRepository;
    private final ListingRepository listingRepository;

    public VehicleService(OwnerRepository ownerRepository, VehicleRepository vehicleRepository,
                          ListingRepository listingRepository) {
        this.ownerRepository = ownerRepository;
        this.vehicleRepository = vehicleRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public Vehicle addVehicle(Long ownerId, VehicleForm form) {
        validateForm(form);
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner account was not found."));
        return vehicleRepository.save(buildVehicle(owner, form));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findOwnerVehicles(Long ownerId) {
        return vehicleRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);
    }

    @Transactional(readOnly = true)
    public Vehicle findOwnedVehicle(Long ownerId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle was not found."));
        if (!ownerId.equals(vehicle.getOwner().getId())) {
            throw new ResourceNotFoundException("Vehicle was not found.");
        }
        return vehicle;
    }

    @Transactional
    public Vehicle updateVehicle(Long ownerId, Long vehicleId, VehicleForm form) {
        Vehicle vehicle = findOwnedVehicle(ownerId, vehicleId);
        validateForm(form);
        if (form.getVehicleType() != vehicle.getVehicleType()) {
            throw new IllegalArgumentException("A vehicle's type cannot be changed after it is created.");
        }
        vehicle.setMake(form.getMake());
        vehicle.setModel(form.getModel());
        vehicle.setManufactureYear(form.getManufactureYear());
        if (vehicle instanceof Car car) {
            car.setSeats(form.getSeats().byteValue());
            car.setTransmission(form.getTransmission());
        } else if (vehicle instanceof Bike bike) {
            bike.setEngineCc(form.getEngineCc());
        }
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long ownerId, Long vehicleId) {
        Vehicle vehicle = findOwnedVehicle(ownerId, vehicleId);
        if (listingRepository.existsByVehicle_Id(vehicleId)) {
            throw new IllegalArgumentException("This vehicle has listings. Deactivate or remove its listings before deleting it.");
        }
        vehicleRepository.delete(vehicle);
    }

    private Vehicle buildVehicle(Owner owner, VehicleForm form) {
        if (form.getVehicleType() == VehicleType.CAR) {
            return new Car(owner, form.getMake(), form.getModel(), form.getManufactureYear(),
                    form.getSeats().byteValue(), form.getTransmission());
        }
        return new Bike(owner, form.getMake(), form.getModel(), form.getManufactureYear(), form.getEngineCc());
    }

    private void validateForm(VehicleForm form) {
        if (form == null || form.getVehicleType() == null) {
            throw new IllegalArgumentException("Choose Car or Bike.");
        }
        if (form.getManufactureYear() == null || form.getManufactureYear() < 1980
                || form.getManufactureYear() > Year.now().getValue() + 1) {
            throw new IllegalArgumentException("Vehicle year must be from 1980 through next year.");
        }
        if (form.getVehicleType() == VehicleType.CAR
                && (form.getSeats() == null || form.getSeats() < 1 || form.getSeats() > 12
                    || form.getTransmission() == null)) {
            throw new IllegalArgumentException("For a car, enter 1 to 12 seats and choose a transmission.");
        }
        if (form.getVehicleType() == VehicleType.BIKE
                && (form.getEngineCc() == null || form.getEngineCc() < 50 || form.getEngineCc() > 2500)) {
            throw new IllegalArgumentException("For a bike, enter an engine capacity from 50 to 2500 cc.");
        }
    }
}
