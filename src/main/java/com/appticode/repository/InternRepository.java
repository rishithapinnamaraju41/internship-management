package com.appticode.repository;

import com.appticode.entity.Intern;
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
public interface InternRepository extends JpaRepository<Intern, String> {
    
    @Query("SELECT i FROM Intern i WHERE i.user.userId = :userId")
    Optional<Intern> findByUserId(@Param("userId") String userId);
    
    Optional<Intern> findByInternId(String internId);
    
    @Query("SELECT i FROM Intern i WHERE i.batch.batchId = :batchId")
    List<Intern> findByBatchId(@Param("batchId") String batchId);
    
    @Query("SELECT i FROM Intern i WHERE i.mentor.mentorId = :mentorId")
    List<Intern> findByMentorId(@Param("mentorId") String mentorId);
    
    List<Intern> findByStatus(String status);
    
    List<Intern> findByDepartment(String department);
    
    Page<Intern> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT i FROM Intern i WHERE i.batch.batchId = :batchId")
    Page<Intern> findByBatchId(@Param("batchId") String batchId, Pageable pageable);
    
    @Query("SELECT i FROM Intern i WHERE i.mentor.mentorId = :mentorId")
    Page<Intern> findByMentorId(@Param("mentorId") String mentorId, Pageable pageable);
    
    @Query("SELECT i FROM Intern i WHERE i.startDate <= :date AND i.endDate >= :date")
    List<Intern> findActiveInterns(@Param("date") LocalDate date);
    
    @Query("SELECT i FROM Intern i WHERE i.startDate > :date")
    List<Intern> findUpcomingInterns(@Param("date") LocalDate date);
    
    @Query("SELECT i FROM Intern i WHERE i.endDate < :date AND i.status = 'ACTIVE'")
    List<Intern> findInternsWithExpiredEndDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(i) FROM Intern i WHERE i.batch.batchId = :batchId")
    Long countByBatchId(@Param("batchId") String batchId);
    
    @Query("SELECT AVG(i.averageRating) FROM Intern i WHERE i.mentor.mentorId = :mentorId")
    Double getAverageRatingByMentor(@Param("mentorId") String mentorId);
    
    @Query("SELECT i.department, COUNT(i) FROM Intern i GROUP BY i.department")
    List<Object[]> countInternsByDepartment();
    
    @Query("SELECT i.status, COUNT(i) FROM Intern i GROUP BY i.status")
    List<Object[]> countInternsByStatus();
    
    @Modifying
    @Transactional
    @Query("UPDATE Intern i SET i.status = :status WHERE i.internId = :internId")
    void updateInternStatus(@Param("internId") String internId, @Param("status") String status);
    
    @Modifying
    @Transactional
    @Query("UPDATE Intern i SET i.mentor.mentorId = :mentorId WHERE i.internId = :internId")
    void assignMentor(@Param("internId") String internId, @Param("mentorId") String mentorId);
    
    @Query("SELECT i FROM Intern i WHERE i.university = :university")
    List<Intern> findByUniversity(@Param("university") String university);
    
    @Query("SELECT i FROM Intern i WHERE i.graduationYear = :year")
    List<Intern> findByGraduationYear(@Param("year") Integer year);
    
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Intern i WHERE i.user.userId = :userId")
    boolean existsByUserId(@Param("userId") String userId);
    
    boolean existsByInternId(String internId);
}