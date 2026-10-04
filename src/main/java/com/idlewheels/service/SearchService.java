package com.idlewheels.service;

import com.idlewheels.dto.ListingCardView;
import com.idlewheels.dto.ListingSort;
import com.idlewheels.dto.SearchForm;
import com.idlewheels.dto.SearchPage;
import com.idlewheels.model.Listing;
import com.idlewheels.model.ListingStatus;
import com.idlewheels.repository.ListingRepository;
import com.idlewheels.service.filter.CityFilter;
import com.idlewheels.service.filter.DateFilter;
import com.idlewheels.service.filter.PriceFilter;
import com.idlewheels.service.filter.SearchFilter;
import com.idlewheels.service.filter.VehicleTypeFilter;
import com.idlewheels.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SearchService {
    public static final int PAGE_SIZE = 9;
    private final ListingRepository listingRepository;
    private final StorageService storageService;

    public SearchService(ListingRepository listingRepository, StorageService storageService) {
        this.listingRepository = listingRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public SearchPage search(SearchForm form) {
        SearchForm criteria = form == null ? new SearchForm() : form;
        validate(criteria);
        List<SearchFilter> filters = new ArrayList<>();
        if (criteria.getCity() != null && !criteria.getCity().isBlank()) filters.add(new CityFilter(criteria.getCity()));
        if (criteria.getMinimumPrice() != null || criteria.getMaximumPrice() != null) {
            filters.add(new PriceFilter(criteria.getMinimumPrice(), criteria.getMaximumPrice()));
        }
        if (criteria.getVehicleType() != null) filters.add(new VehicleTypeFilter(criteria.getVehicleType()));
        if (criteria.getAvailableFrom() != null) filters.add(new DateFilter(criteria.getAvailableFrom(), criteria.getAvailableTo()));

        // This strategy filter is intentionally in-memory and is appropriate for project scale.
        List<Listing> matching = listingRepository.findByStatusAndAvailableToGreaterThanEqualOrderByCreatedAtDesc(
                        ListingStatus.ACTIVE, LocalDate.now())
                .stream()
                .filter(listing -> filters.stream().allMatch(filter -> filter.matches(listing)))
                .sorted(comparator(criteria.getSort() == null ? ListingSort.NEWEST : criteria.getSort()))
                .toList();

        long count = matching.size();
        int totalPages = (int) Math.ceil(count / (double) PAGE_SIZE);
        int page = totalPages == 0 ? 0 : Math.min(criteria.getPage(), totalPages - 1);
        criteria.setPage(page);
        int fromIndex = Math.min(page * PAGE_SIZE, matching.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, matching.size());
        List<ListingCardView> cards = matching.subList(fromIndex, toIndex).stream()
                .map(listing -> new ListingCardView(listing, storageService.getUrl(listing.getPhotoKey())))
                .toList();
        return new SearchPage(cards, count, page, totalPages);
    }

    public SearchPage emptyPage(int page) {
        return new SearchPage(List.of(), 0, Math.max(0, page), 0);
    }

    private void validate(SearchForm form) {
        if (form.getMinimumPrice() != null && form.getMaximumPrice() != null
                && form.getMinimumPrice().compareTo(form.getMaximumPrice()) > 0) {
            throw new IllegalArgumentException("Minimum price cannot exceed maximum price.");
        }
        if ((form.getAvailableFrom() == null) != (form.getAvailableTo() == null)) {
            throw new IllegalArgumentException("Choose both dates, or leave both empty.");
        }
        if (form.getAvailableFrom() != null && form.getAvailableTo().isBefore(form.getAvailableFrom())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        if (form.getPage() < 0) throw new IllegalArgumentException("Page number must be zero or greater.");
    }

    private Comparator<Listing> comparator(ListingSort sort) {
        Comparator<Listing> newest = Comparator
                .comparing(Listing::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Listing::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        return switch (sort) {
            case PRICE_LOW_TO_HIGH -> Comparator.comparing(Listing::getPricePerDay)
                    .thenComparing(newest);
            case PRICE_HIGH_TO_LOW -> Comparator.comparing(Listing::getPricePerDay, Comparator.reverseOrder())
                    .thenComparing(newest);
            case NEWEST -> newest;
        };
    }
}
