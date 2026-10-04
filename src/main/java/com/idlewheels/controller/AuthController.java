package com.idlewheels.controller;

import com.idlewheels.dto.RegistrationForm;
import com.idlewheels.model.User;
import com.idlewheels.service.AuthService;
import com.idlewheels.service.LoginAttemptLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
public class AuthController {
    private static final String GENERIC_LOGIN_ERROR = "Email or password is incorrect.";
    private final AuthService authService;
    private final LoginAttemptLimiter loginAttemptLimiter;

    public AuthController(AuthService authService, LoginAttemptLimiter loginAttemptLimiter) {
        this.authService = authService;
        this.loginAttemptLimiter = loginAttemptLimiter;
    }

    @GetMapping("/register")
    public String showRegistrationForm(HttpServletRequest request, Model model) {
        if (isLoggedIn(request)) return "redirect:/listings";
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            form.setPassword(null);
            return "register";
        }
        if (form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            form.setPassword(null);
            model.addAttribute("registrationError", "Password must use at most 72 UTF-8 bytes.");
            return "register";
        }
        try {
            authService.register(form);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException exception) {
            form.setPassword(null);
            model.addAttribute("registrationError", exception.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(HttpServletRequest request) {
        return isLoggedIn(request) ? "redirect:/listings" : "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpServletRequest request, Model model) {
        String ipAddress = request.getRemoteAddr();
        Optional<User> authenticatedUser = loginAttemptLimiter.isBlocked(email, ipAddress)
                ? Optional.empty() : authService.authenticate(email, password);
        if (authenticatedUser.isEmpty()) {
            loginAttemptLimiter.recordFailure(email, ipAddress);
            model.addAttribute("loginError", GENERIC_LOGIN_ERROR);
            return "login";
        }
        loginAttemptLimiter.recordSuccess(email, ipAddress);

        HttpSession oldSession = request.getSession(false);
        String requestedPath = oldSession == null ? null : (String) oldSession.getAttribute("requestedPath");
        if (!isSafeRedirectPath(requestedPath)) requestedPath = null;
        if (oldSession != null) oldSession.invalidate();

        User user = authenticatedUser.get();
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("userId", user.getId());
        newSession.setAttribute("userName", user.getName());
        newSession.setAttribute("userRole", user.getRole().name());
        return "redirect:" + (requestedPath == null ? "/listings" : requestedPath);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/";
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("userId") != null;
    }

    private boolean isSafeRedirectPath(String target) {
        return target != null && target.startsWith("/") && !target.startsWith("//")
                && !target.contains("\\") && !target.contains(":")
                && !target.contains("\r") && !target.contains("\n")
                && (target.startsWith("/owner/") || target.equals("/owner") || target.startsWith("/messages"));
    }
}
