package com.yugabyte.boutique.controller;

import com.yugabyte.boutique.model.User;
import com.yugabyte.boutique.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        List<User> users = userRepo.findAll();
        model.addAttribute("users", users);
        model.addAttribute("show_currency", false);
        SessionHelper.addCommon(model, "USD");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletResponse response,
                        Model model) {

        Optional<User> user = userRepo.authenticate(email, password);

        if (user.isEmpty()) {
            List<User> users = userRepo.findAll();
            model.addAttribute("users", users);
            model.addAttribute("error", "Invalid email or password.");
            model.addAttribute("show_currency", false);
            SessionHelper.addCommon(model, "USD");
            return "login";
        }

        // Set logged-in user cookie
        Cookie cookie = new Cookie("shop_user", email);
        cookie.setMaxAge(60 * 60 * 48); // 48 hours
        cookie.setPath("/");
        response.addCookie(cookie);

        // Also set session-id to user email so cart is tied to user
        Cookie sessionCookie = new Cookie("shop_session-id", email);
        sessionCookie.setMaxAge(60 * 60 * 48);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie userCookie = new Cookie("shop_user", "");
        userCookie.setMaxAge(0);
        userCookie.setPath("/");
        response.addCookie(userCookie);

        Cookie sessionCookie = new Cookie("shop_session-id", "");
        sessionCookie.setMaxAge(0);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);

        return "redirect:/";
    }
}
