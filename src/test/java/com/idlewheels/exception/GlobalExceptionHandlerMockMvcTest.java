package com.idlewheels.exception;

import com.idlewheels.controller.ListingDetailsController;
import com.idlewheels.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class GlobalExceptionHandlerMockMvcTest {
    private ListingService listingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        listingService = mock(ListingService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ListingDetailsController(listingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void unknownListingReturnsNotFound() throws Exception {
        when(listingService.findActiveListing(445L))
                .thenThrow(new ResourceNotFoundException("This listing is unavailable."));

        mockMvc.perform(get("/listings/445"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    void nonNumericListingIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/listings/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error"));
    }
}
