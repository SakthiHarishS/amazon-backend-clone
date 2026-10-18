package com.amazon.clone.controller;

import com.amazon.clone.dto.OrderRequestDTO;
import com.amazon.clone.model.Order;
import com.amazon.clone.service.OrderService;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Injected directly from application.properties
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @PostMapping("/create-razorpay-order")
    public ResponseEntity<?> createRazorpayOrder(@RequestBody Map<String, Object> data) {
        try {
            if (!data.containsKey("amount") || data.get("amount") == null) {
                return ResponseEntity.badRequest().body("Amount is required");
            }

            double amount = Double.parseDouble(data.get("amount").toString());

            // Minimum amount check for Razorpay (Minimum ₹1 = 100 Paise)
            if (amount < 1.0) {
                amount = 1.0;
            }

            long amountInPaise = Math.round(amount * 100);

            // Initialize Razorpay Client using properties
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());

            com.razorpay.Order rzpOrder = razorpay.orders.create(orderRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", rzpOrder.get("id"));
            response.put("currency", rzpOrder.get("currency"));
            response.put("amount", rzpOrder.get("amount"));
            response.put("keyId", razorpayKeyId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Razorpay Order Error: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequestDTO orderRequest) {
        return ResponseEntity.ok(orderService.createOrder(orderRequest));
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/user/{email}")
    public List<Order> getOrdersByUser(@PathVariable String email) {
        return orderService.getOrdersByUser(email);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        String newStatus = statusMap.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, newStatus));
    }
}