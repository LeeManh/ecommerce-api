package com.ecommerce.backend.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order")
public record OrderProperties(long pendingExpirationMinutes) {}
