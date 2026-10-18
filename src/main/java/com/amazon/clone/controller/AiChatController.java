package com.amazon.clone.controller;

import com.amazon.clone.model.Product;
import com.amazon.clone.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AiChatController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> handleAiChat(@RequestBody Map<String, String> request) {
        String userQuery = request.getOrDefault("message", "").trim().toLowerCase();
        List<Product> allProducts = productRepository.findAll();

        String aiResponse = processAnyQuery(userQuery, allProducts);

        Map<String, String> response = new HashMap<>();
        response.put("response", aiResponse);
        return ResponseEntity.ok(response);
    }

    private String processAnyQuery(String query, List<Product> products) {
        if (query.isEmpty()) {
            return "How can I help you today? Ask me about products, prices, or recommendations!";
        }

        // 1. Enhanced Greeting Matcher for Casual Hi/Hii/Hello/Hey
        if (query.matches(".*\\b(h+[i|e|a]+[l|o|y]*|namaste|vanakkam)\\b.*") || query.startsWith("hi") || query.startsWith("hey") || query.startsWith("hello")) {
            return "Hello! 👋 I am your Amazon AI Shopping Assistant. Ask me anything about our products, recommendations, or prices!";
        }

        if (query.contains("who are you") || query.contains("your name") || query.contains("what can you do")) {
            return "I am Amazon's AI Shopping Assistant! I can help you search for products, compare prices, find top deals, and give smart product recommendations.";
        }

        if (query.contains("shipping") || query.contains("delivery") || query.contains("charges")) {
            return "We offer FREE Prime delivery on most of our catalog items! Delivery usually takes 1-3 business days.";
        }

        if (query.contains("return") || query.contains("refund") || query.contains("replacement")) {
            return "We have a hassle-free 10-day return policy on all eligible items. You can manage returns directly from the 'Returns & Orders' section!";
        }

        // 2. Budget / Price-based Filter Queries (e.g. "under 50000", "below 2000", "cheap")
        if (query.contains("under") || query.contains("below") || query.contains("less than") || query.contains("budget")) {
            double maxPrice = extractPriceFromQuery(query);
            if (maxPrice > 0) {
                List<Product> budgetProducts = products.stream()
                        .filter(p -> p.getPrice() <= maxPrice)
                        .sorted(Comparator.comparing(Product::getPrice).reversed())
                        .collect(Collectors.toList());

                if (!budgetProducts.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Here are the best items under ₹" + (long)maxPrice + ":\n");
                    for (int i = 0; i < Math.min(3, budgetProducts.size()); i++) {
                        Product p = budgetProducts.get(i);
                        sb.append("• **").append(p.getName()).append("** - ₹").append(p.getPrice()).append("\n");
                    }
                    return sb.toString();
                } else {
                    return "Sorry, I couldn't find any products under ₹" + (long)maxPrice + ". Try increasing your budget limit!";
                }
            }
        }

        // 3. Dynamic Keyword Matching against Product Names, Categories, and Descriptions
        String[] keywords = query.split("\\s+");
        List<Product> matchedProducts = new ArrayList<>();

        for (Product p : products) {
            String fullText = (p.getName() + " " + p.getCategory() + " " + p.getDescription()).toLowerCase();
            boolean matchesAny = false;

            for (String kw : keywords) {
                if (kw.length() > 2 && fullText.contains(kw)) {
                    matchesAny = true;
                    break;
                }
            }

            if (matchesAny) {
                matchedProducts.add(p);
            }
        }

        // 4. Return Matched Product Summary
        if (!matchedProducts.isEmpty()) {
            StringBuilder sb = new StringBuilder("I found these relevant products for you:\n");
            for (int i = 0; i < Math.min(3, matchedProducts.size()); i++) {
                Product p = matchedProducts.get(i);
                sb.append("• **").append(p.getName()).append("** (₹").append(p.getPrice()).append(") - ").append(p.getCategory()).append("\n");
            }
            sb.append("\nYou can click on any product card on the homepage to view full details and buy!");
            return sb.toString();
        }

        // 5. Fallback Default Answer
        return "I couldn't find exact products matching '" + query + "' in our store catalog right now. Try searching for broader categories like *Electronics*, *Fashion*, *Books*, or *Home & Kitchen*!";
    }

    private double extractPriceFromQuery(String query) {
        try {
            String numbers = query.replaceAll("[^0-9]", " ").trim();
            String[] tokens = numbers.split("\\s+");
            for (String t : tokens) {
                if (!t.isEmpty()) {
                    return Double.parseDouble(t);
                }
            }
        } catch (Exception ignored) {}
        return 0.0;
    }
}