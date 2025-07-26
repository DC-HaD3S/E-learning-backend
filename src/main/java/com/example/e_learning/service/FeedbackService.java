package com.example.e_learning.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.e_learning.dto.FeedbackDTO;
import com.example.e_learning.dto.HighestRatedCourseDTO;
import com.example.e_learning.entity.Feedback;
import com.example.e_learning.entity.User;
import com.example.e_learning.entity.Course;
import com.example.e_learning.repository.FeedbackRepository;
import com.example.e_learning.repository.UserRepository;
import com.example.e_learning.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);


    public void submitFeedback(FeedbackDTO feedbackDTO) {
        if (feedbackDTO.getCourseId() == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        if (feedbackDTO.getRating() == null) {
            throw new IllegalArgumentException("Rating cannot be null");
        }

        User user = userRepository.findByUsername(feedbackDTO.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + feedbackDTO.getUsername()));

        Course course = courseRepository.findById(feedbackDTO.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + feedbackDTO.getCourseId()));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setCourse(course);
        feedback.setRating(feedbackDTO.getRating());
        feedback.setComments(feedbackDTO.getComments());

        try {
            feedbackRepository.save(feedback);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Invalid course or user ID: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save feedback: " + e.getMessage(), e);
        }
    }

    public void updateFeedback(Long id, FeedbackDTO feedbackDTO) {
        if (feedbackDTO.getCourseId() == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        if (feedbackDTO.getRating() == null) {
            throw new IllegalArgumentException("Rating cannot be null");
        }

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with ID: " + id));

        User user = userRepository.findByUsername(feedbackDTO.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + feedbackDTO.getUsername()));

        if (!feedback.getUser().getUsername().equals(feedbackDTO.getUsername())) {
            throw new IllegalArgumentException("You can only update your own feedback");
        }

        Course course = courseRepository.findById(feedbackDTO.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + feedbackDTO.getCourseId()));

        feedback.setCourse(course);
        feedback.setRating(feedbackDTO.getRating());
        feedback.setComments(feedbackDTO.getComments());

        try {
            feedbackRepository.save(feedback);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Invalid course or user ID: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update feedback: " + e.getMessage(), e);
        }
    }

    public void deleteFeedback(Long id, String username) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found with ID: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        if (!feedback.getUser().getUsername().equals(username) && !user.getRole().equalsIgnoreCase("ADMIN")) {
            throw new IllegalArgumentException("You can only delete your own feedback or must be an admin");
        }

        try {
            feedbackRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete feedback: " + e.getMessage(), e);
        }
    }

    public List<FeedbackDTO> getAllFeedbacks() {
        try {
            return feedbackRepository.findAll().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve feedbacks: " + e.getMessage(), e);
        }
    }

    public List<FeedbackDTO> getFeedbacksByCourseId(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        try {
            return feedbackRepository.findByCourseId(courseId).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve course feedbacks: " + e.getMessage(), e);
        }
    }

    public List<FeedbackDTO> getAllFeedbacksByCourseId(Long courseId) {
        try {
            return feedbackRepository.findByCourseId(courseId).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve feedbacks by course: " + e.getMessage(), e);
        }
    }

    public Double getAverageRatingByCourseId(Long courseId) {
        try {
            Double averageRating = feedbackRepository.findAverageRatingByCourseId(courseId);
            return averageRating != null ? averageRating : 0.0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate average rating: " + e.getMessage(), e);
        }
    }

    public Long getFeedbackCountByInstructorId(Long instructorId) {
        return feedbackRepository.countFeedbackByInstructorId(instructorId);
    }
    
    private FeedbackDTO mapToDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setUsername(feedback.getUser().getUsername());
        dto.setCourseName(feedback.getCourse().getTitle());
        dto.setCourseId(feedback.getCourse().getId());
        dto.setRating(feedback.getRating());
        dto.setComments(feedback.getComments());
        return dto;
    }
    

    public List<HighestRatedCourseDTO> getHighestRatedCourses() {
        List<Object[]> results = feedbackRepository.findCoursesWithHighestAverageRating();
        logger.info("Query returned {} results", results.size());

        if (results.isEmpty()) {
            logger.info("No feedback found for any courses");
            return List.of();
        }

        List<HighestRatedCourseDTO> dtos = results.stream()
            .filter(result -> result.length >= 3)
            .map(result -> {
                Long courseId = ((Number) result[0]).longValue();
                String title = (String) result[1];
                Double averageRating = ((Number) result[2]).doubleValue();
                logger.info("Processing course: courseId = {}, title = {}, averageRating = {}", 
                            courseId, title, averageRating);
                HighestRatedCourseDTO dto = new HighestRatedCourseDTO();
                dto.setCourseId(courseId);
                dto.setTitle(title);
                dto.setAverageRating(averageRating);
                return dto;
            })
            .collect(Collectors.toList());

        if (dtos.isEmpty()) {
            logger.warn("No valid results after processing");
        }

        return dtos;
    }
}
