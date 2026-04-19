package com.yugabyte.boutique.repository;

import com.yugabyte.boutique.model.CartItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<CartItem> ROW_MAPPER = (rs, rowNum) -> new CartItem(
            rs.getString("user_id"),
            rs.getString("product_id"),
            rs.getInt("quantity")
    );

    public CartRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void addItem(String userId, String productId, int quantity) {
        jdbc.update("""
                INSERT INTO cart_items (user_id, product_id, quantity)
                VALUES (?, ?, ?)
                ON CONFLICT (user_id, product_id)
                DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity
                """, userId, productId, quantity);
    }

    public List<CartItem> getCart(String userId) {
        return jdbc.query(
                "SELECT * FROM cart_items WHERE user_id = ?",
                ROW_MAPPER, userId);
    }

    public void emptyCart(String userId) {
        jdbc.update("DELETE FROM cart_items WHERE user_id = ?", userId);
    }

    public int cartSize(String userId) {
        Integer size = jdbc.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE user_id = ?",
                Integer.class, userId);
        return size != null ? size : 0;
    }
}
