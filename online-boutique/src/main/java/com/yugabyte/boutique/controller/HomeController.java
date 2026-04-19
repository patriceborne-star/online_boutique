package com.yugabyte.boutique.controller;

import com.yugabyte.boutique.model.Money;
import com.yugabyte.boutique.model.Product;
import com.yugabyte.boutique.model.User;
import com.yugabyte.boutique.repository.UserRepository;
import com.yugabyte.boutique.service.CartService;
import com.yugabyte.boutique.service.CurrencyService;
import com.yugabyte.boutique.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CartService cartService;
    private final CurrencyService currencyService;
    private final UserRepository userRepo;

    public HomeController(ProductService productService, CartService cartService,
                          CurrencyService currencyService, UserRepository userRepo) {
        this.productService = productService;
        this.cartService = cartService;
        this.currencyService = currencyService;
        this.userRepo = userRepo;
    }

    @GetMapping("/")
    public String home(Model model,
                       @CookieValue(name = "shop_currency", defaultValue = "USD") String currency,
                       @CookieValue(name = "shop_session-id", defaultValue = "") String sessionId,
                       @CookieValue(name = "shop_user", defaultValue = "") String userEmail,
                       HttpServletRequest request) {

        String userId = userEmail.isBlank()
                ? SessionHelper.getSessionId(sessionId, request)
                : userEmail;

        List<Product> products = productService.listProducts();

        List<ProductView> productViews = new ArrayList<>();
        for (Product p : products) {
            Money price = currencyService.convert(p.priceUsd(), currency);
            productViews.add(new ProductView(p, price));
        }

        model.addAttribute("products", productViews);
        model.addAttribute("cart_size", cartService.cartSize(userId));
        model.addAttribute("currencies", currencyService.getSupportedCurrencies());
        model.addAttribute("user_currency", currency);
        model.addAttribute("show_currency", true);
        addLoggedInUser(model, userEmail);
        SessionHelper.addCommon(model, currency);

        return "home";
    }

    private void addLoggedInUser(Model model, String userEmail) {
        if (!userEmail.isBlank()) {
            userRepo.findByEmail(userEmail).ifPresent(u -> model.addAttribute("logged_in_user", u));
        }
    }

    public record ProductView(Product item, Money price) {
        public String renderedPrice() {
            return price.render();
        }
    }
}
