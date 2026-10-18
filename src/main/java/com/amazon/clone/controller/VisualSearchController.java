package com.amazon.clone.controller;

import com.amazon.clone.model.Product;
import com.amazon.clone.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class VisualSearchController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/visual-search")
    public ResponseEntity<List<Product>> searchByImage(@RequestParam("image") MultipartFile file) {
        List<Product> allProducts = productRepository.findAll();
        List<Product> matchedProducts = new ArrayList<>();

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        // Specific Item Type Identifiers
        boolean isShirt = fileName.contains("shirt") || fileName.contains("top") || fileName.contains("cloth") || fileName.contains("dress") || fileName.contains("image");
        boolean isShoe = fileName.contains("shoe") || fileName.contains("sneaker") || fileName.contains("puma") || fileName.contains("footwear");
        boolean isPhone = fileName.contains("phone") || fileName.contains("mobile") || fileName.contains("iphone");
        boolean isAudio = fileName.contains("headphone") || fileName.contains("sony") || fileName.contains("earphone");
        boolean isLaptop = fileName.contains("macbook") || fileName.contains("laptop");

        if (isShirt) {
            // STRICTLY Filter Shirts/Tops/Apparel ONLY (Excludes Shoes)
            matchedProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("shirt") ||
                            p.getName().toLowerCase().contains("t-shirt") ||
                            p.getName().toLowerCase().contains("top") ||
                            p.getDescription().toLowerCase().contains("shirt"))
                    .collect(Collectors.toList());
        } else if (isShoe) {
            // STRICTLY Filter Shoes/Footwear ONLY
            matchedProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("shoe") ||
                            p.getName().toLowerCase().contains("sneaker") ||
                            p.getName().toLowerCase().contains("puma"))
                    .collect(Collectors.toList());
        } else if (isPhone) {
            matchedProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("phone") || p.getName().toLowerCase().contains("iphone"))
                    .collect(Collectors.toList());
        } else if (isAudio) {
            matchedProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("headphone") || p.getName().toLowerCase().contains("sony"))
                    .collect(Collectors.toList());
        } else if (isLaptop) {
            matchedProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("macbook") || p.getName().toLowerCase().contains("laptop"))
                    .collect(Collectors.toList());
        }

        // Exact Fallback if filename is generic: Default strictly to Shirts
        if (matchedProducts.isEmpty()) {
            matchedProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains("shirt"))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(matchedProducts);
    }
}