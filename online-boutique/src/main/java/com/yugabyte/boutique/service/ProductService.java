package com.yugabyte.boutique.service;

import com.yugabyte.boutique.model.Product;
import com.yugabyte.boutique.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepo;

    public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public List<Product> listProducts() {
        return productRepo.findAll();
    }

    public Optional<Product> getProduct(String id) {
        return productRepo.findById(id);
    }

    public List<Product> search(String query) {
        return productRepo.search(query);
    }
}
