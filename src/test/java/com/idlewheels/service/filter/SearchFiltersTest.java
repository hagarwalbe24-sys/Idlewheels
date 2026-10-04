package com.idlewheels.service.filter;

import com.idlewheels.model.Listing;
import com.idlewheels.model.Vehicle;
import com.idlewheels.model.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchFiltersTest {

    @Test
    void cityFilterIgnoresLetterCase() {
        Listing listing = mock(Listing.class);
        when(listing.getCity()).thenReturn("Delhi");
        assertTrue(new CityFilter("delhi").matches(listing));
    }

    @Test
    void cityFilterMatchesPartialCityNameWithoutCaseSensitivity() {
        Listing listing = mock(Listing.class);
        when(listing.getCity()).thenReturn("New Delhi NCR");
        assertTrue(new CityFilter("DELHI").matches(listing));
    }

    @Test
    void priceFilterChecksInclusiveRange() {
        Listing listing = mock(Listing.class);
        when(listing.getPricePerDay()).thenReturn(new BigDecimal("1200.00"));
        assertTrue(new PriceFilter(new BigDecimal("1000"), new BigDecimal("1500")).matches(listing));
        assertFalse(new PriceFilter(null, new BigDecimal("1000")).matches(listing));
    }

    @Test
    void vehicleTypeFilterChecksCarOrBikeType() {
        Listing listing = mock(Listing.class);
        Vehicle vehicle = mock(Vehicle.class);
        when(listing.getVehicle()).thenReturn(vehicle);
        when(vehicle.getVehicleType()).thenReturn(VehicleType.CAR);
        assertTrue(new VehicleTypeFilter(VehicleType.CAR).matches(listing));
        assertFalse(new VehicleTypeFilter(VehicleType.BIKE).matches(listing));
    }

    @Test
    void dateFilterRequiresListingToCoverWholeRequestedPeriod() {
        Listing listing = mock(Listing.class);
        when(listing.getAvailableFrom()).thenReturn(LocalDate.parse("2026-11-01"));
        when(listing.getAvailableTo()).thenReturn(LocalDate.parse("2026-11-30"));
        assertTrue(new DateFilter(LocalDate.parse("2026-11-05"), LocalDate.parse("2026-11-10")).matches(listing));
        assertFalse(new DateFilter(LocalDate.parse("2026-10-30"), LocalDate.parse("2026-11-10")).matches(listing));
    }
}
