package com.idlewheels.service.filter;

import com.idlewheels.model.Listing;

import java.math.BigDecimal;

public class PriceFilter implements SearchFilter {
    private final BigDecimal minimum;
    private final BigDecimal maximum;

    public PriceFilter(BigDecimal minimum, BigDecimal maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override
    public boolean matches(Listing listing) {
        BigDecimal price = listing.getPricePerDay();
        return (minimum == null || price.compareTo(minimum) >= 0)
                && (maximum == null || price.compareTo(maximum) <= 0);
    }
}
