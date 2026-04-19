package com.yugabyte.boutique.service;

import com.yugabyte.boutique.model.CartItem;
import com.yugabyte.boutique.model.Money;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShippingService {

    // Mimics the original shipping service quote logic:
    // base cost $8.99, plus $1.99 per unique item in the cart
    public Money getQuote(List<CartItem> items) {
        int count = 0;
        for (CartItem item : items) {
            count += item.quantity();
        }
        // $8.99 base + $1.99 per item
        double cents = 899 + (count * 199);
        long units = (long) (cents / 100);
        int nanos = (int) ((cents % 100) * 10_000_000);
        return new Money("USD", units, nanos);
    }

    public String generateTrackingId() {
        StringBuilder sb = new StringBuilder("YB-");
        for (int i = 0; i < 5; i++) {
            sb.append((int) (Math.random() * 10));
        }
        sb.append('-');
        for (int i = 0; i < 5; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}
