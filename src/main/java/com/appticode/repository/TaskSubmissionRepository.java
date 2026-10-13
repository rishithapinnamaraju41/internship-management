package com.appticode.repository;

import com.appticode.entity.TaskSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, String> {
    
    Optional<TaskSubmission> findBySubmissionId(String submissionId);
    
    @Query("SELECT ts FROM TaskSubmission ts WHERE ts.task.taskId = :taskId")
    List<TaskSubmission> findByTaskId(@Param("taskId") String taskId);
    
    @Query("SELECT ts FROM TaskSubmission ts WHERE ts.intern.internId = :internId")
    List<TaskSubmission> findByInternId(@Param("internId") String internId);
    
    List<TaskSubmission> findByStatus(String status);
    
    @Query("SELECT ts FROM TaskSubmission ts WHERE ts.task.taskId = :taskId")
    Page<TaskSubmission> findByTaskId(@Param("taskId") String taskId, Pageable pageable);
    
    @Query("SELECT ts FROM TaskSubmission ts WHERE ts.intern.internId = :internId")
    Page<TaskSubmission> findByInternId(@Param("internId") String internId, Pageable pageable);
    
    @Query("SELECT ts FROM TaskSubmission ts WHERE ts.task.taskId = :taskId AND ts.status = 'SUBMITTED'")
    List<TaskSubmission> findPendingSubmissionsByTask(@Param("taskId") String taskId);
    
    @Query("SELECT AVG(ts.obtainedPoints) FROM TaskSubmission ts WHERE ts.intern.internId = :internId")
    Double getAverageScoreByIntern(@Param("internId") String internId);
    
    @Query("SELECT AVG(ts.rating) FROM TaskSubmission ts WHERE ts.task.taskId = :taskId")
    Double getAverageRatingByTask(@Param("taskId") String taskId);
    
    @Query("SELECT ts.intern.internId, AVG(ts.obtainedPoints) FROM TaskSubmission ts GROUP BY ts.intern.internId ORDER BY AVG(ts.obtainedPoints) DESC")
    List<Object[]> getTopPerformingInterns(Pageable pageable);
    
    @Query("SELECT COUNT(ts) FROM TaskSubmission ts WHERE ts.submittedAt BETWEEN :startDate AND :endDate")
    Long countSubmissionsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    @Modifying
    @Transactional
    @Query("UPDATE TaskSubmission ts SET ts.status = :status, ts.gradedAt = CURRENT_TIMESTAMP WHERE ts.submissionId = :submissionId")
    void updateSubmissionStatus(@Param("submissionId") String submissionId, @Param("status") String status);
    
    @Modifying
    @Transactional
    @Query("UPDATE TaskSubmission ts SET ts.obtainedPoints = :points, ts.feedback = :feedback, ts.rating = :rating, ts.status = 'GRADED', ts.gradedAt = CURRENT_TIMESTAMP WHERE ts.submissionId = :submissionId")
    void gradeSubmission(@Param("submissionId") String submissionId, 
                         @Param("points") Integer points, 
                         @Param("feedback") String feedback, 
                         @Param("rating") Double rating);
    
    // Fixed: Use @Query to navigate through task.taskId and intern.internId
    @Query("SELECT ts FROM TaskSubmission ts WHERE ts.task.taskId = :taskId AND ts.intern.internId = :internId")
    Optional<TaskSubmission> findByTaskIdAndInternId(@Param("taskId") String taskId, @Param("internId") String internId);
}