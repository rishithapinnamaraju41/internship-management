package com.appticode.repository;

import com.appticode.entity.Certificate;
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
public interface CertificateRepository extends JpaRepository<Certificate, String> {
    
    Optional<Certificate> findByCertificateId(String certificateId);
    
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    
    Optional<Certificate> findByVerificationToken(String verificationToken);
    
    @Query("SELECT c FROM Certificate c WHERE c.intern.internId = :internId")
    List<Certificate> findByInternId(@Param("internId") String internId);
    
    List<Certificate> findByType(String type);
    
    List<Certificate> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT c FROM Certificate c WHERE c.validUntil < :date AND c.isVerified = true")
    List<Certificate> findExpiredCertificates(@Param("date") LocalDate date);
    
    @Query("SELECT c FROM Certificate c WHERE c.validUntil > :date AND c.isVerified = true")
    List<Certificate> findValidCertificates(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.issueDate BETWEEN :startDate AND :endDate")
    Long countCertificatesIssuedInRange(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    boolean existsByCertificateNumber(String certificateNumber);
    
    @Modifying
    @Transactional
    @Query("UPDATE Certificate c SET c.isVerified = true WHERE c.certificateId = :certificateId")
    void verifyCertificate(@Param("certificateId") String certificateId);
}