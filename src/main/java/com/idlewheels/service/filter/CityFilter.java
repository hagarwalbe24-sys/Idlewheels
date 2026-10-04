package com.idlewheels.service.filter;

import com.idlewheels.model.Listing;

public class CityFilter implements SearchFilter {
    private final String city;

    public CityFilter(String city) {
        this.city = city.trim().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public boolean matches(Listing listing) {
        return listing.getCity() != null
                && listing.getCity().toLowerCase(java.util.Locale.ROOT).contains(city);
    }
}
