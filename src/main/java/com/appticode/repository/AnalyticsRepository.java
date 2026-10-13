package com.appticode.repository;

import com.appticode.entity.AIAnalytics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<AIAnalytics, String> {
    
    Optional<AIAnalytics> findByAnalyticsId(String analyticsId);
    
    @Query("SELECT a FROM AIAnalytics a WHERE a.intern.internId = :internId")
    List<AIAnalytics> findByInternId(@Param("internId") String internId);
    
    List<AIAnalytics> findByAnalysisType(String analysisType);
    
    @Query("SELECT a FROM AIAnalytics a WHERE a.intern.internId = :internId ORDER BY a.analyzedAt DESC")
    List<AIAnalytics> findLatestByInternId(@Param("internId") String internId, Pageable pageable);
    
    @Query("SELECT a FROM AIAnalytics a WHERE a.analysisType = :type AND a.confidenceScore > :minScore")
    List<AIAnalytics> findHighConfidenceAnalytics(@Param("type") String type, 
                                                   @Param("minScore") Double minScore);
    
    @Query("SELECT a FROM AIAnalytics a WHERE a.analyzedAt > :dateTime")
    List<AIAnalytics> findRecentAnalytics(@Param("dateTime") LocalDateTime dateTime);
}