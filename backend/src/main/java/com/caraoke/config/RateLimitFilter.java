package com.caraoke.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Slows bots down: per signed-in user (else per IP), a budget of reads and a smaller budget of
 * writes per minute. Generous for a person, tight for a script. Over budget -> 429.
 * Added to the security chain after authentication (see SecurityConfig), not as a servlet filter.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> WRITES = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final RateLimiter limiter;
    private final int readsPerMinute;
    private final int writesPerMinute;

    public RateLimitFilter(RateLimiter limiter, int readsPerMinute, int writesPerMinute) {
        this.limiter = limiter;
        this.readsPerMinute = readsPerMinute;
        this.writesPerMinute = writesPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        boolean write = WRITES.contains(req.getMethod());
        String key = (write ? "w:" : "r:") + caller(req);
        if (!limiter.allow(key, write ? writesPerMinute : readsPerMinute)) {
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setHeader("Retry-After", "60");
            res.setContentType("application/json");
            res.getWriter().write("{\"message\":\"You're going a little fast. Take a breath and try again in a minute.\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    /** Health checks and media files aren't limited. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        return !path.startsWith("/api/");
    }

    private static String caller(HttpServletRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) return "u:" + auth.getName();
        return "ip:" + req.getRemoteAddr();
    }
}
