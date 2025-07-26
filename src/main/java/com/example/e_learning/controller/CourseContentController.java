package com.example.e_learning.controller;

import com.example.e_learning.dto.CourseContentDTO;
import com.example.e_learning.dto.SubtopicDTO;
import com.example.e_learning.dto.TopicDTO;
import com.example.e_learning.service.CourseContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courses")
@Tag(name = "Course Content", description = "Endpoints for managing course topics and subtopics")
public class CourseContentController {

    private final CourseContentService courseContentService;

    public CourseContentController(CourseContentService courseContentService) {
        this.courseContentService = courseContentService;
    }

    @Operation(summary = "Add topic for a specific course", 
               description = "Creates multiple topic entries with dynamic subtopics for a given course ID. Admins can add topics to any course; instructors only to their own.")
    @ApiResponse(responseCode = "200", description = "Topics added successfully", 
                 content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "400", description = "Invalid course ID or request body")
    @ApiResponse(responseCode = "403", description = "Unauthorized: User is not an admin or instructor, or instructor is not authorized for this course")
    @PostMapping("/{courseId}/topic")
    public ResponseEntity<Map<String, String>> addTopic(@PathVariable Long courseId, 
                                                       @RequestBody List<CourseContentDTO> contentDTOs) {
        courseContentService.createTopic(courseId, contentDTOs);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Topic added successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get topics by course ID", 
               description = "Retrieves all topics and their subtopics for a specific course.")
    @ApiResponse(responseCode = "200", description = "List of topics for the specified course", 
                 content = @Content(schema = @Schema(implementation = CourseContentDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid course ID")
    @GetMapping("/{courseId}/topic")
    public List<CourseContentDTO> getTopicByCourseId(@PathVariable Long courseId) {
        return courseContentService.getTopicByCourseId(courseId);
    }

    @Operation(summary = "Update a topic by ID", 
               description = "Updates the name of a specific topic within a course. Admins can update any topic; instructors only their own.")
    @ApiResponse(responseCode = "200", description = "Topic updated successfully", 
                 content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "400", description = "Invalid course ID or topic ID")
    @ApiResponse(responseCode = "403", description = "Unauthorized: User is not an admin or instructor, or instructor is not authorized for this topic")
    @PutMapping("/{courseId}/topic/{topicId}")
    public ResponseEntity<Map<String, String>> updateTopic(@PathVariable Long courseId, @PathVariable Long topicId, @RequestBody TopicDTO dto) {
        courseContentService.updateTopic(courseId, topicId, dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Topic updated successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add a subtopic to a topic", 
               description = "Adds a new subtopic to a specific topic within a course. Admins can add subtopics to any topic; instructors only to their own.")
    @ApiResponse(responseCode = "200", description = "Subtopic added successfully", 
                 content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "400", description = "Invalid course ID, topic ID, or request body")
    @ApiResponse(responseCode = "403", description = "Unauthorized: User is not an admin or instructor, or instructor is not authorized for this topic")
    @PostMapping("/{courseId}/topic/{topicId}/subtopic")
    public ResponseEntity<Map<String, String>> addSubtopic(@PathVariable Long courseId, @PathVariable Long topicId, @RequestBody SubtopicDTO dto) {
        courseContentService.addSubtopic(courseId, topicId, dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Subtopic added successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a subtopic by ID", 
               description = "Updates a specific subtopic (name and URL) within a topic and course. Admins can update any subtopic; instructors only their own.")
    @ApiResponse(responseCode = "200", description = "Subtopic updated successfully", 
                 content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "400", description = "Invalid course ID, topic ID, or subtopic ID")
    @ApiResponse(responseCode = "403", description = "Unauthorized: User is not an admin or instructor, or instructor is not authorized for this subtopic")
    @PutMapping("/{courseId}/topic/{topicId}/subtopic/{subtopicId}")
    public ResponseEntity<Map<String, String>> updateSubtopic(@PathVariable Long courseId, @PathVariable Long topicId, @PathVariable Long subtopicId, @RequestBody SubtopicDTO dto) {
        courseContentService.updateSubtopic(courseId, topicId, subtopicId, dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Subtopic updated successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a topic", 
               description = "Deletes a specific topic and its subtopics within a course. Admins can delete any topic; instructors only their own.")
    @ApiResponse(responseCode = "200", description = "Topic deleted successfully", 
                 content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "400", description = "Invalid course ID or topic ID")
    @ApiResponse(responseCode = "403", description = "Unauthorized: User is not an admin or instructor, or instructor is not authorized for this topic")
    @DeleteMapping("/{courseId}/topic/{topicId}")
    public ResponseEntity<Map<String, String>> deleteTopic(@PathVariable Long courseId, @PathVariable Long topicId) {
        courseContentService.deleteTopic(courseId, topicId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Topic deleted successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a subtopic", 
               description = "Deletes a specific subtopic from a topic within a course. Admins can delete any subtopic; instructors only from their own topic.")
    @ApiResponse(responseCode = "200", description = "Subtopic deleted successfully", 
                 content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "400", description = "Invalid course ID, topic ID, or subtopic ID")
    @ApiResponse(responseCode = "403", description = "Unauthorized: User is not an admin or instructor, or instructor is not authorized for this subtopic")
    @DeleteMapping("/{courseId}/topic/{topicId}/subtopic/{subtopicId}")
    public ResponseEntity<Map<String, String>> deleteSubtopic(@PathVariable Long courseId, @PathVariable Long topicId, @PathVariable Long subtopicId) {
        courseContentService.deleteSubtopic(courseId, topicId, subtopicId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Subtopic deleted successfully");
        return ResponseEntity.ok(response);
    }
}