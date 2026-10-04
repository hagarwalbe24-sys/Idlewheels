package com.idlewheels.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

@ControllerAdvice
public class CsrfModelAdvice {
    @ModelAttribute("csrfToken")
    public String csrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(CsrfInterceptor.SESSION_ATTRIBUTE);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(CsrfInterceptor.SESSION_ATTRIBUTE, token);
        }
        return token;
    }
}
