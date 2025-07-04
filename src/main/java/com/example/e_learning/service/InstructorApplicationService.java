package com.example.e_learning.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.e_learning.dto.InstructorApplicationDTO;
import com.example.e_learning.entity.InstructorApplication;
import com.example.e_learning.entity.User;
import com.example.e_learning.repository.InstructorApplicationRepository;
import com.example.e_learning.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class InstructorApplicationService {

    @Autowired
    private InstructorApplicationRepository instructorRepo;

    @Autowired
    private UserRepository userRepo;

    @Transactional
    public void submitApplication(InstructorApplicationDTO dto, String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        InstructorApplication application = new InstructorApplication();
        application.setName(dto.getName());
        application.setEmail(dto.getEmail());
        application.setQualifications(dto.getQualifications());
        application.setExperience(dto.getExperience());
        application.setUser(user);
        application.setCourses(dto.getCourses());

        instructorRepo.save(application);
    }

    public List<InstructorApplicationDTO> getAllApplications() {
        return instructorRepo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private InstructorApplicationDTO toDTO(InstructorApplication application) {
        InstructorApplicationDTO dto = new InstructorApplicationDTO();
        dto.setId(application.getId());
        dto.setName(application.getName());
        dto.setEmail(application.getEmail());
        dto.setQualifications(application.getQualifications());
        dto.setExperience(application.getExperience());
        dto.setCourses(application.getCourses()); 

        return dto;
    }
}