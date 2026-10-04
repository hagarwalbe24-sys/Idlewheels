package com.idlewheels.dto;

import com.idlewheels.model.Listing;

public class ListingCardView {
    private final Listing listing;
    private final String photoUrl;

    public ListingCardView(Listing listing, String photoUrl) {
        this.listing = listing;
        this.photoUrl = photoUrl;
    }

    public Listing getListing() { return listing; }
    public String getPhotoUrl() { return photoUrl; }
}
