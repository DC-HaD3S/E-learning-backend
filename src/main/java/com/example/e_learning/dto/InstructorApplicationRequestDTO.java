
package com.example.e_learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InstructorApplicationRequestDTO {
    @NotBlank(message = "Qualifications cannot be empty")
    @Size(max = 300, message = "Qualifications must not exceed 300 characters")
    private String qualifications;

    @Min(value = 0, message = "Experience cannot be negative")
    private int experience;

    @NotBlank(message = "Courses cannot be empty")
    @Size(max = 300, message = "Courses must not exceed 300 characters")
    private String courses;

    @Size(max = 3000, message = "Photo URL must not exceed 3000 characters")
    private String photoUrl;

    @Size(max = 10000, message = "About me must not exceed 10000 characters")
    private String aboutMe;

    @Size(max = 3000, message = "Twitter URL must not exceed 3000 characters")
    private String twitterUrl;

    @Size(max = 3000, message = "GitHub URL must not exceed 3000 characters")
    private String githubUrl;

    // Getters and setters
    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getCourses() {
        return courses;
    }

    public void setCourses(String courses) {
        this.courses = courses;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }
}