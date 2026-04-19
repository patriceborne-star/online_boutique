package com.yugabyte.boutique.controller;

import com.yugabyte.boutique.model.User;
import com.yugabyte.boutique.repository.UserRepository;
import com.yugabyte.boutique.service.CartService;
import com.yugabyte.boutique.service.CurrencyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ProfileController {

    private final UserRepository userRepo;
    private final CartService cartService;
    private final CurrencyService currencyService;

    public ProfileController(UserRepository userRepo, CartService cartService,
                             CurrencyService currencyService) {
        this.userRepo = userRepo;
        this.cartService = cartService;
        this.currencyService = currencyService;
    }

    @GetMapping("/profile")
    public String profilePage(@CookieValue(name = "shop_user", defaultValue = "") String userEmail,
                              @CookieValue(name = "shop_currency", defaultValue = "USD") String currency,
                              Model model) {

        if (userEmail.isBlank()) {
            return "redirect:/login";
        }

        Optional<User> user = userRepo.findByEmail(userEmail);
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("user", user.get());
        model.addAttribute("cart_size", cartService.cartSize(userEmail));
        model.addAttribute("currencies", currencyService.getSupportedCurrencies());
        model.addAttribute("user_currency", currency);
        model.addAttribute("show_currency", true);
        model.addAttribute("logged_in_user", user.get());
        SessionHelper.addCommon(model, currency);

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@CookieValue(name = "shop_user", defaultValue = "") String userEmail,
                                @RequestParam("first_name") String firstName,
                                @RequestParam("last_name") String lastName,
                                @RequestParam("street_address") String streetAddress,
                                @RequestParam String city,
                                @RequestParam String state,
                                @RequestParam("zip_code") String zipCode,
                                @RequestParam String country,
                                @RequestParam String phone,
                                @RequestParam("credit_card_number") String ccNumber,
                                @RequestParam("credit_card_exp_month") int ccMonth,
                                @RequestParam("credit_card_exp_year") int ccYear,
                                @RequestParam("credit_card_cvv") String ccCvv,
                                @CookieValue(name = "shop_currency", defaultValue = "USD") String currency,
                                Model model) {

        if (userEmail.isBlank()) {
            return "redirect:/login";
        }

        User updated = new User(userEmail, "password", firstName, lastName,
                streetAddress, city, state, zipCode, country, phone,
                ccNumber, ccMonth, ccYear, ccCvv);

        userRepo.updateProfile(updated);

        model.addAttribute("user", updated);
        model.addAttribute("success", "Profile updated successfully.");
        model.addAttribute("cart_size", cartService.cartSize(userEmail));
        model.addAttribute("currencies", currencyService.getSupportedCurrencies());
        model.addAttribute("user_currency", currency);
        model.addAttribute("show_currency", true);
        model.addAttribute("logged_in_user", updated);
        SessionHelper.addCommon(model, currency);

        return "profile";
    }
}
