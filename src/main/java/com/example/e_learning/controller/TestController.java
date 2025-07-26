  package com.example.e_learning.controller;

  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;

  import jakarta.servlet.http.HttpServletResponse;

  @RestController
  @RequestMapping("/test")
  public class TestController {

      @GetMapping("/cors")
      public ResponseEntity<String> testCors(HttpServletResponse response) {
          response.setHeader("Access-Control-Allow-Origin", "https://e-learning-management.netlify.app");
          response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
          response.setHeader("Access-Control-Allow-Headers", "*");
          response.setHeader("Access-Control-Allow-Credentials", "true");
          return ResponseEntity.ok("CORS test successful");
      }
  }
