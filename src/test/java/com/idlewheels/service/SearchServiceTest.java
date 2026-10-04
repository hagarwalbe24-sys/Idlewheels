package com.idlewheels.service;

import com.idlewheels.dto.SearchForm;
import com.idlewheels.dto.SearchPage;
import com.idlewheels.model.Listing;
import com.idlewheels.model.ListingStatus;
import com.idlewheels.repository.ListingRepository;
import com.idlewheels.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
    @Mock private ListingRepository listingRepository;
    @Mock private StorageService storageService;
    @InjectMocks private SearchService searchService;

    @Test
    void searchAppliesCityContainsFilterToActiveNonExpiredListings() {
        Listing delhi = mock(Listing.class);
        Listing newDelhi = mock(Listing.class);
        Listing mumbai = mock(Listing.class);
        when(delhi.getCity()).thenReturn("Delhi");
        when(newDelhi.getCity()).thenReturn("New Delhi NCR");
        when(mumbai.getCity()).thenReturn("Mumbai");
        when(listingRepository.findByStatusAndAvailableToGreaterThanEqualOrderByCreatedAtDesc(
                ListingStatus.ACTIVE, LocalDate.now())).thenReturn(List.of(delhi, newDelhi, mumbai));

        SearchForm form = new SearchForm();
        form.setCity("delhi");

        SearchPage page = searchService.search(form);
        assertEquals(2, page.getTotalResults());
        assertEquals(List.of(delhi, newDelhi), page.getItems().stream().map(card -> card.getListing()).toList());
    }

    @Test
    void paginatesNineCardsAtATimeAndKeepsTheTotalCount() {
        List<Listing> listings = new ArrayList<>();
        for (int i = 0; i < 10; i++) listings.add(mock(Listing.class));
        when(listingRepository.findByStatusAndAvailableToGreaterThanEqualOrderByCreatedAtDesc(
                ListingStatus.ACTIVE, LocalDate.now())).thenReturn(listings);
        SearchForm form = new SearchForm();
        form.setPage(1);

        SearchPage page = searchService.search(form);
        assertEquals(10, page.getTotalResults());
        assertEquals(2, page.getTotalPages());
        assertEquals(1, page.getItems().size());
        assertEquals(1, page.getPage());
    }
}
