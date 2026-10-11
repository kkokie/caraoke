package com.caraoke.config;

import com.caraoke.auth.AccountGate;
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
 * Turns suspended and banned accounts away (403) before any controller runs, so every write
 * path is covered without each feature checking. Your standing and deleting your account
 * always work (App Store: deletion must be possible from any state).
 */
public class StandingFilter extends OncePerRequestFilter {

    private static final Set<String> WRITES = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AccountGate gate;

    public StandingFilter(AccountGate gate) {
        this.gate = gate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (signedIn(auth)) {
            var refusal = gate.refusal(auth.getName(), WRITES.contains(req.getMethod()));
            if (refusal.isPresent()) {
                res.setStatus(HttpStatus.FORBIDDEN.value());
                res.setContentType("application/json");
                res.getWriter().write("{\"message\":\"" + refusal.get().replace("\"", "'") + "\",\"code\":\"ACCOUNT_STANDING\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        if (!path.startsWith("/api/")) return true;
        if (path.equals("/api/me/standing")) return true;
        return path.equals("/api/me") && ("GET".equals(req.getMethod()) || "DELETE".equals(req.getMethod()));
    }

    private static boolean signedIn(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
    }
}
