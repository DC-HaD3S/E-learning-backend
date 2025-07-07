package com.example.e_learning.config;

import org.springframework.stereotype.Component;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("CorsFilter: Processing request: " + request.getMethod() + " " + request.getRequestURI());
        response.setHeader("Access-Control-Allow-Origin", "https://e-learning-management.netlify.app");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers", "Access-Control-Allow-Origin, Access-Control-Allow-Methods");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("CorsFilter: Handling OPTIONS request for " + request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        System.out.println("CorsFilter: Proceeding with chain for " + request.getMethod() + " " + request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}