package com.amazon.clone.controller;

import com.amazon.clone.model.Review;
import com.amazon.clone.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReviewSummaryController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/review-summary/{productId}")
    public ResponseEntity<Map<String, Object>> getAiReviewSummary(@PathVariable Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        Map<String, Object> response = new HashMap<>();

        if (reviews.isEmpty()) {
            response.put("summary", "No reviews available yet for AI analysis.");
            response.put("pros", Arrays.asList("High build quality expected", "Authentic brand product"));
            response.put("cons", Arrays.asList("Awaiting customer feedback"));
            response.put("sentiment", "Neutral");
            return ResponseEntity.ok(response);
        }

        // Calculate Average Rating
        double avgRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(4.5);

        List<String> pros = new ArrayList<>();
        List<String> cons = new ArrayList<>();

        for (Review r : reviews) {
            String comment = r.getComment().toLowerCase();
            if (comment.contains("good") || comment.contains("great") || comment.contains("awesome") ||
                    comment.contains("quality") || comment.contains("fast") || comment.contains("love") || comment.contains("best")) {
                pros.add(r.getComment());
            } else if (comment.contains("bad") || comment.contains("slow") || comment.contains("heavy") ||
                    comment.contains("issue") || comment.contains("poor") || comment.contains("defect")) {
                cons.add(r.getComment());
            }
        }

        if (pros.isEmpty()) {
            pros.add("Customers appreciate the value for money.");
            pros.add("Durable packaging and fast delivery.");
        }
        if (cons.isEmpty()) {
            cons.add("Price could be slightly more competitive.");
        }

        response.put("summary", "Based on " + reviews.size() + " customer reviews, buyers generally rate this product " + String.format("%.1f", avgRating) + "/5 stars.");
        response.put("pros", pros.subList(0, Math.min(2, pros.size())));
        response.put("cons", cons.subList(0, Math.min(2, cons.size())));
        response.put("sentiment", avgRating >= 4.0 ? "Highly Positive" : "Mixed");

        return ResponseEntity.ok(response);
    }
}