package com.oracle.boutique.model;

public record User(
        String email,
        String password,
        String firstName,
        String lastName,
        String streetAddress,
        String city,
        String state,
        String zipCode,
        String country,
        String phone,
        String creditCardNumber,
        int creditCardExpMonth,
        int creditCardExpYear,
        String creditCardCvv
) {
    public String fullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
