package com.appticode.repository;

import com.appticode.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    
    Optional<Attendance> findByAttendanceId(String attendanceId);
    
    @Query("SELECT a FROM Attendance a WHERE a.intern.internId = :internId")
    List<Attendance> findByInternId(@Param("internId") String internId);
    
    @Query("SELECT a FROM Attendance a WHERE a.intern.internId = :internId AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByInternIdAndDateBetween(@Param("internId") String internId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.intern.internId = :internId AND a.date = :date")
    Optional<Attendance> findByInternIdAndDate(@Param("internId") String internId, @Param("date") LocalDate date);
    
    List<Attendance> findByDate(LocalDate date);
    
    List<Attendance> findByStatus(String status);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.intern.internId = :internId AND a.status = 'PRESENT' AND a.date BETWEEN :startDate AND :endDate")
    Long countPresentDays(@Param("internId") String internId, 
                          @Param("startDate") LocalDate startDate, 
                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.intern.internId = :internId AND a.status = 'LATE' AND a.date BETWEEN :startDate AND :endDate")
    Long countLateDays(@Param("internId") String internId, 
                       @Param("startDate") LocalDate startDate, 
                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(a.workHours), 0) FROM Attendance a WHERE a.intern.internId = :internId AND a.date BETWEEN :startDate AND :endDate")
    Double getTotalWorkHours(@Param("internId") String internId, 
                             @Param("startDate") LocalDate startDate, 
                             @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(a.workHours) FROM Attendance a WHERE a.intern.internId = :internId")
    Double getAverageWorkHours(@Param("internId") String internId);
    
    @Query("SELECT a.date, COUNT(a) FROM Attendance a WHERE a.date BETWEEN :startDate AND :endDate GROUP BY a.date")
    List<Object[]> getDailyAttendanceSummary(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    @Modifying
    @Transactional
    @Query("UPDATE Attendance a SET a.checkOutTime = :checkOutTime, a.workHours = :workHours, a.status = :status WHERE a.attendanceId = :attendanceId")
    void updateCheckOut(@Param("attendanceId") String attendanceId, 
                        @Param("checkOutTime") LocalTime checkOutTime, 
                        @Param("workHours") Double workHours, 
                        @Param("status") String status);
    
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendance a WHERE a.intern.internId = :internId AND a.date = :date")
    boolean existsByInternIdAndDate(@Param("internId") String internId, @Param("date") LocalDate date);
}