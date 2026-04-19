package com.yugabyte.boutique.controller;

import com.yugabyte.boutique.model.Money;
import com.yugabyte.boutique.model.Product;
import com.yugabyte.boutique.repository.UserRepository;
import com.yugabyte.boutique.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;
    private final CurrencyService currencyService;
    private final RecommendationService recommendationService;
    private final UserRepository userRepo;

    public ProductController(ProductService productService, CartService cartService,
                             CurrencyService currencyService,
                             RecommendationService recommendationService,
                             UserRepository userRepo) {
        this.productService = productService;
        this.cartService = cartService;
        this.currencyService = currencyService;
        this.recommendationService = recommendationService;
        this.userRepo = userRepo;
    }

    @GetMapping("/product/{id}")
    public String productPage(@PathVariable String id, Model model,
                              @CookieValue(name = "shop_currency", defaultValue = "USD") String currency,
                              @CookieValue(name = "shop_session-id", defaultValue = "") String sessionId,
                              @CookieValue(name = "shop_user", defaultValue = "") String userEmail,
                              HttpServletRequest request) {

        String userId = userEmail.isBlank()
                ? SessionHelper.getSessionId(sessionId, request)
                : userEmail;

        Product product = productService.getProduct(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        Money price = currencyService.convert(product.priceUsd(), currency);

        List<Product> recommendations = recommendationService.getRecommendations(List.of(id));

        model.addAttribute("product", new HomeController.ProductView(product, price));
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("cart_size", cartService.cartSize(userId));
        model.addAttribute("currencies", currencyService.getSupportedCurrencies());
        model.addAttribute("user_currency", currency);
        model.addAttribute("show_currency", true);
        if (!userEmail.isBlank()) {
            userRepo.findByEmail(userEmail).ifPresent(u -> model.addAttribute("logged_in_user", u));
        }
        SessionHelper.addCommon(model, currency);

        return "product";
    }
}
