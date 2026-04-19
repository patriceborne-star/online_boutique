package com.yugabyte.boutique.controller;

import com.yugabyte.boutique.model.CartItem;
import com.yugabyte.boutique.model.Money;
import com.yugabyte.boutique.model.Product;
import com.yugabyte.boutique.model.User;
import com.yugabyte.boutique.repository.UserRepository;
import com.yugabyte.boutique.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final CurrencyService currencyService;
    private final ShippingService shippingService;
    private final RecommendationService recommendationService;
    private final UserRepository userRepo;

    public CartController(CartService cartService, ProductService productService,
                          CurrencyService currencyService, ShippingService shippingService,
                          RecommendationService recommendationService, UserRepository userRepo) {
        this.cartService = cartService;
        this.productService = productService;
        this.currencyService = currencyService;
        this.shippingService = shippingService;
        this.recommendationService = recommendationService;
        this.userRepo = userRepo;
    }

    @GetMapping("/cart")
    public String viewCart(Model model,
                           @CookieValue(name = "shop_currency", defaultValue = "USD") String currency,
                           @CookieValue(name = "shop_user", defaultValue = "") String userEmail) {

        if (userEmail.isBlank()) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartService.getCart(userEmail);

        List<CartItemView> items = new ArrayList<>();
        Money totalPrice = new Money(currency, 0, 0);

        for (CartItem ci : cartItems) {
            Product product = productService.getProduct(ci.productId()).orElse(null);
            if (product == null) continue;

            Money price = currencyService.convert(product.priceUsd(), currency);
            Money lineTotal = price.multiply(ci.quantity());
            items.add(new CartItemView(product, ci.quantity(), lineTotal));
            totalPrice = totalPrice.add(lineTotal);
        }

        Money shippingCost = currencyService.convert(
                shippingService.getQuote(cartItems), currency);
        totalPrice = totalPrice.add(shippingCost);

        List<String> cartProductIds = cartItems.stream()
                .map(CartItem::productId).toList();
        List<Product> recommendations = recommendationService.getRecommendations(cartProductIds);

        int year = Year.now().getValue();
        List<Integer> expirationYears = List.of(year, year + 1, year + 2, year + 3, year + 4);

        User loggedInUser = userRepo.findByEmail(userEmail).orElse(null);

        model.addAttribute("items", items);
        model.addAttribute("cart_size", cartService.cartSize(userEmail));
        model.addAttribute("shipping_cost", shippingCost);
        model.addAttribute("total_cost", totalPrice);
        model.addAttribute("expiration_years", expirationYears);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("currencies", currencyService.getSupportedCurrencies());
        model.addAttribute("user_currency", currency);
        model.addAttribute("show_currency", true);
        model.addAttribute("logged_in_user", loggedInUser);
        model.addAttribute("prefill_user", loggedInUser);
        SessionHelper.addCommon(model, currency);

        return "cart";
    }

    @PostMapping("/cart")
    public String addToCart(@RequestParam("product_id") String productId,
                            @RequestParam("quantity") int quantity,
                            @CookieValue(name = "shop_user", defaultValue = "") String userEmail) {

        if (userEmail.isBlank()) {
            return "redirect:/login";
        }
        cartService.addItem(userEmail, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/empty")
    public String emptyCart(@CookieValue(name = "shop_user", defaultValue = "") String userEmail) {
        if (userEmail.isBlank()) {
            return "redirect:/login";
        }
        cartService.emptyCart(userEmail);
        return "redirect:/";
    }

    public record CartItemView(Product item, int quantity, Money price) {
        public String renderedPrice() {
            return price.render();
        }
    }
}
