package com.oracle.boutique.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

import java.time.Year;
import java.util.UUID;

public final class SessionHelper {

    private SessionHelper() {
    }

    public static String getSessionId(String cookieValue, HttpServletRequest request) {
        if (cookieValue != null && !cookieValue.isBlank()) {
            return cookieValue;
        }
        String sessionId = UUID.randomUUID().toString();
        request.setAttribute("new_session_id", sessionId);
        return sessionId;
    }

    public static String getLoggedInUser(String shopUserCookie) {
        if (shopUserCookie != null && !shopUserCookie.isBlank()) {
            return shopUserCookie;
        }
        return null;
    }

    public static void addCommon(Model model, String currency) {
        model.addAttribute("currentYear", Year.now().getValue());
        model.addAttribute("platform_name", "yugabyteDB");
        model.addAttribute("platform_css", "local");
    }
}
