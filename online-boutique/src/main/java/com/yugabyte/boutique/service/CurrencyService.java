package com.yugabyte.boutique.service;

import com.yugabyte.boutique.model.Money;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {

    // Exchange rates relative to EUR (from the original Online Boutique)
    private static final Map<String, Double> RATES = new LinkedHashMap<>();
    static {
        RATES.put("EUR", 1.0);
        RATES.put("USD", 1.1305);
        RATES.put("JPY", 126.40);
        RATES.put("BGN", 1.9558);
        RATES.put("CZK", 25.592);
        RATES.put("DKK", 7.4609);
        RATES.put("GBP", 0.85970);
        RATES.put("HUF", 315.51);
        RATES.put("PLN", 4.2996);
        RATES.put("RON", 4.7463);
        RATES.put("SEK", 10.5375);
        RATES.put("CHF", 1.1360);
        RATES.put("ISK", 136.80);
        RATES.put("NOK", 9.8040);
        RATES.put("HRK", 7.4210);
        RATES.put("TRY", 6.1247);
        RATES.put("AUD", 1.6072);
        RATES.put("BRL", 4.2682);
        RATES.put("CAD", 1.5128);
        RATES.put("CNY", 7.5857);
        RATES.put("HKD", 8.8743);
        RATES.put("IDR", 15999.40);
        RATES.put("ILS", 4.0875);
        RATES.put("INR", 79.4320);
        RATES.put("KRW", 1275.05);
        RATES.put("MXN", 21.7999);
        RATES.put("MYR", 4.6289);
        RATES.put("NZD", 1.6679);
        RATES.put("PHP", 59.083);
        RATES.put("SGD", 1.5349);
        RATES.put("THB", 36.012);
        RATES.put("ZAR", 16.0583);
    }

    // Whitelisted currencies shown in the UI dropdown
    private static final List<String> WHITELISTED = List.of(
            "USD", "EUR", "CAD", "JPY", "GBP", "TRY"
    );

    public List<String> getSupportedCurrencies() {
        return WHITELISTED;
    }

    public Money convert(Money from, String toCode) {
        if (from.currencyCode().equals(toCode)) {
            return from;
        }

        Double fromRate = RATES.get(from.currencyCode());
        Double toRate = RATES.get(toCode);
        if (fromRate == null || toRate == null) {
            return from; // unknown currency, return as-is
        }

        // Convert: amount_in_EUR = amount / fromRate, then amount_in_target = EUR * toRate
        double totalCents = (from.units() * 100.0 + from.nanos() / 10_000_000.0);
        double converted = totalCents / fromRate * toRate;

        long units = (long) (converted / 100);
        int nanos = (int) Math.round((converted % 100) * 10_000_000);
        if (nanos >= 1_000_000_000) {
            units++;
            nanos -= 1_000_000_000;
        }

        return new Money(toCode, units, nanos);
    }
}
