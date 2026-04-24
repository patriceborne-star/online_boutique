package com.oracle.boutique.service;

import com.oracle.boutique.model.CartItem;
import com.oracle.boutique.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepo;

    public CartService(CartRepository cartRepo) {
        this.cartRepo = cartRepo;
    }

    public void addItem(String userId, String productId, int quantity) {
        cartRepo.addItem(userId, productId, quantity);
    }

    public List<CartItem> getCart(String userId) {
        return cartRepo.getCart(userId);
    }

    public void emptyCart(String userId) {
        cartRepo.emptyCart(userId);
    }

    public int cartSize(String userId) {
        return cartRepo.cartSize(userId);
    }
}
