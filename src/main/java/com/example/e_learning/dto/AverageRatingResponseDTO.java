package com.example.e_learning.dto;

public class AverageRatingResponseDTO {
    private Long instructorId;
    private Double averageRating;
    private String message;

    public AverageRatingResponseDTO() {
    }

    public AverageRatingResponseDTO(Long instructorId, Double averageRating, String message) {
        this.instructorId = instructorId;
        this.averageRating = averageRating;
        this.message = message;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}