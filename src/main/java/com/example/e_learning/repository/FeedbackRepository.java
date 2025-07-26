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

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.course.instructor.id = :instructorId")
    Long countFeedbackByInstructorId(@Param("instructorId") Long instructorId);
    

    @Query("SELECT f.course.id AS courseId, c.title AS title, AVG(f.rating) AS averageRating " +
            "FROM Feedback f JOIN f.course c " +
            "GROUP BY f.course.id, c.title " +
            "HAVING AVG(f.rating) = (" +
            "  SELECT MAX(avgRating) " +
            "  FROM (SELECT AVG(f2.rating) AS avgRating " +
            "        FROM Feedback f2 " +
            "        GROUP BY f2.course.id) AS subquery" +
            ") " +
            "ORDER BY f.course.id ASC")
     List<Object[]> findCoursesWithHighestAverageRating();
}