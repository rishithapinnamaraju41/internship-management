package com.appticode.repository;

import com.appticode.entity.Batch;
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
public interface BatchRepository extends JpaRepository<Batch, String> {
    
    Optional<Batch> findByBatchId(String batchId);
    
    Optional<Batch> findByBatchCode(String batchCode);
    
    List<Batch> findByBatchNameContaining(String batchName);
    
    List<Batch> findByStartDateAfter(LocalDate date);
    
    List<Batch> findByEndDateBefore(LocalDate date);
    
    @Query("SELECT b FROM Batch b WHERE b.startDate <= :date AND b.endDate >= :date")
    List<Batch> findActiveBatches(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Batch b WHERE b.startDate > :date")
    List<Batch> findUpcomingBatches(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Batch b WHERE b.currentCount < b.maxCapacity")
    List<Batch> findBatchesWithCapacity();
    
    @Modifying
    @Transactional
    @Query("UPDATE Batch b SET b.currentCount = b.currentCount + 1 WHERE b.batchId = :batchId")
    void incrementCurrentCount(@Param("batchId") String batchId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Batch b SET b.currentCount = b.currentCount - 1 WHERE b.batchId = :batchId")
    void decrementCurrentCount(@Param("batchId") String batchId);
    
    @Query("SELECT b FROM Batch b ORDER BY b.startDate DESC")
    Page<Batch> findAllOrderByStartDateDesc(Pageable pageable);
    
    boolean existsByBatchCode(String batchCode);
}