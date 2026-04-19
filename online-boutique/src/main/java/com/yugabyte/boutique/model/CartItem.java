package com.yugabyte.boutique.model;

public record CartItem(String userId, String productId, int quantity) {
}
