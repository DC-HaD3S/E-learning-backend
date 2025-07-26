package com.example.e_learning.dto;

public class HighestEnrollmentDTO {
    private Long courseId;
    private Long count;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}