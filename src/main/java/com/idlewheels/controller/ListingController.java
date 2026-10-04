package com.idlewheels.controller;

import com.idlewheels.dto.ListingForm;
import com.idlewheels.model.Listing;
import com.idlewheels.service.ListingService;
import com.idlewheels.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class ListingController {
    private final ListingService listingService;
    private final VehicleService vehicleService;

    public ListingController(ListingService listingService, VehicleService vehicleService) {
        this.listingService = listingService;
        this.vehicleService = vehicleService;
    }

    @GetMapping({"/owner/listings", "/owner/listings/new"})
    public String showListings(
            @SessionAttribute("userId") Long ownerId,
            @RequestParam(defaultValue = "false") boolean added,
            @RequestParam(defaultValue = "false") boolean updated,
            @RequestParam(defaultValue = "false") boolean deactivated,
            @RequestParam(defaultValue = "false") boolean activated,
            Model model
    ) {
        addPageData(ownerId, model);
        model.addAttribute("listingForm", new ListingForm());
        model.addAttribute("listingAdded", added);
        model.addAttribute("listingUpdated", updated);
        model.addAttribute("listingDeactivated", deactivated);
        model.addAttribute("listingActivated", activated);
        model.addAttribute("formAction", "/owner/listings");
        model.addAttribute("editMode", false);
        return "owner/listings";
    }

    @PostMapping("/owner/listings")
    public String createListing(
            @SessionAttribute("userId") Long ownerId,
            @Valid @ModelAttribute("listingForm") ListingForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addPageData(ownerId, model);
            model.addAttribute("formAction", "/owner/listings");
            model.addAttribute("editMode", false);
            return "owner/listings";
        }
        try {
            listingService.createListing(ownerId, form);
            redirectAttributes.addAttribute("added", true);
            return "redirect:/owner/listings";
        } catch (IllegalArgumentException exception) {
            addPageData(ownerId, model);
            model.addAttribute("formAction", "/owner/listings");
            model.addAttribute("editMode", false);
            model.addAttribute("listingError", exception.getMessage());
            return "owner/listings";
        }
    }

    @GetMapping("/owner/listings/{listingId}/edit")
    public String editListingForm(
            @SessionAttribute("userId") Long ownerId,
            @PathVariable Long listingId,
            Model model
    ) {
        Listing listing = listingService.findOwnedListing(ownerId, listingId);
        ListingForm form = new ListingForm();
        form.setVehicleId(listing.getVehicle().getId());
        form.setTitle(listing.getTitle());
        form.setDescription(listing.getDescription());
        form.setCity(listing.getCity());
        form.setPricePerDay(listing.getPricePerDay());
        form.setAvailableFrom(listing.getAvailableFrom());
        form.setAvailableTo(listing.getAvailableTo());
        addPageData(ownerId, model);
        model.addAttribute("listingForm", form);
        model.addAttribute("formAction", "/owner/listings/" + listingId + "/edit");
        model.addAttribute("editMode", true);
        model.addAttribute("editingListing", listing);
        model.addAttribute("editingPhotoUrl", listingService.getPhotoUrl(listing.getPhotoKey()));
        return "owner/listings";
    }

    @PostMapping("/owner/listings/{listingId}/edit")
    public String updateListing(
            @SessionAttribute("userId") Long ownerId,
            @PathVariable Long listingId,
            @Valid @ModelAttribute("listingForm") ListingForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addPageData(ownerId, model);
            model.addAttribute("formAction", "/owner/listings/" + listingId + "/edit");
            model.addAttribute("editMode", true);
            model.addAttribute("editingListing", listingService.findOwnedListing(ownerId, listingId));
            model.addAttribute("editingPhotoUrl", listingService.getPhotoUrl(
                    ((Listing) model.getAttribute("editingListing")).getPhotoKey()));
            return "owner/listings";
        }
        try {
            listingService.updateListing(ownerId, listingId, form);
            redirectAttributes.addAttribute("updated", true);
            return "redirect:/owner/listings";
        } catch (IllegalArgumentException exception) {
            addPageData(ownerId, model);
            model.addAttribute("formAction", "/owner/listings/" + listingId + "/edit");
            model.addAttribute("editMode", true);
            model.addAttribute("editingListing", listingService.findOwnedListing(ownerId, listingId));
            model.addAttribute("editingPhotoUrl", listingService.getPhotoUrl(
                    ((Listing) model.getAttribute("editingListing")).getPhotoKey()));
            model.addAttribute("listingError", exception.getMessage());
            return "owner/listings";
        }
    }

    @PostMapping("/owner/listings/{listingId}/deactivate")
    public String deactivateListing(
            @SessionAttribute("userId") Long ownerId,
            @PathVariable Long listingId,
            RedirectAttributes redirectAttributes
    ) {
        listingService.deactivateListing(ownerId, listingId);
        redirectAttributes.addAttribute("deactivated", true);
        return "redirect:/owner/listings";
    }

    @PostMapping("/owner/listings/{listingId}/activate")
    public String activateListing(
            @SessionAttribute("userId") Long ownerId,
            @PathVariable Long listingId,
            RedirectAttributes redirectAttributes
    ) {
        listingService.activateListing(ownerId, listingId);
        redirectAttributes.addAttribute("activated", true);
        return "redirect:/owner/listings";
    }

    private void addPageData(Long ownerId, Model model) {
        var listings = listingService.findOwnerListings(ownerId);
        model.addAttribute("vehicles", vehicleService.findOwnerVehicles(ownerId));
        model.addAttribute("listings", listings);
        model.addAttribute("activeCount", listingService.countActiveOwnerListings(ownerId));
        model.addAttribute("inactiveCount", listingService.countInactiveOwnerListings(ownerId));
        model.addAttribute("expiredCount", listingService.countExpiredOwnerListings(ownerId));
        Map<Long, String> photoUrls = new HashMap<>();
        Set<Long> expiredListingIds = new HashSet<>();
        for (Listing listing : listings) {
            photoUrls.put(listing.getId(), listingService.getPhotoUrl(listing.getPhotoKey()));
            if (listing.getAvailableTo().isBefore(LocalDate.now())) expiredListingIds.add(listing.getId());
        }
        model.addAttribute("photoUrls", photoUrls);
        model.addAttribute("expiredListingIds", expiredListingIds);
    }
}
