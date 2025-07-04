package com.example.e_learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.e_learning.entity.InstructorApplication;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, Long> {
	
}