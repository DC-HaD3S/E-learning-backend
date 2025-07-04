package com.example.e_learning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.e_learning.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByCourseId(Long courseId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);
}
