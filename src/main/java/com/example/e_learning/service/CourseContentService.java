package com.example.e_learning.service;

import com.example.e_learning.dto.CourseContentDTO;
import com.example.e_learning.dto.SubtopicDTO;
import com.example.e_learning.dto.TopicDTO;
import com.example.e_learning.entity.Course;
import com.example.e_learning.entity.CourseContent;
import com.example.e_learning.entity.Subtopic;
import com.example.e_learning.entity.User;
import com.example.e_learning.repository.CourseContentRepository;
import com.example.e_learning.repository.CourseRepository;
import com.example.e_learning.repository.SubtopicRepository;
import com.example.e_learning.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseContentService {

    private final CourseContentRepository courseContentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SubtopicRepository subtopicRepository;

    public CourseContentService(CourseContentRepository courseContentRepository,
                                CourseRepository courseRepository,
                                UserRepository userRepository,
                                SubtopicRepository subtopicRepository) {
        this.courseContentRepository = courseContentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.subtopicRepository = subtopicRepository;
    }

    @Transactional
    public void createTopic(Long courseId, List<CourseContentDTO> dtos) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (!user.getRole().equals("ADMIN") && !user.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("Only admins and instructors can create topics");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        if (!user.getRole().equals("ADMIN") && 
            (course.getInstructor() == null || !course.getInstructor().getId().equals(user.getId()))) {
            throw new IllegalStateException("Instructors can only add topics to their own courses");
        }

        dtos.forEach(dto -> {
            CourseContent topic = new CourseContent();
            topic.setTopic(dto.getTopic());
            topic.setCourse(course);
            topic.setInstructor(user.getRole().equals("ADMIN") ? null : user);

            if (dto.getSubtopics() != null) {
                List<Subtopic> subtopics = dto.getSubtopics().stream().map(subtopicDto -> {
                    Subtopic subtopic = new Subtopic();
                    subtopic.setName(subtopicDto.getName());
                    subtopic.setUrl(subtopicDto.getUrl());
                    subtopic.setCourseContent(topic);
                    return subtopic;
                }).collect(Collectors.toList());
                topic.setSubtopics(subtopics);
            }

            courseContentRepository.save(topic);
        });
    }

    public List<CourseContentDTO> getTopicByCourseId(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        List<CourseContent> topics = courseContentRepository.findByCourseId(courseId);
        return topics.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void updateTopic(Long courseId, Long topicId, TopicDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (!user.getRole().equals("ADMIN") && !user.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("Only admins and instructors can update topics");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        CourseContent topic = courseContentRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicId));

        if (!topic.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Topic does not belong to the specified course");
        }

        if (!user.getRole().equals("ADMIN") && 
            (topic.getInstructor() == null || !topic.getInstructor().getId().equals(user.getId()))) {
            throw new IllegalStateException("Instructors can only update their own topics");
        }

        topic.setTopic(dto.getTopic());
        courseContentRepository.save(topic);
    }

    @Transactional
    public void addSubtopic(Long courseId, Long topicId, SubtopicDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (!user.getRole().equals("ADMIN") && !user.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("Only admins and instructors can add subtopics");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        CourseContent topic = courseContentRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicId));

        if (!topic.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Topic does not belong to the specified course");
        }

        if (!user.getRole().equals("ADMIN") && 
            (topic.getInstructor() == null || !topic.getInstructor().getId().equals(user.getId()))) {
            throw new IllegalStateException("Instructors can only add subtopics to their own topics");
        }

        Subtopic subtopic = new Subtopic();
        subtopic.setName(dto.getName());
        subtopic.setUrl(dto.getUrl());
        subtopic.setCourseContent(topic);
        subtopicRepository.save(subtopic);
    }

    @Transactional
    public void updateSubtopic(Long courseId, Long topicId, Long subtopicId, SubtopicDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (!user.getRole().equals("ADMIN") && !user.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("Only admins and instructors can update subtopics");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        CourseContent topic = courseContentRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicId));

        if (!topic.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Topic does not belong to the specified course");
        }

        if (!user.getRole().equals("ADMIN") && 
            (topic.getInstructor() == null || !topic.getInstructor().getId().equals(user.getId()))) {
            throw new IllegalStateException("Instructors can only update subtopics from their own topics");
        }

        Subtopic subtopic = subtopicRepository.findById(subtopicId)
                .orElseThrow(() -> new IllegalArgumentException("Subtopic not found: " + subtopicId));

        if (!subtopic.getCourseContent().getId().equals(topicId)) {
            throw new IllegalArgumentException("Subtopic does not belong to the specified topic");
        }

        subtopic.setName(dto.getName());
        subtopic.setUrl(dto.getUrl());
        subtopicRepository.save(subtopic);
    }

    @Transactional
    public void deleteTopic(Long courseId, Long topicId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (!user.getRole().equals("ADMIN") && !user.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("Only admins and instructors can delete topics");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        CourseContent topic = courseContentRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicId));

        if (!topic.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Topic does not belong to the specified course");
        }

        if (!user.getRole().equals("ADMIN") && 
            (topic.getInstructor() == null || !topic.getInstructor().getId().equals(user.getId()))) {
            throw new IllegalStateException("Instructors can only delete their own topics");
        }

        courseContentRepository.deleteById(topicId);
    }

    @Transactional
    public void deleteSubtopic(Long courseId, Long topicId, Long subtopicId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (!user.getRole().equals("ADMIN") && !user.getRole().equals("INSTRUCTOR")) {
            throw new IllegalStateException("Only admins and instructors can delete subtopics");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        CourseContent topic = courseContentRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicId));

        if (!topic.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Topic does not belong to the specified course");
        }

        if (!user.getRole().equals("ADMIN") && 
            (topic.getInstructor() == null || !topic.getInstructor().getId().equals(user.getId()))) {
            throw new IllegalStateException("Instructors can only delete subtopics from their own topics");
        }

        Subtopic subtopic = subtopicRepository.findById(subtopicId)
                .orElseThrow(() -> new IllegalArgumentException("Subtopic not found: " + subtopicId));

        if (!subtopic.getCourseContent().getId().equals(topicId)) {
            throw new IllegalArgumentException("Subtopic does not belong to the specified topic");
        }

        subtopicRepository.deleteById(subtopicId);
    }

    private CourseContentDTO convertToDTO(CourseContent topic) {
        CourseContentDTO dto = new CourseContentDTO();
        dto.setId(topic.getId());
        dto.setTopic(topic.getTopic());
        dto.setSubtopics(topic.getSubtopics().stream().map(subtopic -> {
            CourseContentDTO.Subtopic subtopicDto = new CourseContentDTO.Subtopic();
            subtopicDto.setId(subtopic.getId());
            subtopicDto.setName(subtopic.getName());
            subtopicDto.setUrl(subtopic.getUrl());
            return subtopicDto;
        }).collect(Collectors.toList()));
        return dto;
    }
}