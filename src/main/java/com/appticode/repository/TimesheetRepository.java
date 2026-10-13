package com.appticode.repository;

import com.appticode.entity.Timesheet;
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
public interface TimesheetRepository extends JpaRepository<Timesheet, String> {
    
	Timesheet findByTimesheetId(String timesheetId);
    
    @Query("SELECT t FROM Timesheet t WHERE t.intern.internId = :internId")
    List<Timesheet> findByInternId(@Param("internId") String internId);
    
    @Query("SELECT t FROM Timesheet t WHERE t.intern.internId = :internId AND t.date BETWEEN :startDate AND :endDate")
    List<Timesheet> findByInternIdAndDateBetween(@Param("internId") String internId, 
                                                  @Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Timesheet t WHERE t.intern.internId = :internId AND t.date = :date")
    Timesheet findByInternIdAndDate(
            @Param("internId") String internId,
            @Param("date") LocalDate date);
    
    List<Timesheet> findByDate(LocalDate date);
    
    List<Timesheet> findByStatus(String status);
    
    @Query("SELECT t FROM Timesheet t WHERE t.status = 'PENDING'")
    List<Timesheet> findPendingTimesheets();
    
    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0) FROM Timesheet t WHERE t.intern.internId = :internId AND t.date BETWEEN :startDate AND :endDate AND t.status = 'APPROVED'")
    Double getTotalApprovedHours(@Param("internId") String internId, 
                                  @Param("startDate") LocalDate startDate, 
                                  @Param("endDate") LocalDate endDate);
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Timesheet t WHERE t.intern.internId = :internId AND t.date = :date")
    boolean existsByInternIdAndDate(@Param("internId") String internId, @Param("date") LocalDate date);
    
    @Modifying
    @Transactional
    @Query("""
    UPDATE Timesheet t
    SET t.status = :status,
        t.approvedAt = CURRENT_TIMESTAMP
    WHERE t.timesheetId = :timesheetId
    """)
    void updateTimesheetStatus(
            @Param("timesheetId") String timesheetId,
            @Param("status") String status);
}