package com.example.e_learning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.e_learning.entity.Course;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByTitle(String title);
}