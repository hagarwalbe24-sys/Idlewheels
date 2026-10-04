package com.idlewheels.repository;

import com.idlewheels.model.Listing;
import com.idlewheels.model.ListingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Override
    @EntityGraph(attributePaths = {"vehicle", "owner"})
    java.util.Optional<Listing> findById(Long id);

    @EntityGraph(attributePaths = "vehicle")
    List<Listing> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"vehicle", "owner"})
    java.util.Optional<Listing> findByIdAndOwner_Id(Long id, Long ownerId);

    @EntityGraph(attributePaths = "vehicle")
    List<Listing> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    @EntityGraph(attributePaths = {"vehicle", "owner"})
    List<Listing> findByStatusAndAvailableToGreaterThanEqualOrderByCreatedAtDesc(
            ListingStatus status, LocalDate today);

    boolean existsByVehicle_IdAndStatus(Long vehicleId, ListingStatus status);

    boolean existsByVehicle_Id(Long vehicleId);

    boolean existsByVehicle_IdAndStatusAndIdNot(Long vehicleId, ListingStatus status, Long listingId);

    long countByOwner_IdAndStatus(Long ownerId, ListingStatus status);

    long countByOwner_IdAndStatusAndAvailableToBefore(Long ownerId, ListingStatus status, LocalDate date);

    long countByOwner_IdAndStatusAndAvailableToGreaterThanEqual(Long ownerId, ListingStatus status, LocalDate date);

    @EntityGraph(attributePaths = {"vehicle", "owner"})
    Optional<Listing> findByIdAndStatus(Long id, ListingStatus status);
}
