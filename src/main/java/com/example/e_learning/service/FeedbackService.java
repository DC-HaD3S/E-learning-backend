package com.example.e_learning.service;

import com.example.e_learning.dto.FeedbackDTO;
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

    public void submitFeedback(FeedbackDTO feedbackDTO) {
        if (feedbackDTO.getCourseId() == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        if (feedbackDTO.getRating() == null) {
            throw new IllegalArgumentException("Rating cannot be null");
        }

        User user = userRepository.findByUsername(feedbackDTO.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + feedbackDTO.getUsername()));

        Course course = courseRepository.findById(feedbackDTO.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + feedbackDTO.getCourseId()));

        if (!courseRepository.existsById(feedbackDTO.getCourseId())) {
            throw new IllegalArgumentException("Course ID " + feedbackDTO.getCourseId() + " does not exist in database");
        }

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
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));

        if (!feedback.getUser().getUsername().equals(feedbackDTO.getUsername())) {
            throw new IllegalArgumentException("You can only update your own feedback");
        }

        Course course = courseRepository.findById(feedbackDTO.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + feedbackDTO.getCourseId()));

        if (!courseRepository.existsById(feedbackDTO.getCourseId())) {
            throw new IllegalArgumentException("Course ID " + feedbackDTO.getCourseId() + " does not exist in database");
        }

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
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!feedback.getUser().getUsername().equals(username) && !user.getRole().equals("ADMIN")) {
            throw new IllegalArgumentException("You can only delete your own feedback or must be an admin");
        }

        try {
            feedbackRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete feedback: " + e.getMessage(), e);
        }
    }

    public List<FeedbackDTO> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return feedbacks.stream().map(feedback -> {
            FeedbackDTO dto = new FeedbackDTO();
            dto.setId(feedback.getId());
            dto.setUsername(feedback.getUser().getUsername());
            dto.setCourseName(feedback.getCourse().getTitle());
            dto.setCourseId(feedback.getCourse().getId());
            dto.setRating(feedback.getRating());
            dto.setComments(feedback.getComments());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<FeedbackDTO> getFeedbacksByCourseId(Long courseId) {
        return feedbackRepository.findByCourseId(courseId).stream().map(feedback -> {
            FeedbackDTO dto = new FeedbackDTO();
            dto.setId(feedback.getId());
            dto.setUsername(feedback.getUser().getUsername());
            dto.setCourseName(feedback.getCourse().getTitle());
            dto.setCourseId(feedback.getCourse().getId());
            dto.setRating(feedback.getRating());
            dto.setComments(feedback.getComments());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<FeedbackDTO> getAllFeedbacksByCourseId(Long courseId) {
        return feedbackRepository.findByCourseId(courseId).stream().map(feedback -> {
            FeedbackDTO dto = new FeedbackDTO();
            dto.setId(feedback.getId());
            dto.setUsername(feedback.getUser().getUsername());
            dto.setCourseName(feedback.getCourse().getTitle());
            dto.setCourseId(feedback.getCourse().getId());
            dto.setRating(feedback.getRating());
            dto.setComments(feedback.getComments());
            return dto;
        }).collect(Collectors.toList());
    }

    public Double getAverageRatingByCourseId(Long courseId) {
        Double averageRating = feedbackRepository.findAverageRatingByCourseId(courseId);
        return averageRating != null ? averageRating : 0.0;
    }
}