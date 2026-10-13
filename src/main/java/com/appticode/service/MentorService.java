package com.appticode.service;

import com.appticode.entity.Mentor;
import com.appticode.entity.User;
import com.appticode.repository.MentorRepository;
import com.appticode.repository.UserRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MentorService {

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomIdGenerator idGenerator;

    public Mentor createMentor(Mentor mentor) {
        mentor.setMentorId(idGenerator.generateMentorId());
        mentor.setCurrentInterns(0);
        mentor.setAverageRating(0.0);
        mentor.setCreatedAt(LocalDateTime.now());
        return mentorRepository.save(mentor);
    }

    public Mentor updateMentor(String mentorId, Mentor mentorDetails) {
        Mentor mentor = mentorRepository.findByMentorId(mentorId);
        if (mentor != null) {
            if (mentorDetails.getEmployeeId() != null) mentor.setEmployeeId(mentorDetails.getEmployeeId());
            if (mentorDetails.getDepartment() != null) mentor.setDepartment(mentorDetails.getDepartment());
            if (mentorDetails.getDesignation() != null) mentor.setDesignation(mentorDetails.getDesignation());
            if (mentorDetails.getExperienceYears() != null) mentor.setExperienceYears(mentorDetails.getExperienceYears());
            if (mentorDetails.getSpecialization() != null) mentor.setSpecialization(mentorDetails.getSpecialization());
            if (mentorDetails.getMaxInterns() != null) mentor.setMaxInterns(mentorDetails.getMaxInterns());
            mentor.setUpdatedAt(LocalDateTime.now());
            return mentorRepository.save(mentor);
        }
        return null;
    }

    public Mentor getMentorById(String mentorId) {
        return mentorRepository.findByMentorId(mentorId);
    }

    public List<Mentor> getMentorsByUserId(String userId) {
        return mentorRepository.findByUser_UserId(userId);
    }

    public Mentor getMentorByEmployeeId(String employeeId) {
        return mentorRepository.findByEmployeeId(employeeId);
    }

    public List<Mentor> getAllMentors() {
        return mentorRepository.findAll();
    }

    public Page<Mentor> getAllMentors(Pageable pageable) {
        return mentorRepository.findAll(pageable);
    }

    public List<Mentor> getAvailableMentors() {
        return mentorRepository.findAvailableMentors();
    }

    public Page<Mentor> getAvailableMentors(Pageable pageable) {
        return mentorRepository.findAvailableMentors(pageable);
    }

    public List<Mentor> getMentorsByDepartment(String department) {
        return mentorRepository.findByDepartment(department);
    }

    public List<Mentor> getMentorsBySpecialization(String specialization) {
        return mentorRepository.findBySpecialization(specialization);
    }

    public List<Mentor> getTopRatedMentors(Pageable pageable) {
        return mentorRepository.findTopRatedMentors(pageable);
    }

    public boolean deleteMentor(String mentorId) {
        Mentor mentor = mentorRepository.findByMentorId(mentorId);
        if (mentor != null && mentor.getCurrentInterns() == 0) {
            mentorRepository.delete(mentor);
            return true;
        }
        return false;
    }

    public boolean updateAverageRating(String mentorId) {
        Mentor mentor = mentorRepository.findByMentorId(mentorId);
        if (mentor != null) {
            return true;
        }
        return false;
    }

    public void incrementInternCount(String mentorId) {
        mentorRepository.incrementInternCount(mentorId);
    }

    public void decrementInternCount(String mentorId) {
        mentorRepository.decrementInternCount(mentorId);
    }

    public boolean existsByUserId(String userId) {
        return mentorRepository.existsByUserId(userId);
    }

    public Double getAverageRatingByDepartment(String department) {
        return mentorRepository.getAverageRatingByDepartment(department);
    }

    public List<Object[]> countMentorsByDepartment() {
        return mentorRepository.countMentorsByDepartment();
    }

    public List<Mentor> getMentorsByRatingGreaterThan(Double rating) {
        return mentorRepository.findByAverageRatingGreaterThan(rating);
    }
}