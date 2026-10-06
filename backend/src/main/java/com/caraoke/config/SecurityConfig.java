package com.caraoke.config;

import com.caraoke.auth.DevUserFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * Stateless API security.
 *
 * Prod: every request carries a Firebase ID token (Authorization: Bearer ...),
 *       validated as a standard JWT against Google's public keys.
 * Dev:  app.auth.dev-mode=true swaps in an "X-Dev-User" header so you can
 *       exercise the API with curl/Postman before Firebase exists.
 *
 * Either way, controllers read the caller's uid from Authentication#getName().
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            @Value("${app.auth.dev-mode:false}") boolean devMode) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                       // no cookies/sessions -> no CSRF surface
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // When a controller throws (404, 409, 400...), Spring forwards internally to /error.
                // That ERROR dispatch must not be re-authenticated, or every error becomes a 401.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Public profiles and handle checks are readable without login
                .requestMatchers(HttpMethod.GET, "/api/users/*", "/api/handles/*/available").permitAll()
                .anyRequest().authenticated())
            // Missing/invalid credentials -> 401 (not the default 403) so the app knows to re-auth
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        if (devMode) {
            http.addFilterBefore(new DevUserFilter(), AnonymousAuthenticationFilter.class);
        } else {
            http.oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
        }
        return http.build();
    }
}
