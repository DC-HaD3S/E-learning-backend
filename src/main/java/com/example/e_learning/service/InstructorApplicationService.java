package com.example.e_learning.service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.e_learning.dto.InstructorApplicationDTO;
import com.example.e_learning.dto.InstructorApplicationRequestDTO;
import com.example.e_learning.dto.InstructorDetailsDTO;
import com.example.e_learning.entity.Course;
import com.example.e_learning.entity.InstructorApplication;
import com.example.e_learning.entity.User;
import com.example.e_learning.repository.CourseRepository;
import com.example.e_learning.repository.FeedbackRepository;
import com.example.e_learning.repository.InstructorApplicationRepository;
import com.example.e_learning.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.regex.*;

@Service
public class InstructorApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(InstructorApplicationService.class);

    @Autowired
    private InstructorApplicationRepository instructorRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private FeedbackRepository feedbackRepo;

    @Transactional
    public void submitApplication(InstructorApplicationRequestDTO dto, String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (user.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("User is already an instructor");
        }

        InstructorApplication application = new InstructorApplication();
        application.setName(user.getName());
        application.setEmail(user.getEmail());
        application.setQualifications(dto.getQualifications());
        application.setExperience(dto.getExperience());
        application.setCourses(dto.getCourses());
        // Transform Google Drive shareable URL to direct URL
        String photoUrl = dto.getPhotoUrl();
        if (photoUrl != null && photoUrl.contains("drive.google.com/file/d/")) {
            String fileId = extractGoogleDriveFileId(photoUrl);
            if (fileId != null) {
                photoUrl = "https://drive.google.com/uc?export=view&id=" + fileId;
                logger.debug("Transformed photoUrl for submission: {}", photoUrl);
            } else {
                logger.warn("Could not extract file ID from photoUrl: {}", photoUrl);
                photoUrl = null; // Set to null if invalid
            }
        }
        application.setPhotoUrl(photoUrl);
        application.setAboutMe(dto.getAboutMe());
        application.setTwitterUrl(dto.getTwitterUrl());
        application.setGithubUrl(dto.getGithubUrl());
        application.setUser(user);
        application.setApproved(false);

        instructorRepo.save(application);
        logger.info("Application submitted for user: {}, approved: {}", username, application.isApproved());
    }

    public List<InstructorApplicationDTO> getAllApplications() {
        List<InstructorApplication> applications = instructorRepo.findAll();
        return applications.stream().map(application -> {
            InstructorApplicationDTO dto = new InstructorApplicationDTO();
            dto.setId(application.getId());
            dto.setName(application.getName());
            dto.setEmail(application.getEmail());
            dto.setQualifications(application.getQualifications());
            dto.setExperience(application.getExperience());
            dto.setCourses(application.getCourses());
            // Transform Google Drive shareable URL to direct URL
            String photoUrl = application.getPhotoUrl();
            if (photoUrl != null && photoUrl.contains("drive.google.com/file/d/")) {
                String fileId = extractGoogleDriveFileId(photoUrl);
                if (fileId != null) {
                    photoUrl = "https://drive.google.com/uc?export=view&id=" + fileId;
                    logger.debug("Transformed photoUrl for application ID {}: {}", application.getId(), photoUrl);
                } else {
                    logger.warn("Could not extract file ID from photoUrl: {}", photoUrl);
                    photoUrl = null;
                }
            }
            dto.setPhotoUrl(photoUrl);
            dto.setAboutMe(application.getAboutMe());
            dto.setTwitterUrl(application.getTwitterUrl());
            dto.setGithubUrl(application.getGithubUrl());
            dto.setApproved(application.isApproved());
            logger.debug("Mapping application ID: {}, approved: {}", application.getId(), application.isApproved());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void approveApplication(Long applicationId) {
        InstructorApplication application = instructorRepo.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + applicationId));

        User user = application.getUser();
        if (user == null) {
            throw new IllegalStateException("No user associated with application: " + applicationId);
        }

        user.setRole("INSTRUCTOR");
        application.setApproved(true);

        userRepo.save(user);
        instructorRepo.save(application);
        logger.info("Application ID: {} approved, courses: {}", applicationId, application.getCourses());
    }

    public InstructorDetailsDTO getInstructorDetailsById(Long instructorId) {
        InstructorApplication instructor = instructorRepo.findById(instructorId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor application not found: " + instructorId));

        InstructorDetailsDTO dto = new InstructorDetailsDTO();
        dto.setName(instructor.getName());
        dto.setEmail(instructor.getEmail());
        dto.setQualifications(instructor.getQualifications());
        dto.setExperience(instructor.getExperience());
        dto.setCourses(instructor.getCourses());
        // Transform Google Drive shareable URL to direct URL
        String photoUrl = instructor.getPhotoUrl();
        if (photoUrl != null && photoUrl.contains("drive.google.com")) {
            String fileId = extractGoogleDriveFileId(photoUrl);
            if (fileId != null) {
                photoUrl = "https://drive.google.com/uc?export=view&id=" + fileId;
                logger.debug("Transformed photoUrl for instructor ID {}: {}", instructorId, photoUrl);
            } else {
                logger.warn("Could not extract file ID from photoUrl: {}", photoUrl);
                photoUrl = null;
            }
        }
        dto.setPhotoUrl(photoUrl);
        dto.setAboutMe(instructor.getAboutMe());
        dto.setTwitterUrl(instructor.getTwitterUrl());
        dto.setGithubUrl(instructor.getGithubUrl());

        logger.info("Retrieved instructor details for ID: {}", instructorId);
        return dto;
    }

    public Double getInstructorAverageRating(Long instructorId) {
        User instructor = userRepo.findById(instructorId)
                .orElseThrow(() -> new EntityNotFoundException("Instructor not found: " + instructorId));

        if (!instructor.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("User is not an instructor: " + instructorId);
        }

        List<Course> courses = courseRepo.findByInstructorId(instructorId);
        if (courses.isEmpty()) {
            logger.info("No courses found for instructor ID: {}", instructorId);
            return null;
        }

        double totalRating = 0.0;
        int courseCount = 0;

        for (Course course : courses) {
            Double avgRating = feedbackRepo.findAverageRatingByCourseId(course.getId());
            if (avgRating != null) {
                totalRating += avgRating;
                courseCount++;
            }
        }

        if (courseCount == 0) {
            logger.info("No feedback ratings found for courses of instructor ID: {}", instructorId);
            return null;
        }

        double averageRating = totalRating / courseCount;
        logger.info("Average rating for instructor ID {}: {}", instructorId, averageRating);
        return averageRating;
    }

    public void updateInstructorDetails(InstructorApplicationRequestDTO dto, Principal principal) {
        String username = principal.getName();
        User currentUser = userRepo.findByUsername(username).orElseThrow(() -> {
            logger.error("User not found: {}", username);
            return new IllegalStateException("User not found: " + username);
        });

        if (!currentUser.getRole().equals("INSTRUCTOR")) {
            logger.error("User {} is not an instructor", username);
            throw new IllegalStateException("Only instructors can update their details");
        }

        InstructorApplication instructorApplication = instructorRepo.findByUserId(currentUser.getId())
                .orElseThrow(() -> {
                    logger.error("Instructor application not found for user ID: {}", currentUser.getId());
                    return new IllegalArgumentException("Instructor application not found for user: " + username);
                });

        if (dto.getQualifications() != null) {
            instructorApplication.setQualifications(dto.getQualifications());
        }
        if (dto.getExperience() >= 0) {
            instructorApplication.setExperience(dto.getExperience());
        }
        if (dto.getCourses() != null) {
            instructorApplication.setCourses(dto.getCourses());
        }
        if (dto.getPhotoUrl() != null) {
            String photoUrl = dto.getPhotoUrl();
            if (photoUrl.contains("drive.google.com")) {
                String fileId = extractGoogleDriveFileId(photoUrl);
                if (fileId != null) {
                    photoUrl = "https://drive.google.com/uc?export=view&id=" + fileId;
                    logger.debug("Transformed photoUrl for update: {}", photoUrl);
                } else {
                    logger.warn("Could not extract file ID from photoUrl: {}", photoUrl);
                    photoUrl = null;
                }
            }
            instructorApplication.setPhotoUrl(photoUrl);
        }
        if (dto.getAboutMe() != null) {
            instructorApplication.setAboutMe(dto.getAboutMe());
        }
        if (dto.getTwitterUrl() != null) {
            instructorApplication.setTwitterUrl(dto.getTwitterUrl());
        }
        if (dto.getGithubUrl() != null) {
            instructorApplication.setGithubUrl(dto.getGithubUrl());
        }

        instructorRepo.save(instructorApplication);
        logger.info("Updated instructor details for user {} (instructor application ID: {})", username,
                instructorApplication.getId());
    }

    public Long getEnrollmentCountByInstructorId(Long instructorId) {
        if (!instructorRepo.existsById(instructorId)) {
            throw new IllegalArgumentException("Instructor application not found: " + instructorId);
        }
        Long count = instructorRepo.countEnrollmentsByInstructorId(instructorId);
        logger.info("Enrollment count for instructor ID {}: {}", instructorId, count);
        return count;
    }

    private String extractGoogleDriveFileId(String url) {
        if (url == null) return null;
        String[] patterns = {
            "/file/d/([^/]+)/",           // Matches /file/d/FILE_ID/
            "id=([^&]+)"                 // Matches id=FILE_ID
        };
        for (String pattern : patterns) {
            Matcher matcher = Pattern.compile(pattern).matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}