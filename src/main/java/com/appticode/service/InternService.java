package com.appticode.service;

import com.appticode.entity.Batch;
import com.appticode.entity.Intern;
import com.appticode.entity.Mentor;
import com.appticode.repository.AttendanceRepository;
import com.appticode.repository.BatchRepository;
import com.appticode.repository.InternRepository;
import com.appticode.repository.MentorRepository;
import com.appticode.repository.TaskSubmissionRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InternService {

    @Autowired
    private InternRepository internRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private TaskSubmissionRepository taskSubmissionRepository;

    @Autowired
    private CustomIdGenerator idGenerator;

    public Intern createIntern(Intern intern) {
        intern.setInternId(idGenerator.generateInternId());
        intern.setStatus("ACTIVE");
        intern.setTotalScore(0.0);
        intern.setAverageRating(0.0);
        intern.setCreatedAt(LocalDateTime.now());
        
        if (intern.getBatch() != null && intern.getBatch().getBatchId() != null) {
            Optional<Batch> batchOpt = batchRepository.findById(intern.getBatch().getBatchId());
            if (batchOpt.isPresent()) {
                intern.setBatch(batchOpt.get());
                batchRepository.incrementCurrentCount(batchOpt.get().getBatchId());
            }
        }
        
        return internRepository.save(intern);
    }

    public Intern updateIntern(String internId, Intern internDetails) {
        Optional<Intern> internOpt = internRepository.findById(internId);
        if (internOpt.isPresent()) {
            Intern intern = internOpt.get();
            if (internDetails.getDepartment() != null) intern.setDepartment(internDetails.getDepartment());
            if (internDetails.getUniversity() != null) intern.setUniversity(internDetails.getUniversity());
            if (internDetails.getDegree() != null) intern.setDegree(internDetails.getDegree());
            if (internDetails.getGraduationYear() != null) intern.setGraduationYear(internDetails.getGraduationYear());
            if (internDetails.getStartDate() != null) intern.setStartDate(internDetails.getStartDate());
            if (internDetails.getEndDate() != null) intern.setEndDate(internDetails.getEndDate());
            intern.setUpdatedAt(LocalDateTime.now());
            return internRepository.save(intern);
        }
        return null;
    }

    public Optional<Intern> getInternById(String internId) {
        return internRepository.findById(internId);
    }

    public Optional<Intern> getInternByUserId(String userId) {
        return internRepository.findByUserId(userId);
    }

    public List<Intern> getAllInterns() {
        return internRepository.findAll();
    }

    public Page<Intern> getAllInterns(Pageable pageable) {
        return internRepository.findAll(pageable);
    }

    public List<Intern> getInternsByBatch(String batchId) {
        return internRepository.findByBatchId(batchId);
    }

    public List<Intern> getInternsByMentor(String mentorId) {
        return internRepository.findByMentorId(mentorId);
    }

    public List<Intern> getInternsByStatus(String status) {
        return internRepository.findByStatus(status);
    }

    public List<Intern> getInternsByDepartment(String department) {
        return internRepository.findByDepartment(department);
    }

    public boolean assignMentor(String internId, String mentorId) {
        Optional<Intern> internOpt = internRepository.findById(internId);
        Optional<Mentor> mentorOpt = mentorRepository.findById(mentorId);
        
        if (internOpt.isPresent() && mentorOpt.isPresent()) {
            Intern intern = internOpt.get();
            Mentor mentor = mentorOpt.get();
            
            if (mentor.getCurrentInterns() < mentor.getMaxInterns()) {
                intern.setMentor(mentor);
                internRepository.save(intern);
                mentorRepository.incrementInternCount(mentorId);
                return true;
            }
        }
        return false;
    }

    public boolean updateInternStatus(String internId, String status) {
        Optional<Intern> internOpt = internRepository.findById(internId);
        if (internOpt.isPresent()) {
            Intern intern = internOpt.get();
            intern.setStatus(status);
            intern.setUpdatedAt(LocalDateTime.now());
            internRepository.save(intern);
            return true;
        }
        return false;
    }

    public boolean calculateAverageRating(String internId) {
        Optional<Intern> internOpt = internRepository.findById(internId);
        if (internOpt.isPresent()) {
            Double avgRating = taskSubmissionRepository.getAverageScoreByIntern(internId);
            if (avgRating != null) {
                Intern intern = internOpt.get();
                intern.setAverageRating(avgRating);
                internRepository.save(intern);
                return true;
            }
        }
        return false;
    }

    public Double calculateAttendancePercentage(String internId) {
        Optional<Intern> internOpt = internRepository.findById(internId);
        if (internOpt.isPresent()) {
            Intern intern = internOpt.get();
            LocalDate startDate = intern.getStartDate();
            LocalDate endDate = LocalDate.now();
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            long presentDays = attendanceRepository.countPresentDays(internId, startDate, endDate);
            return totalDays > 0 ? (presentDays * 100.0) / totalDays : 0.0;
        }
        return 0.0;
    }

    public boolean deleteIntern(String internId) {
        Optional<Intern> internOpt = internRepository.findById(internId);
        if (internOpt.isPresent()) {
            Intern intern = internOpt.get();
            if (intern.getBatch() != null) {
                batchRepository.decrementCurrentCount(intern.getBatch().getBatchId());
            }
            if (intern.getMentor() != null) {
                mentorRepository.decrementInternCount(intern.getMentor().getMentorId());
            }
            internRepository.delete(intern);
            return true;
        }
        return false;
    }
}