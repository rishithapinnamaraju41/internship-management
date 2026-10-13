package com.appticode.repository;

import com.appticode.entity.Mentor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, String> {
    
    @Query("SELECT m FROM Mentor m WHERE m.user.userId = :userId")
    Mentor findByUserId(@Param("userId") String userId);
    
    Mentor findByMentorId(String mentorId);
    
    Mentor findByEmployeeId(String employeeId);
    
    List<Mentor> findByDepartment(String department);
    
    List<Mentor> findBySpecialization(String specialization);
    
    Page<Mentor> findByDepartment(String department, Pageable pageable);
    
    @Query("SELECT m FROM Mentor m WHERE m.currentInterns < m.maxInterns")
    List<Mentor> findAvailableMentors();
    
    @Query("SELECT m FROM Mentor m WHERE m.currentInterns < m.maxInterns ORDER BY m.currentInterns ASC")
    Page<Mentor> findAvailableMentors(Pageable pageable);
    
    List<Mentor> findByAverageRatingGreaterThan(Double rating);
    
    @Query("SELECT m FROM Mentor m ORDER BY m.averageRating DESC")
    List<Mentor> findTopRatedMentors(Pageable pageable);
    
    @Query("SELECT AVG(m.averageRating) FROM Mentor m WHERE m.department = :department")
    Double getAverageRatingByDepartment(@Param("department") String department);
    
    @Modifying
    @Transactional
    @Query("UPDATE Mentor m SET m.currentInterns = m.currentInterns + 1 WHERE m.mentorId = :mentorId")
    void incrementInternCount(@Param("mentorId") String mentorId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Mentor m SET m.currentInterns = m.currentInterns - 1 WHERE m.mentorId = :mentorId")
    void decrementInternCount(@Param("mentorId") String mentorId);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Mentor m WHERE m.user.userId = :userId")
    boolean existsByUserId(@Param("userId") String userId);
    
    @Query("SELECT m.department, COUNT(m) FROM Mentor m GROUP BY m.department")
    List<Object[]> countMentorsByDepartment();
    
    Mentor findTopByOrderByMentorIdDesc();

	List<Mentor> findByUser_UserId(String userId);
}