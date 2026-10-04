package com.idlewheels.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            HttpSession loginSession = request.getSession(true);
            String target = request.getRequestURI().substring(request.getContextPath().length());
            if (request.getQueryString() != null) {
                target += "?" + request.getQueryString();
            }
            if (isSafeProtectedPath(target)) {
                loginSession.setAttribute("requestedPath", target);
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String requestPath = request.getRequestURI().substring(request.getContextPath().length());

        if (requestPath.startsWith("/owner")
                && !"OWNER".equals(session.getAttribute("userRole"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    private boolean isSafeProtectedPath(String target) {
        return target != null && target.startsWith("/") && !target.startsWith("//")
                && !target.contains("\\") && !target.contains(":")
                && !target.contains("\r") && !target.contains("\n")
                && (target.startsWith("/owner/") || target.equals("/owner") || target.startsWith("/messages"));
    }
}
