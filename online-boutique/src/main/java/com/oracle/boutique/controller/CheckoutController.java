package com.oracle.boutique.controller;

import com.oracle.boutique.model.Order;
import com.oracle.boutique.model.Product;
import com.oracle.boutique.repository.UserRepository;
import com.oracle.boutique.service.CheckoutService;
import com.oracle.boutique.service.CurrencyService;
import com.oracle.boutique.service.RecommendationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CurrencyService currencyService;
    private final RecommendationService recommendationService;
    private final UserRepository userRepo;

    public CheckoutController(CheckoutService checkoutService,
                              CurrencyService currencyService,
                              RecommendationService recommendationService,
                              UserRepository userRepo) {
        this.checkoutService = checkoutService;
        this.currencyService = currencyService;
        this.recommendationService = recommendationService;
        this.userRepo = userRepo;
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam String email,
                           @RequestParam("street_address") String streetAddress,
                           @RequestParam("zip_code") String zipCode,
                           @RequestParam String city,
                           @RequestParam String state,
                           @RequestParam String country,
                           @RequestParam("credit_card_number") String ccNumber,
                           @RequestParam("credit_card_expiration_month") int ccMonth,
                           @RequestParam("credit_card_expiration_year") int ccYear,
                           @RequestParam("credit_card_cvv") int ccCvv,
                           @CookieValue(name = "shop_currency", defaultValue = "USD") String currency,
                           @CookieValue(name = "shop_user", defaultValue = "") String userEmail,
                           Model model) {

        if (userEmail.isBlank()) {
            return "redirect:/login";
        }

        Order order = checkoutService.placeOrder(userEmail, currency, email,
                streetAddress, city, state, zipCode, country);

        List<Product> recommendations = recommendationService.getRecommendations(List.of());

        model.addAttribute("order", order);
        model.addAttribute("total_paid", order.totalPaid());
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("currencies", currencyService.getSupportedCurrencies());
        model.addAttribute("user_currency", currency);
        model.addAttribute("show_currency", false);
        userRepo.findByEmail(userEmail).ifPresent(u -> model.addAttribute("logged_in_user", u));
        SessionHelper.addCommon(model, currency);

        return "order";
    }
}
