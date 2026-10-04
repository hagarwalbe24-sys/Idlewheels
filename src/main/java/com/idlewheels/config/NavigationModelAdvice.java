package com.idlewheels.config;

import com.idlewheels.repository.MessageRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavigationModelAdvice {
    private final MessageRepository messageRepository;

    public NavigationModelAdvice(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @ModelAttribute("unreadMessageCount")
    public long unreadMessageCount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long userId)) return 0;
        return messageRepository.countUnreadForUser(userId);
    }
}
