//package com.example.e_learning.controller;
//
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import com.example.e_learning.dto.InstructorApplicationDTO;
//import com.example.e_learning.service.InstructorApplicationService;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//
//import java.util.Map;
//
//@Tag(name = "instructor")
//@RestController
//@RequestMapping("/instructor")
//public class InstructorApplicationController {
//    @Autowired
//    private InstructorApplicationService service;
//
//    @PreAuthorize("hasRole('USER')")
//    @PostMapping("/apply")
//    public ResponseEntity<Map<String, String>> apply(@RequestBody InstructorApplicationDTO dto, @RequestParam String username) {
//        service.submitApplication(dto, username);
//        return ResponseEntity.ok(Map.of("message", "Application submitted"));
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/applications")
//    public ResponseEntity<List<InstructorApplicationDTO>> getAllApplications() {
//        return ResponseEntity.ok(service.getAllApplications());
//    }
//}