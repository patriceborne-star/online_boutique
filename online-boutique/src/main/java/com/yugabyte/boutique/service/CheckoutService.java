package com.yugabyte.boutique.service;

import com.yugabyte.boutique.model.*;
import com.yugabyte.boutique.repository.CartRepository;
import com.yugabyte.boutique.repository.OrderRepository;
import com.yugabyte.boutique.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CheckoutService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final ShippingService shippingService;
    private final CurrencyService currencyService;

    public CheckoutService(CartRepository cartRepo, ProductRepository productRepo,
                           OrderRepository orderRepo, ShippingService shippingService,
                           CurrencyService currencyService) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.shippingService = shippingService;
        this.currencyService = currencyService;
    }

    @Transactional
    public Order placeOrder(String userId, String userCurrency, String email,
                            String streetAddress, String city, String state,
                            String zipCode, String country) {

        // 1. Get cart items
        List<CartItem> cartItems = cartRepo.getCart(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // 2. Calculate shipping
        Money shippingUsd = shippingService.getQuote(cartItems);
        Money shippingCost = currencyService.convert(shippingUsd, userCurrency);
        String trackingId = shippingService.generateTrackingId();

        // 3. Build order items and calculate total
        Money total = shippingCost;
        List<OrderItem> orderItems = new ArrayList<>();
        String orderId = UUID.randomUUID().toString();

        for (CartItem ci : cartItems) {
            Product product = productRepo.findById(ci.productId())
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + ci.productId()));

            Money itemPrice = currencyService.convert(product.priceUsd(), userCurrency);
            Money lineCost = itemPrice.multiply(ci.quantity());

            orderItems.add(new OrderItem(orderId, ci.productId(), ci.quantity(),
                    lineCost.units(), lineCost.nanos()));

            total = total.add(lineCost);
        }

        // 4. Create order
        Order order = new Order(orderId, userId, userCurrency, email,
                streetAddress, city, state, zipCode, country,
                shippingCost.units(), shippingCost.nanos(),
                trackingId, total.units(), total.nanos(),
                "pending", null, null, null, orderItems);

        orderRepo.createOrder(order);

        // 5. Empty cart
        cartRepo.emptyCart(userId);

        return order;
    }
}
