package com.fitmind.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
@Slf4j
public class RateLimitingFilter implements Filter {

    private final Map<String, ClientBucket> buckets = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 600;
    private static final long WINDOW_MS = 60_000;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest req) || !(response instanceof HttpServletResponse resp)) {
            chain.doFilter(request, response);
            return;
        }
        String path = req.getRequestURI();

        if (path.startsWith("/actuator/")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = req.getRemoteAddr();
        ClientBucket bucket = buckets.computeIfAbsent(ip, k -> new ClientBucket());
        if (!bucket.tryConsume()) {
            log.warn("Rate limit exceeded for IP: {}", ip);
            resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setStatus(429);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Please slow down.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private static class ClientBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MS) {
                synchronized (this) {
                    if (now - windowStart > WINDOW_MS) {
                        count.set(0);
                        windowStart = now;
                    }
                }
            }
            return count.incrementAndGet() <= MAX_REQUESTS;
        }
    }
}