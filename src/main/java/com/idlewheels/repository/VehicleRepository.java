package com.idlewheels.repository;

import com.idlewheels.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "owner")
    java.util.Optional<Vehicle> findById(Long id);

    List<Vehicle> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);
}
