package com.appticode.service;

import com.appticode.entity.Intern;
import com.appticode.entity.TaskSubmission;
import com.appticode.repository.InternRepository;
import com.appticode.repository.TaskSubmissionRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskSubmissionService {

    @Autowired
    private TaskSubmissionRepository taskSubmissionRepository;

    @Autowired
    private InternRepository internRepository;

    @Autowired
    private CustomIdGenerator idGenerator;

    public TaskSubmission submitTask(TaskSubmission taskSubmission) {
        taskSubmission.setSubmissionId(idGenerator.generateSubmissionId());
        taskSubmission.setSubmittedAt(LocalDateTime.now());
        taskSubmission.setStatus("SUBMITTED");
        taskSubmission.setCreatedAt(LocalDateTime.now());
        
        if (taskSubmission.getTask() != null && taskSubmission.getTask().getDueDate() != null) {
            if (LocalDate.now().isAfter(taskSubmission.getTask().getDueDate())) {
                taskSubmission.setIsLate(true);
                taskSubmission.setLateDays((int) ChronoUnit.DAYS.between(
                    taskSubmission.getTask().getDueDate(), LocalDate.now()));
            }
        }
        
        return taskSubmissionRepository.save(taskSubmission);
    }

    public Optional<TaskSubmission> gradeSubmission(String submissionId, Integer obtainedPoints, String feedback, Double rating) {
        Optional<TaskSubmission> submissionOpt = taskSubmissionRepository.findById(submissionId);
        if (submissionOpt.isPresent()) {
            TaskSubmission submission = submissionOpt.get();
            if (obtainedPoints != null) submission.setObtainedPoints(obtainedPoints);
            if (feedback != null) submission.setFeedback(feedback);
            if (rating != null) submission.setRating(rating);
            submission.setStatus("GRADED");
            submission.setGradedAt(LocalDateTime.now());
            submission.setUpdatedAt(LocalDateTime.now());
            return Optional.of(taskSubmissionRepository.save(submission));
        }
        return Optional.empty();
    }

    public Optional<TaskSubmission> getSubmissionById(String submissionId) {
        return taskSubmissionRepository.findById(submissionId);
    }

    public List<TaskSubmission> getSubmissionsByTask(String taskId) {
        return taskSubmissionRepository.findByTaskId(taskId);
    }

    public List<TaskSubmission> getSubmissionsByIntern(String internId) {
        return taskSubmissionRepository.findByInternId(internId);
    }

    public Optional<TaskSubmission> getSubmissionByTaskAndIntern(String taskId, String internId) {
        return taskSubmissionRepository.findByTaskIdAndInternId(taskId, internId);
    }

    public List<TaskSubmission> getPendingSubmissions() {
        return taskSubmissionRepository.findByStatus("SUBMITTED");
    }

    public List<TaskSubmission> getPendingSubmissionsByTask(String taskId) {
        return taskSubmissionRepository.findPendingSubmissionsByTask(taskId);
    }

    public List<TaskSubmission> getSubmissionsByStatus(String status) {
        return taskSubmissionRepository.findByStatus(status);
    }

    public Double getInternAverageScore(String internId) {
        return taskSubmissionRepository.getAverageScoreByIntern(internId);
    }

    public Double getAverageRatingByTask(String taskId) {
        return taskSubmissionRepository.getAverageRatingByTask(taskId);
    }

    public List<Object[]> getTopPerformingInterns(Pageable pageable) {
        return taskSubmissionRepository.getTopPerformingInterns(pageable);
    }

    public Long countSubmissionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return taskSubmissionRepository.countSubmissionsByDateRange(startDate, endDate);
    }

    public boolean updateSubmissionStatus(String submissionId, String status) {
        try {
            taskSubmissionRepository.updateSubmissionStatus(submissionId, status);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean gradeSubmissionDirect(String submissionId, Integer points, String feedback, Double rating) {
        try {
            taskSubmissionRepository.gradeSubmission(submissionId, points, feedback, rating);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean existsByTaskAndIntern(String taskId, String internId) {
        return taskSubmissionRepository.findByTaskIdAndInternId(taskId, internId).isPresent();
    }

    public void updateInternAverageRating(String internId) {
        Double avgRating = taskSubmissionRepository.getAverageScoreByIntern(internId);
        if (avgRating != null) {
            Optional<Intern> internOpt = internRepository.findById(internId);
            if (internOpt.isPresent()) {
                Intern intern = internOpt.get();
                intern.setAverageRating(avgRating);
                
                Double totalScore = taskSubmissionRepository.findByInternId(internId).stream()
                    .mapToDouble(ts -> ts.getObtainedPoints() != null ? ts.getObtainedPoints() : 0)
                    .sum();
                intern.setTotalScore(totalScore);
                
                internRepository.save(intern);
            }
        }
    }
}