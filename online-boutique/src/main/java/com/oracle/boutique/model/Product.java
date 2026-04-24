package com.oracle.boutique.model;

import java.util.List;

public record Product(
        String id,
        String name,
        String description,
        String picture,
        String currencyCode,
        long priceUnits,
        int priceNanos,
        String categories
) {
    public Money priceUsd() {
        return new Money(currencyCode, priceUnits, priceNanos);
    }

    public List<String> categoryList() {
        if (categories == null || categories.isBlank()) return List.of();
        return List.of(categories.split(","));
    }
}
