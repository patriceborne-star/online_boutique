package com.oracle.boutique.model;

public record Money(String currencyCode, long units, int nanos) {

    public Money add(Money other) {
        long u = this.units + other.units;
        int n = this.nanos + other.nanos;
        if (n >= 1_000_000_000) {
            u++;
            n -= 1_000_000_000;
        }
        return new Money(this.currencyCode, u, n);
    }

    public Money multiply(int qty) {
        long totalNanos = (long) this.nanos * qty;
        long totalUnits = this.units * qty + totalNanos / 1_000_000_000;
        int remainNanos = (int) (totalNanos % 1_000_000_000);
        return new Money(this.currencyCode, totalUnits, remainNanos);
    }

    public String render() {
        String logo = switch (currencyCode) {
            case "USD", "CAD" -> "$";
            case "JPY" -> "\u00a5";
            case "EUR" -> "\u20ac";
            case "TRY" -> "\u20ba";
            case "GBP" -> "\u00a3";
            default -> "$";
        };
        return String.format("%s%d.%02d", logo, units, nanos / 10_000_000);
    }
}
