package com.idlewheels.service;

import com.idlewheels.dto.ListingForm;
import com.idlewheels.exception.ForbiddenException;
import com.idlewheels.model.Listing;
import com.idlewheels.model.Owner;
import com.idlewheels.model.Vehicle;
import com.idlewheels.repository.ListingRepository;
import com.idlewheels.repository.OwnerRepository;
import com.idlewheels.repository.VehicleRepository;
import com.idlewheels.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {
    @Mock private ListingRepository listingRepository;
    @Mock private OwnerRepository ownerRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private StorageService storageService;
    @InjectMocks private ListingService listingService;

    @Test
    void rejectsVehicleOwnedByAnotherUser() {
        Owner accountOwner = mock(Owner.class);
        Owner vehicleOwner = mock(Owner.class);
        Vehicle vehicle = mock(Vehicle.class);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(accountOwner));
        when(vehicleRepository.findById(7L)).thenReturn(Optional.of(vehicle));
        when(vehicle.getOwner()).thenReturn(vehicleOwner);
        when(vehicleOwner.getId()).thenReturn(2L);

        ListingForm form = new ListingForm();
        form.setVehicleId(7L);
        form.setTitle("Car for rent");
        form.setCity("Delhi");
        form.setPricePerDay(new BigDecimal("1000.00"));
        form.setAvailableFrom(LocalDate.parse("2026-11-01"));
        form.setAvailableTo(LocalDate.parse("2026-11-05"));

        assertThrows(ForbiddenException.class, () -> listingService.createListing(1L, form));
        verify(listingRepository, never()).save(any(Listing.class));
    }
}
