package com.yugabyte.boutique.service;

import com.yugabyte.boutique.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusScheduler {

    private final OrderRepository orderRepo;

    public OrderStatusScheduler(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    /**
     * Every 60 seconds, check for orders that need status updates.
     * - pending  -> shipped   after 10 minutes
     * - shipped  -> delivered after 10 minutes (20 total from creation)
     */
    @Scheduled(fixedRate = 60000)
    public void updateOrderStatuses() {
        int shipped = orderRepo.markShipped();
        if (shipped > 0) {
            System.out.println("[OrderStatus] Marked " + shipped + " order(s) as SHIPPED");
        }

        int delivered = orderRepo.markDelivered();
        if (delivered > 0) {
            System.out.println("[OrderStatus] Marked " + delivered + " order(s) as DELIVERED");
        }
    }
}
