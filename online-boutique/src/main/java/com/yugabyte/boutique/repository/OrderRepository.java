package com.yugabyte.boutique.repository;

import com.yugabyte.boutique.model.Order;
import com.yugabyte.boutique.model.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<OrderItem> ITEM_MAPPER = (rs, rowNum) -> new OrderItem(
            rs.getString("order_id"),
            rs.getString("product_id"),
            rs.getInt("quantity"),
            rs.getLong("cost_units"),
            rs.getInt("cost_nanos")
    );

    private static final RowMapper<Order> ORDER_MAPPER = (rs, rowNum) -> {
        Timestamp shippedTs = rs.getTimestamp("shipped_at");
        Timestamp deliveredTs = rs.getTimestamp("delivered_at");
        return new Order(
                rs.getString("order_id"),
                rs.getString("user_id"),
                rs.getString("user_currency"),
                rs.getString("email"),
                rs.getString("street_address"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("zip_code"),
                rs.getString("country"),
                rs.getLong("shipping_cost_units"),
                rs.getInt("shipping_cost_nanos"),
                rs.getString("shipping_tracking_id"),
                rs.getLong("total_units"),
                rs.getInt("total_nanos"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toInstant(),
                shippedTs != null ? shippedTs.toInstant() : null,
                deliveredTs != null ? deliveredTs.toInstant() : null,
                null
        );
    };

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void createOrder(Order order) {
        jdbc.update("""
                INSERT INTO orders (order_id, user_id, user_currency, email,
                    street_address, city, state, zip_code, country,
                    shipping_cost_units, shipping_cost_nanos,
                    shipping_tracking_id, total_units, total_nanos, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')
                """,
                order.orderId(), order.userId(), order.userCurrency(), order.email(),
                order.streetAddress(), order.city(), order.state(), order.zipCode(),
                order.country(), order.shippingCostUnits(), order.shippingCostNanos(),
                order.shippingTrackingId(), order.totalUnits(), order.totalNanos());

        if (order.items() != null) {
            for (OrderItem item : order.items()) {
                jdbc.update("""
                        INSERT INTO order_items (order_id, product_id, quantity, cost_units, cost_nanos)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                        item.orderId(), item.productId(), item.quantity(),
                        item.costUnits(), item.costNanos());
            }
        }
    }

    /**
     * Order history with JOIN to get product names and item details.
     * Uses ORDER BY created_at DESC for most recent first.
     */
    public List<Order> findByUserId(String userId) {
        List<Order> orders = jdbc.query("""
                SELECT o.*
                FROM orders o
                WHERE o.user_id = ?
                ORDER BY o.created_at DESC
                """, ORDER_MAPPER, userId);

        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            List<OrderItem> items = jdbc.query("""
                    SELECT oi.order_id, oi.product_id, oi.quantity,
                           oi.cost_units, oi.cost_nanos
                    FROM order_items oi
                    JOIN products p ON oi.product_id = p.id
                    WHERE oi.order_id = ?
                    ORDER BY p.name
                    """, ITEM_MAPPER, o.orderId());
            orders.set(i, new Order(o.orderId(), o.userId(), o.userCurrency(),
                    o.email(), o.streetAddress(), o.city(), o.state(), o.zipCode(),
                    o.country(), o.shippingCostUnits(), o.shippingCostNanos(),
                    o.shippingTrackingId(), o.totalUnits(), o.totalNanos(),
                    o.status(), o.createdAt(), o.shippedAt(), o.deliveredAt(), items));
        }
        return orders;
    }

    /**
     * Rich order detail query: JOIN orders, order_items, products, and users.
     */
    public Order findByIdWithDetails(String orderId) {
        List<Order> orders = jdbc.query("""
                SELECT o.*
                FROM orders o
                JOIN users u ON o.user_id = u.email
                WHERE o.order_id = ?
                """, ORDER_MAPPER, orderId);

        if (orders.isEmpty()) return null;

        Order o = orders.get(0);
        List<OrderItem> items = jdbc.query("""
                SELECT oi.order_id, oi.product_id, oi.quantity,
                       oi.cost_units, oi.cost_nanos
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
                ORDER BY p.name
                """, ITEM_MAPPER, orderId);

        return new Order(o.orderId(), o.userId(), o.userCurrency(),
                o.email(), o.streetAddress(), o.city(), o.state(), o.zipCode(),
                o.country(), o.shippingCostUnits(), o.shippingCostNanos(),
                o.shippingTrackingId(), o.totalUnits(), o.totalNanos(),
                o.status(), o.createdAt(), o.shippedAt(), o.deliveredAt(), items);
    }

    /**
     * Update pending orders to shipped after 10 minutes.
     * Uses timestamp arithmetic and batch UPDATE with WHERE clause.
     */
    public int markShipped() {
        return jdbc.update("""
                UPDATE orders
                SET status = 'shipped', shipped_at = now()
                WHERE status = 'pending'
                  AND created_at < now() - INTERVAL '10 minutes'
                """);
    }

    /**
     * Update shipped orders to delivered after 10 more minutes.
     */
    public int markDelivered() {
        return jdbc.update("""
                UPDATE orders
                SET status = 'delivered', delivered_at = now()
                WHERE status = 'shipped'
                  AND shipped_at < now() - INTERVAL '10 minutes'
                """);
    }

    /**
     * Aggregate query: total spending per user with COUNT and SUM.
     */
    public List<Object[]> getOrderSummaryByUser(String userId) {
        return jdbc.query("""
                SELECT u.first_name || ' ' || u.last_name AS full_name,
                       COUNT(DISTINCT o.order_id) AS order_count,
                       SUM(o.total_units) AS total_spent_units,
                       COUNT(DISTINCT oi.product_id) AS unique_products
                FROM users u
                LEFT JOIN orders o ON u.email = o.user_id
                LEFT JOIN order_items oi ON o.order_id = oi.order_id
                WHERE u.email = ?
                GROUP BY u.first_name, u.last_name
                """,
                (rs, rowNum) -> new Object[]{
                        rs.getString("full_name"),
                        rs.getInt("order_count"),
                        rs.getLong("total_spent_units"),
                        rs.getInt("unique_products")
                }, userId);
    }
}
