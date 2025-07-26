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
    
    @Query("SELECT COUNT(e.user.id) FROM Enrollment e WHERE e.course.id = :courseId")
    Long countEnrollmentsByCourseId(@Param("courseId") Long courseId);
    

    @Query("SELECT e.course.id AS courseId, COUNT(DISTINCT e.user.id) AS userCount " +
            "FROM Enrollment e " +
            "GROUP BY e.course.id " +
            "ORDER BY userCount DESC, e.course.id ASC")
     List<Object[]> findCourseWithHighestEnrolledUsersCount();
     
     @Query("SELECT e.course.id AS courseId, c.title AS title, COUNT(DISTINCT e.user.id) AS enrollmentCount " +
             "FROM Enrollment e JOIN e.course c " +
             "WHERE c.instructor.id = :instructorId " +
             "GROUP BY e.course.id, c.title " +
             "HAVING COUNT(DISTINCT e.user.id) = (" +
             "  SELECT MAX(enrollmentCount) " +
             "  FROM (SELECT COUNT(DISTINCT e2.user.id) AS enrollmentCount " +
             "        FROM Enrollment e2 JOIN e2.course c2 " +
             "        WHERE c2.instructor.id = :instructorId " +
             "        GROUP BY e2.course.id) AS subquery" +
             ") " +
             "ORDER BY e.course.id ASC")
      List<Object[]> findCoursesWithHighestEnrollmentsByInstructorId(@Param("instructorId") Long instructorId);

    
}