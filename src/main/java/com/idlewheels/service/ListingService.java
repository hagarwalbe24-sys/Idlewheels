package com.idlewheels.service;

import com.idlewheels.dto.ListingForm;
import com.idlewheels.model.Listing;
import com.idlewheels.model.ListingStatus;
import com.idlewheels.model.Owner;
import com.idlewheels.model.Vehicle;
import com.idlewheels.exception.ResourceNotFoundException;
import com.idlewheels.exception.ForbiddenException;
import com.idlewheels.repository.ListingRepository;
import com.idlewheels.repository.OwnerRepository;
import com.idlewheels.repository.VehicleRepository;
import com.idlewheels.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Service
public class ListingService {
    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final OwnerRepository ownerRepository;
    private final VehicleRepository vehicleRepository;
    private final StorageService storageService;

    public ListingService(
            ListingRepository listingRepository,
            OwnerRepository ownerRepository,
            VehicleRepository vehicleRepository,
            StorageService storageService
    ) {
        this.listingRepository = listingRepository;
        this.ownerRepository = ownerRepository;
        this.vehicleRepository = vehicleRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<Listing> findOwnerListings(Long ownerId) {
        return listingRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);
    }

    @Transactional(readOnly = true)
    public long countActiveOwnerListings(Long ownerId) {
        return listingRepository.countByOwner_IdAndStatusAndAvailableToGreaterThanEqual(
                ownerId, ListingStatus.ACTIVE, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long countInactiveOwnerListings(Long ownerId) {
        return listingRepository.countByOwner_IdAndStatus(ownerId, ListingStatus.INACTIVE);
    }

    @Transactional(readOnly = true)
    public long countExpiredOwnerListings(Long ownerId) {
        return listingRepository.countByOwner_IdAndStatusAndAvailableToBefore(
                ownerId, ListingStatus.ACTIVE, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Listing findOwnedListing(Long ownerId, Long listingId) {
        return listingRepository.findByIdAndOwner_Id(listingId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing was not found for this owner."));
    }

    @Transactional(readOnly = true)
    public List<Listing> findActiveListings() {
        return listingRepository.findByStatusAndAvailableToGreaterThanEqualOrderByCreatedAtDesc(
                ListingStatus.ACTIVE, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Listing findActiveListing(Long listingId) {
        Listing listing = listingRepository.findByIdAndStatus(listingId, ListingStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("This listing is unavailable."));
        if (listing.getAvailableTo().isBefore(LocalDate.now())) {
            throw new ResourceNotFoundException("This listing is unavailable.");
        }
        return listing;
    }

    @Transactional
    public Listing createListing(Long ownerId, ListingForm form) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner account was not found."));
        Vehicle vehicle = findOwnedVehicle(ownerId, form.getVehicleId());
        validateDates(form.getAvailableFrom(), form.getAvailableTo(), true);
        if (listingRepository.existsByVehicle_IdAndStatus(vehicle.getId(), ListingStatus.ACTIVE)) {
            throw new IllegalArgumentException("This vehicle already has an active listing.");
        }

        Listing listing = new Listing(
                vehicle, owner, form.getTitle(), form.getDescription(), form.getCity(),
                form.getPricePerDay(), form.getAvailableFrom(), form.getAvailableTo()
        );
        String photoKey = storageService.upload(form.getPhoto());
        listing.setPhotoKey(photoKey);
        schedulePhotoLifecycle(photoKey, null);
        return listingRepository.save(listing);
    }

    @Transactional
    public Listing updateListing(Long ownerId, Long listingId, ListingForm form) {
        Listing listing = findOwnedListing(ownerId, listingId);
        Vehicle vehicle = findOwnedVehicle(ownerId, form.getVehicleId());
        boolean startDateChanged = !java.util.Objects.equals(form.getAvailableFrom(), listing.getAvailableFrom());
        validateDates(form.getAvailableFrom(), form.getAvailableTo(), startDateChanged);
        if (listing.getStatus() == ListingStatus.ACTIVE
                && listingRepository.existsByVehicle_IdAndStatusAndIdNot(vehicle.getId(), ListingStatus.ACTIVE, listingId)) {
            throw new IllegalArgumentException("This vehicle already has another active listing.");
        }

        listing.setVehicle(vehicle);
        listing.setTitle(form.getTitle());
        listing.setDescription(form.getDescription());
        listing.setCity(form.getCity());
        listing.setPricePerDay(form.getPricePerDay());
        listing.setAvailableFrom(form.getAvailableFrom());
        listing.setAvailableTo(form.getAvailableTo());

        String previousKey = listing.getPhotoKey();
        String newKey = null;
        if (form.getPhoto() != null && !form.getPhoto().isEmpty()) {
            newKey = storageService.upload(form.getPhoto());
            listing.setPhotoKey(newKey);
        } else if (form.isRemovePhoto()) {
            listing.setPhotoKey(null);
        }
        if (!java.util.Objects.equals(previousKey, listing.getPhotoKey())) {
            schedulePhotoLifecycle(newKey, previousKey);
        }
        return listingRepository.save(listing);
    }

    @Transactional
    public void deactivateListing(Long ownerId, Long listingId) {
        Listing listing = findOwnedListing(ownerId, listingId);
        listing.setStatus(ListingStatus.INACTIVE);
        listingRepository.save(listing);
    }

    @Transactional
    public void activateListing(Long ownerId, Long listingId) {
        Listing listing = findOwnedListing(ownerId, listingId);
        if (listing.getAvailableTo().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expired listings cannot be reactivated. Edit the dates first.");
        }
        if (listingRepository.existsByVehicle_IdAndStatusAndIdNot(
                listing.getVehicle().getId(), ListingStatus.ACTIVE, listingId)) {
            throw new IllegalArgumentException("This vehicle already has another active listing.");
        }
        listing.setStatus(ListingStatus.ACTIVE);
        listingRepository.save(listing);
    }

    public String getPhotoUrl(String photoKey) {
        return storageService.getUrl(photoKey);
    }

    private Vehicle findOwnedVehicle(Long ownerId, Long vehicleId) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("Choose a vehicle for this listing.");
        }
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Choose a vehicle that exists."));
        if (!ownerId.equals(vehicle.getOwner().getId())) {
            throw new ForbiddenException("You can only list your own vehicles.");
        }
        return vehicle;
    }

    private void validateDates(LocalDate availableFrom, LocalDate availableTo, boolean validateStartNotPast) {
        if (availableFrom == null || availableTo == null) {
            throw new IllegalArgumentException("Both availability dates are required.");
        }
        if (availableTo.isBefore(availableFrom)) {
            throw new IllegalArgumentException("The end date cannot be before the start date.");
        }
        if (validateStartNotPast && availableFrom.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The availability start date cannot be in the past.");
        }
    }

    private void schedulePhotoLifecycle(String newPhotoKey, String oldPhotoKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    if (oldPhotoKey != null && !oldPhotoKey.equals(newPhotoKey)) deleteQuietly(oldPhotoKey);
                } else if (newPhotoKey != null) {
                    deleteQuietly(newPhotoKey);
                }
            }
        });
    }

    private void deleteQuietly(String photoKey) {
        try {
            storageService.delete(photoKey);
        } catch (RuntimeException exception) {
            logger.warn("Could not clean up local listing photo {}", photoKey, exception);
        }
    }
}
