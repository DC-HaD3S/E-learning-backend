package com.example.e_learning.dto;

import jakarta.validation.constraints.Size;

public class ApproveApplicationRequestDTO {
    @Size(max = 300, message = "Approved courses must not exceed 300 characters")
    private String approvedCourses;

    public String getApprovedCourses() {
        return approvedCourses;
    }

    public void setApprovedCourses(String approvedCourses) {
        this.approvedCourses = approvedCourses;
    }
}