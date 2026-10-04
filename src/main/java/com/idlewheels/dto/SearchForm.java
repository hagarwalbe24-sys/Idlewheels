package com.idlewheels.dto;

import com.idlewheels.model.VehicleType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SearchForm {
    @Size(max = 80)
    private String city;
    private VehicleType vehicleType;
    @DecimalMin(value = "0.00", inclusive = false)
    @DecimalMax(value = "1000000.00")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal minimumPrice;
    @DecimalMin(value = "0.00", inclusive = false)
    @DecimalMax(value = "1000000.00")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal maximumPrice;
    private LocalDate availableFrom;
    private LocalDate availableTo;
    private ListingSort sort = ListingSort.NEWEST;
    @Min(0)
    private int page;

    @AssertTrue(message = "Minimum price cannot exceed maximum price.")
    public boolean isPriceRangeValid() {
        return minimumPrice == null || maximumPrice == null || minimumPrice.compareTo(maximumPrice) <= 0;
    }

    @AssertTrue(message = "Choose both dates, or leave both empty.")
    public boolean isDatePairValid() {
        return (availableFrom == null) == (availableTo == null);
    }

    @AssertTrue(message = "End date cannot be before start date.")
    public boolean isDateOrderValid() {
        return availableFrom == null || availableTo == null || !availableTo.isBefore(availableFrom);
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public BigDecimal getMinimumPrice() { return minimumPrice; }
    public void setMinimumPrice(BigDecimal minimumPrice) { this.minimumPrice = minimumPrice; }
    public BigDecimal getMaximumPrice() { return maximumPrice; }
    public void setMaximumPrice(BigDecimal maximumPrice) { this.maximumPrice = maximumPrice; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }
    public LocalDate getAvailableTo() { return availableTo; }
    public void setAvailableTo(LocalDate availableTo) { this.availableTo = availableTo; }
    public ListingSort getSort() { return sort; }
    public void setSort(ListingSort sort) { this.sort = sort; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
}
