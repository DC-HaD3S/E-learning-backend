package com.example.e_learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.e_learning.entity.Enrollment;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUserId(Long userId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.course.id = :courseId")
    Optional<Enrollment> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
}