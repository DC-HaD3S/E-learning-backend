
package com.example.e_learning.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_learning.dto.AverageRatingResponseDTO;
import com.example.e_learning.dto.CourseDTO;
import com.example.e_learning.dto.InstructorApplicationDTO;
import com.example.e_learning.dto.InstructorApplicationRequestDTO;
import com.example.e_learning.dto.InstructorDetailsDTO;
import com.example.e_learning.dto.InstructorHighestEnrollmentDTO;
import com.example.e_learning.service.CourseService;
import com.example.e_learning.service.InstructorApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

@Tag(name = "instructor", description = "Endpoints for managing instructor applications and approvals")
@RestController
@RequestMapping("/instructor")
public class InstructorApplicationController {
    private static final Logger logger = LoggerFactory.getLogger(InstructorApplicationController.class);

    @Autowired
    private InstructorApplicationService service;
    
    
    @Autowired
    private CourseService courseService;

    @Operation(
        summary = "Submit an instructor application",
        description = "Allows a user to submit an instructor application. Name, email, and username are fetched from the authenticated user's account.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Application submitted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "409", description = "User is already an instructor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/apply")
    public ResponseEntity<Map<String, String>> apply(@Valid @RequestBody InstructorApplicationRequestDTO dto) {
        try {
            String username = getAuthenticatedUsername();
            service.submitApplication(dto, username);
            return ResponseEntity.ok(Map.of("message", "Application submitted successfully"));
        } catch (IllegalStateException e) {
            logger.error("Application submission failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (DataAccessException e) {
            logger.error("Database error during application submission: {}", e.getMessage());
            String errorMessage = e.getMostSpecificCause().getMessage().contains("value too long")
                ? "Input too long for one or more fields (e.g., qualifications, courses, URLs, or aboutMe)"
                : "Invalid input: " + e.getMostSpecificCause().getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errorMessage));
        } catch (Exception e) {
            logger.error("Unexpected error during application submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Submission failed: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Get all instructor applications",
        description = "Allows an admin to retrieve all instructor applications.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of applications", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InstructorApplicationDTO.class))),
            @ApiResponse(responseCode = "500", description = "Server error")
        }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/applications")
    public ResponseEntity<List<InstructorApplicationDTO>> getAllApplications() {
        List<InstructorApplicationDTO> applications = service.getAllApplications();
        logger.debug("Returning {} applications", applications.size());
        applications.forEach(app -> logger.debug("Application ID: {}, approved: {}", app.getId(), app.isApproved()));
        return ResponseEntity.ok(applications);
    }

    @Operation(
        summary = "Approve an instructor application",
        description = "Allows an admin to approve an instructor application.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Application approved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve")
    public ResponseEntity<Map<String, String>> approveApplication(
        @Parameter(description = "ID of the application to approve", required = true) @RequestParam Long applicationId
    ) {
        try {
            service.approveApplication(applicationId);
            return ResponseEntity.ok(Map.of("message", "Application approved successfully"));
        } catch (Exception e) {
            logger.error("Error approving application ID {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
            summary = "To fetch average ratings using  all course feedbacks",
            description = "Allows a user to fetch instructor average ratings of their coursesx.")    
    @GetMapping("/average-rating")
    public ResponseEntity<AverageRatingResponseDTO> getInstructorAverageRating(
        @Parameter(description = "ID of the instructor", required = true) @RequestParam Long instructorId
    ) {
        try {
            Double averageRating = service.getInstructorAverageRating(instructorId);
            AverageRatingResponseDTO response = new AverageRatingResponseDTO();
            response.setInstructorId(instructorId);
            response.setAverageRating(averageRating);
            response.setMessage(averageRating != null ? "Average rating calculated successfully" : "No feedback ratings available for instructor's courses");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Instructor not found for ID {}: {}", instructorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AverageRatingResponseDTO(instructorId, null, e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error("Invalid request for instructor ID {}: {}", instructorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AverageRatingResponseDTO(instructorId, null, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving average rating for instructor ID {}: {}", instructorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AverageRatingResponseDTO(instructorId, null, "Server error: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Get total enrollment count for an instructor's courses",
        description = "Retrieves the total number of enrolled students across all courses taught by a specific instructor, identified by instructor application ID. Accessible to all users, including unauthenticated users.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Total enrollment count for the instructor's courses", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "Invalid instructor ID", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @GetMapping("/{instructorId}/enrollment-count")
    public ResponseEntity<?> getEnrollmentCountByInstructorId(
        @Parameter(description = "ID of the instructor application to retrieve enrollment count for", required = true) 
        @PathVariable Long instructorId) {
        try {
            Long enrollmentCount = service.getEnrollmentCountByInstructorId(instructorId);
            return ResponseEntity.ok(enrollmentCount);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid instructor ID {}: {}", instructorId, e.getMessage());
            Map<String, String> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Failed to retrieve enrollment count for instructor ID {}: {}", instructorId, e.getMessage());
            Map<String, String> errorResponse = Map.of("error", "Failed to retrieve enrollment count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Get instructor details by ID",
            description = "Public endpoint to retrieve details of a specific instructor, identified by instructor application ID. Includes name, email, qualifications, experience, courses, photo URL, about me, Twitter URL, and GitHub URL. Accessible to all users, including unauthenticated users.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Instructor details", 
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = InstructorDetailsDTO.class))),
                @ApiResponse(responseCode = "400", description = "Invalid instructor ID", 
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
                @ApiResponse(responseCode = "500", description = "Server error", 
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
            }
        )
        @GetMapping("/{instructorId}")
        public ResponseEntity<?> getInstructorDetailsById(
            @Parameter(description = "ID of the instructor application to retrieve details for", required = true) 
            @PathVariable Long instructorId) {
            try {
                InstructorDetailsDTO instructorDetails = service.getInstructorDetailsById(instructorId);
                return ResponseEntity.ok(instructorDetails);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid instructor ID {}: {}", instructorId, e.getMessage());
                Map<String, String> errorResponse = Map.of("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } catch (Exception e) {
                logger.error("Failed to retrieve instructor details for ID {}: {}", instructorId, e.getMessage());
                Map<String, String> errorResponse = Map.of("error", "Failed to retrieve instructor details: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
    
    
    @Operation(
            summary = "Get instructor's courses with highest enrollments",
            description = "Public endpoint to retrieve the course IDs, titles, and enrollment counts of the courses with the highest number of enrollments for a specific instructor, identified by instructorId. Returns multiple courses in case of ties. Accessible to all users, including unauthenticated users.")
    @GetMapping("/{instructorId}/highest-enrolled-courses")
    public ResponseEntity<?> getInstructorHighestEnrolledCourses(@PathVariable Long instructorId) {
        try {
            List<InstructorHighestEnrollmentDTO> courses = courseService.getInstructorCoursesWithHighestEnrollments(instructorId);
            return ResponseEntity.ok(courses);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid instructor ID: {}", e.getMessage());
            Map<String, String> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Failed to retrieve instructor's highest enrolled courses: {}", e.getMessage());
            Map<String, String> errorResponse = Map.of("error", "Failed to retrieve instructor's highest enrolled courses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @Operation(
        summary = "Get courses by instructor ID",
        description = "Public endpoint to retrieve details of all courses taught by a specific instructor, identified by instructor application ID. Includes course ID, title, body, image URL, price, instructor ID, and instructor name. Accessible to all users, including unauthenticated users.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of course details", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid instructor ID", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @GetMapping("/{instructorId}/courses")
    public ResponseEntity<?> getCoursesByInstructorId(
        @Parameter(description = "ID of the instructor application to retrieve courses for", required = true) 
        @PathVariable Long instructorId) {
        try {
            List<CourseDTO> courses = courseService.getCoursesByInstructorId(instructorId);
            return ResponseEntity.ok(courses);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid instructor ID {}: {}", instructorId, e.getMessage());
            Map<String, String> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Failed to retrieve courses for instructor ID {}: {}", instructorId, e.getMessage());
            Map<String, String> errorResponse = Map.of("error", "Failed to retrieve courses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @Operation(
        summary = "Update instructor details",
        description = "Allows an authenticated instructor to update their own details (e.g., qualifications, experience, courses) for the specified instructor ID. The instructor must match the authenticated user.")
    @PutMapping
    public ResponseEntity<Map<String, String>> updateInstructorDetails(
            @Valid @RequestBody InstructorApplicationRequestDTO dto,
            Principal principal) {
        try {
            if (principal == null) {
                logger.error("No authenticated user found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication required"));
            }
            service.updateInstructorDetails(dto, principal);
            return ResponseEntity.ok(Map.of("message", "Instructor details updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to update instructor details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update instructor details: " + e.getMessage()));
        }
    }
    private String getAuthenticatedUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
    

    @GetMapping("/proxy-image")
    public ResponseEntity<Resource> proxyImage(@RequestParam String url) {
        try {
            logger.debug("Proxying image from: {}", url);
            URL imageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.error("Failed to fetch image from {}: HTTP {}", url, responseCode);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }
            InputStream inputStream = connection.getInputStream();
            InputStreamResource resource = new InputStreamResource(inputStream);
            String contentType = connection.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg"; // Fallback content type
            }
            logger.debug("Serving image with content type: {}", contentType);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error proxying image from {}: {}", url, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}