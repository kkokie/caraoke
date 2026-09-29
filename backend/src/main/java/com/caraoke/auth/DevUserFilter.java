package com.caraoke.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * LOCAL DEVELOPMENT ONLY. Authenticates the request as whatever uid is in
 * the X-Dev-User header. Only registered when app.auth.dev-mode=true.
 */
public class DevUserFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Dev-User";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uid = req.getHeader(HEADER);
        if (uid != null && !uid.isBlank()) {
            var auth = new UsernamePasswordAuthenticationToken(uid, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(req, res);
    }
}
