package com.example.e_learning.repository;

import com.example.e_learning.entity.CourseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {

    @Query("SELECT c FROM CourseContent c WHERE c.course.id = :courseId")
    List<CourseContent> findByCourseId(@Param("courseId") Long courseId);

}