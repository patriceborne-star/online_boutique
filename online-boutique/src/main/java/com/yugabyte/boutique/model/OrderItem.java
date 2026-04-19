package com.yugabyte.boutique.model;

public record OrderItem(
        String orderId,
        String productId,
        int quantity,
        long costUnits,
        int costNanos
) {
    public Money cost() {
        return new Money("USD", costUnits, costNanos);
    }
}
