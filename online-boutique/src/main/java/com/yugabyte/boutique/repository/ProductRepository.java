package com.yugabyte.boutique.repository;

import com.yugabyte.boutique.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Product> ROW_MAPPER = (rs, rowNum) -> new Product(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("picture"),
            rs.getString("currency_code"),
            rs.getLong("price_units"),
            rs.getInt("price_nanos"),
            rs.getString("categories")
    );

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Product> findAll() {
        return jdbc.query("SELECT * FROM products", ROW_MAPPER);
    }

    public Optional<Product> findById(String id) {
        List<Product> results = jdbc.query(
                "SELECT * FROM products WHERE id = ?", ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Product> findByIdNotIn(List<String> excludeIds, int limit) {
        if (excludeIds == null || excludeIds.isEmpty()) {
            return jdbc.query(
                    "SELECT * FROM products ORDER BY random() LIMIT ?",
                    ROW_MAPPER, limit);
        }
        String placeholders = String.join(",",
                excludeIds.stream().map(id -> "?").toList());
        Object[] params = new Object[excludeIds.size() + 1];
        for (int i = 0; i < excludeIds.size(); i++) {
            params[i] = excludeIds.get(i);
        }
        params[excludeIds.size()] = limit;
        return jdbc.query(
                "SELECT * FROM products WHERE id NOT IN (" + placeholders + ") ORDER BY random() LIMIT ?",
                ROW_MAPPER, params);
    }

    public List<Product> search(String query) {
        String pattern = "%" + query + "%";
        return jdbc.query(
                "SELECT * FROM products WHERE name ILIKE ? OR description ILIKE ?",
                ROW_MAPPER, pattern, pattern);
    }
}
