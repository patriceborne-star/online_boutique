package com.yugabyte.boutique.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CurrencyController {

    @PostMapping("/setCurrency")
    public String setCurrency(@RequestParam("currency_code") String currencyCode,
                              @RequestHeader(value = "referer", defaultValue = "/") String referer,
                              HttpServletResponse response) {
        Cookie cookie = new Cookie("shop_currency", currencyCode);
        cookie.setMaxAge(60 * 60 * 48);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:" + referer;
    }
}
