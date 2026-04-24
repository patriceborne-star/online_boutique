package com.oracle.boutique.repository;

import com.oracle.boutique.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("street_address"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("zip_code"),
            rs.getString("country"),
            rs.getString("phone"),
            rs.getString("credit_card_number"),
            rs.getInt("credit_card_exp_month"),
            rs.getInt("credit_card_exp_year"),
            rs.getString("credit_card_cvv")
    );

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<User> findAll() {
        return jdbc.query(
                "SELECT * FROM users ORDER BY last_name, first_name",
                ROW_MAPPER);
    }

    public Optional<User> findByEmail(String email) {
        List<User> results = jdbc.query(
                "SELECT * FROM users WHERE email = ?",
                ROW_MAPPER, email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<User> authenticate(String email, String password) {
        List<User> results = jdbc.query(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                ROW_MAPPER, email, password);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public void updateProfile(User user) {
        jdbc.update("""
                UPDATE users
                SET first_name = ?, last_name = ?, street_address = ?, city = ?,
                    state = ?, zip_code = ?, country = ?, phone = ?,
                    credit_card_number = ?, credit_card_exp_month = ?,
                    credit_card_exp_year = ?, credit_card_cvv = ?
                WHERE email = ?
                """,
                user.firstName(), user.lastName(), user.streetAddress(), user.city(),
                user.state(), user.zipCode(), user.country(), user.phone(),
                user.creditCardNumber(), user.creditCardExpMonth(),
                user.creditCardExpYear(), user.creditCardCvv(),
                user.email());
    }
}
