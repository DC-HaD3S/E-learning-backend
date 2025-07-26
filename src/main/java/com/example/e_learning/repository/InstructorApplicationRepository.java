package com.example.e_learning.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.e_learning.entity.InstructorApplication;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, Long> {
    Optional<InstructorApplication> findByUserId(Long userId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.instructor.id = :instructorId")
    Long countEnrollmentsByInstructorId(@Param("instructorId") Long instructorId);
}