package com.oracle.boutique.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class SessionFilter implements Filter {

    // Unique ID generated each time the application starts.
    // If the browser has a cookie from a previous run, we clear the login.
    private static final String APP_INSTANCE_ID = UUID.randomUUID().toString();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Check if the browser's app-instance cookie matches this run
        String browserInstanceId = null;
        String sessionId = null;
        String shopUser = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("shop_session-id".equals(c.getName())) {
                    sessionId = c.getValue();
                } else if ("shop_instance".equals(c.getName())) {
                    browserInstanceId = c.getValue();
                } else if ("shop_user".equals(c.getName())) {
                    shopUser = c.getValue();
                }
            }
        }

        // If the app instance doesn't match, this is a stale session from a previous run.
        // Clear the login cookie so the user starts fresh.
        if (!APP_INSTANCE_ID.equals(browserInstanceId)) {
            Cookie instanceCookie = new Cookie("shop_instance", APP_INSTANCE_ID);
            instanceCookie.setMaxAge(60 * 60 * 48);
            instanceCookie.setPath("/");
            response.addCookie(instanceCookie);

            if (shopUser != null && !shopUser.isBlank()) {
                Cookie clearUser = new Cookie("shop_user", "");
                clearUser.setMaxAge(0);
                clearUser.setPath("/");
                response.addCookie(clearUser);
            }
        }

        // Ensure session cookie exists
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("shop_session-id", sessionId);
            cookie.setMaxAge(60 * 60 * 48);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        chain.doFilter(request, response);
    }
}
