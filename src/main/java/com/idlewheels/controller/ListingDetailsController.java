package com.idlewheels.controller;

import com.idlewheels.dto.MessageForm;
import com.idlewheels.model.Listing;
import com.idlewheels.service.ListingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;
import java.time.LocalDate;

@Controller
public class ListingDetailsController {
    private final ListingService listingService;

    public ListingDetailsController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/listings/{id}")
    public String showListing(@PathVariable Long id,
                              @SessionAttribute(value = "userId", required = false) Long userId,
                              Model model) {
        Listing listing;
        boolean ownerPreview = false;
        if (userId != null) {
            try {
                Listing ownerListing = listingService.findOwnedListing(userId, id);
                listing = ownerListing;
                ownerPreview = listing.getStatus() != com.idlewheels.model.ListingStatus.ACTIVE
                        || listing.getAvailableTo().isBefore(LocalDate.now());
            } catch (com.idlewheels.exception.ResourceNotFoundException ignored) {
                listing = listingService.findActiveListing(id);
            }
        } else {
            listing = listingService.findActiveListing(id);
        }
        model.addAttribute("listing", listing);
        model.addAttribute("photoUrl", listingService.getPhotoUrl(listing.getPhotoKey()));
        model.addAttribute("ownerPreview", ownerPreview);
        model.addAttribute("messageForm", new MessageForm());
        return "listing-details";
    }
}
