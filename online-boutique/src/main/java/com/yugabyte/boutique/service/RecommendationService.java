package com.yugabyte.boutique.service;

import com.yugabyte.boutique.model.Product;
import com.yugabyte.boutique.repository.ProductRepository;
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
