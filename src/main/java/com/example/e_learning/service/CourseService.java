package com.example.e_learning.service;

import com.example.e_learning.dto.CourseDTO;
import com.example.e_learning.dto.HighestEnrollmentDTO;
import com.example.e_learning.dto.InstructorHighestEnrollmentDTO;
import com.example.e_learning.entity.Course;
import com.example.e_learning.entity.InstructorApplication;
import com.example.e_learning.entity.User;
import com.example.e_learning.repository.CourseRepository;
import com.example.e_learning.repository.EnrollmentRepository;
import com.example.e_learning.repository.InstructorApplicationRepository;
import com.example.e_learning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired private CourseRepository courseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InstructorApplicationRepository instructorApplicationRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByInstructor() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        if (!user.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("User is not an instructor");
        }
        InstructorApplication instructorApp = instructorApplicationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Instructor application not found for user: " + username));
        List<Course> courses = courseRepository.findByInstructorId(instructorApp.getId());
        if (courses.isEmpty()) {
            throw new IllegalStateException("No courses found for instructor application ID: " + instructorApp.getId());
        }
        return courses.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Course createCourse(CourseDTO courseDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        if (!user.getRole().equals("ADMIN") && !user.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("Only admins and instructors can create courses");
        }

        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setBody(courseDTO.getBody());
        course.setImageUrl(courseDTO.getImageUrl());
        course.setPrice(courseDTO.getPrice());

        // If user is an instructor, they must provide their own instructorId
        if (user.getRole().equals("INSTRUCTOR")) {
            if (courseDTO.getInstructorId() == null) {
                throw new IllegalArgumentException("Instructor ID is required for instructors");
            }

            InstructorApplication instructorApp = instructorApplicationRepository.findById(courseDTO.getInstructorId())
                    .orElseThrow(() -> new IllegalStateException("Instructor application not found: " + courseDTO.getInstructorId()));

            if (!instructorApp.getUser().getId().equals(user.getId())) {
                throw new IllegalStateException("Instructor application does not belong to the authenticated user");
            }

            course.setInstructor(instructorApp);
        }

        // Admins should not set instructor; ignore instructorId if present
        return courseRepository.save(course);
    }

    public void setCourseInstructor(Long courseId, Long instructorId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        if (!user.getRole().equals("ADMIN")) {
            throw new IllegalStateException("Only admins can set course instructors");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (instructorId == null) {
            throw new IllegalArgumentException("Instructor ID is required");
        }
        InstructorApplication instructorApp = instructorApplicationRepository.findById(instructorId)
                .orElseThrow(() -> new IllegalStateException("Instructor application not found: " + instructorId));
        course.setInstructor(instructorApp);
        courseRepository.save(course);
    }

    public void updateCourse(Long courseId, CourseDTO courseDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (!user.getRole().equals("ADMIN")) {
            InstructorApplication instructorApp = instructorApplicationRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalStateException("Instructor application not found for user: " + username));
            if (course.getInstructor() == null || !course.getInstructor().getId().equals(instructorApp.getId())) {
                throw new IllegalStateException("Access denied: you can only update your own courses");
            }
        }
        course.setTitle(courseDTO.getTitle());
        course.setBody(courseDTO.getBody());
        course.setImageUrl(courseDTO.getImageUrl());
        course.setPrice(courseDTO.getPrice());
        courseRepository.save(course);
    }

    public void deleteCourse(Long courseId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (!user.getRole().equals("ADMIN")) {
            InstructorApplication instructorApp = instructorApplicationRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalStateException("Instructor application not found for user: " + username));
            if (course.getInstructor() == null || !course.getInstructor().getId().equals(instructorApp.getId())) {
                throw new IllegalStateException("Access denied: you can only delete your own courses");
            }
        }
        courseRepository.deleteById(courseId);
    }
    

    public CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setBody(course.getBody());
        dto.setImageUrl(course.getImageUrl());
        dto.setPrice(course.getPrice());
        dto.setInstructorId(course.getInstructor() != null ? course.getInstructor().getId() : null);
        
        // Set instructor name
        if (course.getInstructor() != null) {
            User instructorUser = course.getInstructor().getUser();
            dto.setInstructor(instructorUser != null ? instructorUser.getUsername() : "Unknown Instructor");
        } else {
            dto.setInstructor("Unknown Instructor");
        }
        
        return dto;
    }

    public List<CourseDTO> getCoursesByInstructorId(Long instructorId) {
        logger.debug("Fetching courses for instructorId: {}", instructorId);
        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        if (courses.isEmpty()) {
            logger.warn("No courses found for instructorId: {}", instructorId);
        }
        return courses.stream().map(course -> {
            CourseDTO dto = new CourseDTO();
            dto.setId(course.getId());
            dto.setTitle(course.getTitle());
            dto.setBody(course.getBody());
            dto.setImageUrl(course.getImageUrl());
            dto.setPrice(course.getPrice());
            dto.setInstructorId(course.getInstructor().getId());
            dto.setInstructor(course.getInstructor().getName());
            return dto;
        }).collect(Collectors.toList());
    }
    
    public Long getEnrollmentCountByCourseId(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Course not found: " + courseId);
        }
        Long count = enrollmentRepository.countEnrollmentsByCourseId(courseId);
        logger.info("Enrollment count for course ID {}: {}", courseId, count);
        return count;
    }

    public HighestEnrollmentDTO getHighestEnrolledUsersCount() {
        List<Object[]> results = enrollmentRepository.findCourseWithHighestEnrolledUsersCount();
        HighestEnrollmentDTO dto = new HighestEnrollmentDTO();

        if (results == null || results.isEmpty()) {
            logger.info("No enrollments found or query returned empty result: {}", results);
            dto.setCourseId(null);
            dto.setCount(0L);
            return dto;
        }

        Object[] result = results.get(0); // Take the first result
        if (result.length < 2) {
            logger.info("Invalid query result: result is {}, length is {}", 
                        java.util.Arrays.toString(result), result.length);
            dto.setCourseId(null);
            dto.setCount(0L);
            return dto;
        }

        Long courseId = ((Number) result[0]).longValue();
        Long count = ((Number) result[1]).longValue();
        logger.info("Query result: courseId = {}, count = {}", courseId, count);

        if (!courseRepository.existsById(courseId)) {
            logger.warn("Course ID {} from enrollment data does not exist in course table", courseId);
            dto.setCourseId(null);
            dto.setCount(0L);
        } else {
            logger.info("Highest enrolled users count for course ID {}: {}", courseId, count);
            dto.setCourseId(courseId);
            dto.setCount(count);
        }

        return dto;
    }
    
    public List<InstructorHighestEnrollmentDTO>

 getInstructorCoursesWithHighestEnrollments(Long instructorId) {
        if (!instructorApplicationRepository.existsById(instructorId)) {
            logger.warn("Instructor ID {} does not exist", instructorId);
            throw new IllegalArgumentException("Instructor not found: " + instructorId);
        }

        List<Object[]> results = enrollmentRepository.findCoursesWithHighestEnrollmentsByInstructorId(instructorId);
        logger.info("Query returned {} results for instructor ID {}", results.size(), instructorId);

        if (results.isEmpty()) {
            logger.info("No enrollments or courses found for instructor ID {}", instructorId);
            return List.of();
        }

        List<InstructorHighestEnrollmentDTO> dtos = results.stream()
            .filter(result -> result.length >= 3)
            .map(result -> {
                Long courseId = ((Number) result[0]).longValue();
                String title = (String) result[1];
                Long enrollmentCount = ((Number) result[2]).longValue();
                logger.info("Processing course: courseId = {}, title = {}, enrollmentCount = {}", 
                            courseId, title, enrollmentCount);
                InstructorHighestEnrollmentDTO dto = new InstructorHighestEnrollmentDTO();
                dto.setCourseId(courseId);
                dto.setTitle(title);
                dto.setEnrollmentCount(enrollmentCount);
                return dto;
            })
            .collect(Collectors.toList());

        if (dtos.isEmpty()) {
            logger.warn("No valid results after processing for instructor ID {}", instructorId);
        }

        return dtos;
    }
}