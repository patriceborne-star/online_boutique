package com.oracle.boutique.service;

import com.oracle.boutique.model.Product;
import com.oracle.boutique.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    private final ProductRepository productRepo;

    public RecommendationService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public List<Product> getRecommendations(List<String> excludeProductIds) {
        return productRepo.findByIdNotIn(excludeProductIds, 4);
    }
}
