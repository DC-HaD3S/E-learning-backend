package com.example.e_learning.config;

import com.example.e_learning.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        res.setHeader("Access-Control-Allow-Origin", "https://e-learning-management.netlify.app");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "*");
        res.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            logger.debug("Handled CORS preflight request for {}", req.getRequestURI());
            return;
        }

        String path = req.getRequestURI();
        if (isPublicEndpoint(path)) {
            logger.debug("Skipping JWT processing for public endpoint: {}", path);
            chain.doFilter(req, res);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                String username = jwtService.extractUsername(jwt);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
                    if (role != null && jwtService.isTokenValid(jwt)) {
                        var authorities = List.of(new SimpleGrantedAuthority(role));
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        logger.debug("Authenticated user: {} with role: {} for path: {}", username, role, path);
                    } else {
                        logger.warn("No role found or invalid JWT for user: {} on path: {}", username, path);
                    }
                }
            } catch (Exception e) {
                logger.warn("JWT processing error for path: {}: {}", path, e.getMessage());
            }
        } else {
            logger.debug("No valid Authorization header for path: {}", path);
        }

        chain.doFilter(req, res);
    }

    private boolean isPublicEndpoint(String path) {
        String[] publicEndpoints = {
            "/auth/login",
            "/auth/signup",
            "/auth/check-username",
            "/auth/check-email",
            "/courses",
            "/courses/highest-enrolled-users-count",
            "/instructor/",
            "/feedback/",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/test/",
            "/admin/add-bcrypt-prefix"
        };
        for (String endpoint : publicEndpoints) {
            // Exact match for /courses to avoid matching /courses/enrolled-courses
            if (endpoint.equals("/courses") ? path.equals("/courses") : path.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }
}