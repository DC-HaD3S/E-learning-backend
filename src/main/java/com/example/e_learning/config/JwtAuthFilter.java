package com.example.e_learning.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.e_learning.service.JwtService;

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

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = req.getHeader("Authorization");
        System.out.println("JwtAuthFilter: Processing request " + req.getRequestURI() + " with Authorization: " + authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(jwt)) {
                    String role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
                    System.out.println("JwtAuthFilter: Valid token for user " + username + " with role " + role);
                    if (role != null) {
                        var authorities = List.of(new SimpleGrantedAuthority(role));
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } else {
                    System.out.println("JwtAuthFilter: Invalid token for user " + username);
                }
            }
        } else {
            System.out.println("JwtAuthFilter: No Authorization header for " + req.getRequestURI());
        }
        chain.doFilter(req, res);
    }
}