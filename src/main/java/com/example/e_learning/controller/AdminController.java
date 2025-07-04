//package com.example.e_learning.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import com.example.e_learning.dto.CourseDTO;
//import com.example.e_learning.dto.EnrollmentDTO;
//import com.example.e_learning.dto.UserDTO;
//import com.example.e_learning.entity.Course;
//import com.example.e_learning.service.CourseService;
//import com.example.e_learning.service.EnrollmentService;
//import com.example.e_learning.service.UserService;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Tag(name = "admin")
//@RestController
//@RequestMapping("/admin")
//@PreAuthorize("hasRole('ROLE_ADMIN')")
//public class AdminController {
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private EnrollmentService enrollmentService;
//    @Autowired
//    private CourseService courseService;
//
//
//    @GetMapping("/enrolled")
//    public ResponseEntity<List<EnrollmentDTO>> getAllEnrolledUsers() {
//        try {
//            return ResponseEntity.ok(enrollmentService.getAllEnrollments());
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
//
//
//
//
//
//
//}