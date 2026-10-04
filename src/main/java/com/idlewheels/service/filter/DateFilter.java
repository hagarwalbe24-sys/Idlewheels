package com.idlewheels.service.filter;

import com.idlewheels.model.Listing;

import java.time.LocalDate;

public class DateFilter implements SearchFilter {
    private final LocalDate from;
    private final LocalDate to;

    public DateFilter(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean matches(Listing listing) {
        return !listing.getAvailableFrom().isAfter(from)
                && !listing.getAvailableTo().isBefore(to);
    }
}
