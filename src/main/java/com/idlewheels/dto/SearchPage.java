package com.idlewheels.dto;

import java.util.List;

public class SearchPage {
    private final List<ListingCardView> items;
    private final long totalResults;
    private final int page;
    private final int totalPages;

    public SearchPage(List<ListingCardView> items, long totalResults, int page, int totalPages) {
        this.items = List.copyOf(items);
        this.totalResults = totalResults;
        this.page = page;
        this.totalPages = totalPages;
    }

    public List<ListingCardView> getItems() { return items; }
    public long getTotalResults() { return totalResults; }
    public int getPage() { return page; }
    public int getTotalPages() { return totalPages; }
    public boolean isHasPrevious() { return page > 0; }
    public boolean isHasNext() { return page + 1 < totalPages; }
}
