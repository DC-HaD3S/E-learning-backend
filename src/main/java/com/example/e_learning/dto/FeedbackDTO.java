
package com.example.e_learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class FeedbackDTO {
    private Long id; 


	@NotBlank(message = "Username cannot be empty")
	private String username;
	@NotBlank(message = "Course name cannot be empty")
	private String courseName;
	private Long courseId; // Add courseId


	@Min(value = 0, message = "Rating must be at least 0")
	@Max(value = 5, message = "Rating must be at most 5")
	private Integer rating;
	private String comments;

	// Getters and setters
	public String getUsername() {
		return username;
	}
	public Long getCourseId() {
		return courseId;
	}

	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}