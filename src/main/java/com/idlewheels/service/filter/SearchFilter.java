package com.idlewheels.service.filter;

import com.idlewheels.model.Listing;

public interface SearchFilter {
    boolean matches(Listing listing);
}
