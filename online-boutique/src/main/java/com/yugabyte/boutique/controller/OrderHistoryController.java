package com.yugabyte.boutique.controller;

import com.yugabyte.boutique.model.Order;
import com.yugabyte.boutique.model.User;
import com.yugabyte.boutique.repository.OrderRepository;
import com.yugabyte.boutique.repository.UserRepository;
import com.yugabyte.boutique.service.CartService;
import com.yugabyte.boutique.service.CurrencyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class OrderHistoryController {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final CartService cartService;
    private final CurrencyService currencyService;

    public OrderHistoryController(OrderRepository orderRepo, UserRepository userRepo,
                                  CartService cartService, CurrencyService currencyService) {
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.cartService = cartService;
        this.currencyService = currencyService;
    }

    @GetMapping("/orders")
    public String orderHistory(@CookieValue(name = "shop_user", defaultValue = "") String userEmail,
                               @CookieValue(name = "shop_currency", defaultValue = "USD") String currency,
                               Model model) {

        if (userEmail.isBlank()) {
            return "redirect:/login";
        }

        Optional<User> user = userRepo.findByEmail(userEmail);
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        List<Order> orders = orderRepo.findByUserId(userEmail);

        // Get aggregate summary
        List<Object[]> summary = orderRepo.getOrderSummaryByUser(userEmail);
        if (!summary.isEmpty()) {
            Object[] s = summary.get(0);
            model.addAttribute("summary_name", s[0]);
            model.addAttribute("summary_order_count", s[1]);
            model.addAttribute("summary_total_spent", s[2]);
            model.addAttribute("summary_unique_products", s[3]);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("cart_size", cartService.cartSize(userEmail));
        model.addAttribute("currencies", currencyService.getSupportedCurrencies());
        model.addAttribute("user_currency", currency);
        model.addAttribute("show_currency", true);
        model.addAttribute("logged_in_user", user.get());
        SessionHelper.addCommon(model, currency);

        return "order-history";
    }
}
