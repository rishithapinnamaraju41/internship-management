package com.appticode.repository;

import com.appticode.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    
    Optional<Task> findByTaskId(String taskId);
    
    @Query("SELECT t FROM Task t WHERE t.batch.batchId = :batchId")
    List<Task> findByBatchId(@Param("batchId") String batchId);
    
    @Query("SELECT t FROM Task t WHERE t.assignedBy.mentorId = :mentorId")
    List<Task> findByAssignedByMentorId(@Param("mentorId") String mentorId);
    
    List<Task> findByStatus(String status);
    
    List<Task> findByPriority(String priority);
    
    @Query("SELECT t FROM Task t WHERE t.batch.batchId = :batchId")
    Page<Task> findByBatchId(@Param("batchId") String batchId, Pageable pageable);
    
    Page<Task> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("date") LocalDate date);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findTasksByDueDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate = :date")
    List<Task> findTasksDueOn(@Param("date") LocalDate date);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status != 'COMPLETED'")
    List<Task> findUpcomingTasks(@Param("startDate") LocalDate startDate, 
                                  @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(t.totalPoints) FROM Task t WHERE t.batch.batchId = :batchId")
    Double getAverageTaskPoints(@Param("batchId") String batchId);
    
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();
    
    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countTasksByPriority();
    
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = :status WHERE t.taskId = :taskId")
    void updateTaskStatus(@Param("taskId") String taskId, @Param("status") String status);
}