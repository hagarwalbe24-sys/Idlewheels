package com.idlewheels.config;

import com.idlewheels.controller.AuthController;
import com.idlewheels.model.User;
import com.idlewheels.model.UserRole;
import com.idlewheels.service.AuthService;
import com.idlewheels.service.LoginAttemptLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

class CsrfInterceptorMockMvcTest {
    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, new LoginAttemptLimiter());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new CsrfInterceptor())
                .addFilters(new SecurityHeadersFilter())
                .build();
    }

    @Test
    void postWithoutTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "renter@example.com")
                        .param("password", "Pass1234"))
                .andExpect(status().isForbidden());
        verify(authService, never()).authenticate("renter@example.com", "Pass1234");
    }

    @Test
    void postWithMatchingTokenReachesController() throws Exception {
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(user.getRole()).thenReturn(UserRole.RENTER);
        when(authService.authenticate("renter@example.com", "Pass1234")).thenReturn(Optional.of(user));
        mockMvc.perform(post("/login")
                        .sessionAttr(CsrfInterceptor.SESSION_ATTRIBUTE, "session-token")
                        .param(CsrfInterceptor.PARAMETER_NAME, "session-token")
                        .param("email", "renter@example.com")
                        .param("password", "Pass1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listings"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "same-origin"));
        verify(authService).authenticate("renter@example.com", "Pass1234");
    }
}
