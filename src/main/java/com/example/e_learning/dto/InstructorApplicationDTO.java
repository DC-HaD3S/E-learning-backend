package com.example.e_learning.dto;

public class InstructorApplicationDTO {

    private String name;


    private String email;
    private String courses;
	private String qualifications;
	
    private int experience;
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public String getCourses() {
		return courses;
	}

	public void setCourses(String courses) {
		this.courses = courses;
	}
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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
}