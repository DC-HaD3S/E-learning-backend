package com.example.e_learning.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.e_learning.dto.EnrollmentDTO;
import com.example.e_learning.entity.Enrollment;
import com.example.e_learning.entity.User;
import com.example.e_learning.entity.Course;
import com.example.e_learning.repository.EnrollmentRepository;
import com.example.e_learning.repository.UserRepository;
import com.example.e_learning.repository.CourseRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    public void enrollUserToCourse(String username, Long courseId) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Valid course ID is required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        Optional<Enrollment> existing = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId());
        if (existing.isPresent()) {
            throw new IllegalStateException("User is already enrolled in course: " + course.getTitle());
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);
    }

    public List<EnrollmentDTO> getEnrollmentsByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Valid user ID is required");
        }

        return enrollmentRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EnrollmentDTO convertToDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setUsername(enrollment.getUser().getUsername());
        dto.setCourseId(enrollment.getCourse().getId());
        dto.setCourseName(enrollment.getCourse().getTitle());
        dto.setBody(enrollment.getCourse().getBody());
        dto.setImageUrl(enrollment.getCourse().getImageUrl());
        dto.setPrice(enrollment.getCourse().getPrice());
        return dto;
    }
}
