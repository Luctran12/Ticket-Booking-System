package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.common.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> decreaseStockScript;

    @Value("${redis.key.ticket-stock}")
    private String ticketStockPrefix;

    @Value("${redis.key.order-status}")
    private String orderStatusPrefix;

    @Value("${redis.ttl.order-status}")
    private long orderStatusTtl;

    // ── STOCK MANAGEMENT ──────────────────────────────

    // Preheat: Admin gọi trước khi flash sale mở
    public void initStock(Long ticketId, int stock) {
        String key = ticketStockPrefix + ticketId;
        redisTemplate.opsForValue().set(key, stock);
        log.info("Preheated stock for ticket {}: {}", ticketId, stock);
    }

    // Core: Trừ stock atomic bằng Lua Script
    public Long decreaseStock(Long ticketId, int quantity) {
        String key = ticketStockPrefix + ticketId;
        Long result = redisTemplate.execute(
                decreaseStockScript,
                List.of(key),           // KEYS
                String.valueOf(quantity) // ARGV
        );
        return result;
    }

    // Lấy stock hiện tại (cho monitoring)
    public int getStock(Long ticketId) {
        String key = ticketStockPrefix + ticketId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.error("Invalid stock value in Redis for ticket {}: {}", ticketId, value);
            return 0;
        }
    }

    // Hoàn trả stock khi order FAILED (Dead Letter Queue xử lý)
    public void increaseStock(Long ticketId, int quantity) {
        String key = ticketStockPrefix + ticketId;
        redisTemplate.opsForValue().increment(key, quantity);
        log.info("Restored {} stock for ticket {}", quantity, ticketId);
    }

    // ── ORDER STATUS ──────────────────────────────────

    // Consumer gọi sau khi xử lý xong
    public void setOrderStatus(String orderId, OrderStatus status) {
        String key = orderStatusPrefix + orderId;
        redisTemplate.opsForValue().set(key, status.name(),
                Duration.ofSeconds(orderStatusTtl));
    }

    // Client polling gọi
    public OrderStatus getOrderStatus(String orderId) {
        String key = orderStatusPrefix + orderId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;
        return OrderStatus.valueOf(value.toString());
    }
}
