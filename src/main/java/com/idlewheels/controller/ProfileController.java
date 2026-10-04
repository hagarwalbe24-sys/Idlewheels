package com.idlewheels.controller;

import com.idlewheels.dto.ProfileForm;
import com.idlewheels.model.User;
import com.idlewheels.repository.UserRepository;
import com.idlewheels.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class ProfileController {
    private final UserRepository userRepository;
    private final AuthService authService;

    public ProfileController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping("/profile")
    public String show(@SessionAttribute("userId") Long userId, Model model) {
        User user = userRepository.findById(userId).orElseThrow();
        ProfileForm form = new ProfileForm();
        form.setName(user.getName());
        form.setPhone(user.getPhone());
        form.setCity(user.getCity());
        model.addAttribute("profileForm", form);
        model.addAttribute("profileUser", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String update(@SessionAttribute("userId") Long userId,
                         @Valid @ModelAttribute("profileForm") ProfileForm form,
                         BindingResult errors, Model model, HttpSession session) {
        if (form.getNewPassword() != null && !form.getNewPassword().isBlank()
                && (form.getCurrentPassword() == null || form.getCurrentPassword().isBlank())) {
            errors.rejectValue("currentPassword", "required", "Enter your current password to change it.");
        }
        if (errors.hasErrors()) {
            model.addAttribute("profileUser", userRepository.findById(userId).orElseThrow());
            form.setCurrentPassword(null);
            form.setNewPassword(null);
            return "profile";
        }
        try {
            User user = authService.updateProfile(userId, form);
            session.setAttribute("userName", user.getName());
            return "redirect:/profile?updated";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("profileUser", userRepository.findById(userId).orElseThrow());
            model.addAttribute("profileError", exception.getMessage());
            form.setCurrentPassword(null);
            form.setNewPassword(null);
            return "profile";
        }
    }
}
