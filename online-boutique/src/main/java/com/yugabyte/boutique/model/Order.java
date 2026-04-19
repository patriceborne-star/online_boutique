package com.yugabyte.boutique.model;

import java.time.Instant;
import java.util.List;

public record Order(
        String orderId,
        String userId,
        String userCurrency,
        String email,
        String streetAddress,
        String city,
        String state,
        String zipCode,
        String country,
        long shippingCostUnits,
        int shippingCostNanos,
        String shippingTrackingId,
        long totalUnits,
        int totalNanos,
        String status,
        Instant createdAt,
        Instant shippedAt,
        Instant deliveredAt,
        List<OrderItem> items
) {
    public Money shippingCost() {
        return new Money(userCurrency, shippingCostUnits, shippingCostNanos);
    }

    public Money totalPaid() {
        return new Money(userCurrency, totalUnits, totalNanos);
    }

    public String statusLabel() {
        return switch (status != null ? status : "pending") {
            case "pending" -> "Processing";
            case "shipped" -> "Shipped";
            case "delivered" -> "Delivered";
            default -> status;
        };
    }
}
