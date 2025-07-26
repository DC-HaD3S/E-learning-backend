package com.example.e_learning.repository;

import com.example.e_learning.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByTitle(String title);
    List<Course> findByInstructorId(Long instructorId);
}