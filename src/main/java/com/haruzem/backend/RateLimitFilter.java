package com.haruzem.backend;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basit, kütüphanesiz bir rate limiting (istek sınırlama) filtresi.
 * Aynı IP adresinden belirli bir süre içinde çok fazla istek gelirse
 * 429 (Too Many Requests) hatası döner. Amaç, backend'in / Gemini API
 * kotasının kötüye kullanımını (spam, bot vb.) önlemek.
 */
@Component
public class RateLimitFilter implements Filter {

    // Bir IP'nin, aşağıdaki zaman penceresi içinde yapabileceği maksimum istek sayısı
    private static final int MAX_REQUESTS_PER_WINDOW = 15;
    private static final long WINDOW_MILLIS = 60_000; // 1 dakika

    private final ConcurrentHashMap<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String ip = getClientIp(request);
        RequestWindow window = requestCounts.computeIfAbsent(ip, k -> new RequestWindow());

        synchronized (window) {
            long now = System.currentTimeMillis();
            if (now - window.windowStart > WINDOW_MILLIS) {
                window.windowStart = now;
                window.count.set(0);
            }
            int currentCount = window.count.incrementAndGet();
            if (currentCount > MAX_REQUESTS_PER_WINDOW) {
                response.setStatus(429);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(
                        "Çok fazla istek gönderdiniz. Lütfen bir dakika bekleyip tekrar deneyin.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Railway gibi bir proxy arkasında çalışırken gerçek istemci IP'sini
     * X-Forwarded-For başlığından alır; yoksa doğrudan bağlantı adresini kullanır.
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private static class RequestWindow {
        volatile long windowStart = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);
    }
}