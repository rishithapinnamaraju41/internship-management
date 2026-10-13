package com.appticode.service;

import com.appticode.entity.Certificate;
import com.appticode.repository.CertificateRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CustomIdGenerator idGenerator;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public Certificate generateCertificate(Certificate certificate) {
        certificate.setCertificateId(idGenerator.generateCertificateId());
        certificate.setCertificateNumber(generateCertificateNumber());
        certificate.setIssueDate(LocalDate.now());
        certificate.setIsVerified(false);
        certificate.setVerificationToken(UUID.randomUUID().toString());
        certificate.setCreatedAt(LocalDateTime.now());
        
        return certificateRepository.save(certificate);
    }

    public Optional<Certificate> getCertificateById(String certificateId) {
        return certificateRepository.findById(certificateId);
    }

    public Optional<Certificate> getCertificateByNumber(String certificateNumber) {
        return certificateRepository.findByCertificateNumber(certificateNumber);
    }

    public Optional<Certificate> getCertificateByVerificationToken(String verificationToken) {
        return certificateRepository.findByVerificationToken(verificationToken);
    }

    public List<Certificate> getCertificatesByIntern(String internId) {
        return certificateRepository.findByInternId(internId);
    }

    public List<Certificate> getCertificatesByType(String type) {
        return certificateRepository.findByType(type);
    }

    public List<Certificate> getCertificatesByDateRange(LocalDate startDate, LocalDate endDate) {
        return certificateRepository.findByIssueDateBetween(startDate, endDate);
    }

    public List<Certificate> getExpiredCertificates() {
        return certificateRepository.findExpiredCertificates(LocalDate.now());
    }

    public List<Certificate> getValidCertificates() {
        return certificateRepository.findValidCertificates(LocalDate.now());
    }

    public boolean verifyCertificate(String verificationToken) {
        Optional<Certificate> certificateOpt = certificateRepository.findByVerificationToken(verificationToken);
        if (certificateOpt.isPresent()) {
            Certificate certificate = certificateOpt.get();
            certificate.setIsVerified(true);
            certificate.setUpdatedAt(LocalDateTime.now());
            certificateRepository.save(certificate);
            return true;
        }
        return false;
    }

    public boolean verifyCertificateById(String certificateId) {
        try {
            certificateRepository.verifyCertificate(certificateId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteCertificate(String certificateId) {
        Optional<Certificate> certificateOpt = certificateRepository.findById(certificateId);
        if (certificateOpt.isPresent()) {
            certificateRepository.delete(certificateOpt.get());
            return true;
        }
        return false;
    }

    public boolean existsByCertificateNumber(String certificateNumber) {
        return certificateRepository.existsByCertificateNumber(certificateNumber);
    }

    public Long countCertificatesIssuedInRange(LocalDate startDate, LocalDate endDate) {
        return certificateRepository.countCertificatesIssuedInRange(startDate, endDate);
    }

    public String getVerificationUrl(String verificationToken) {
        return baseUrl + "/api/certificates/verify/" + verificationToken;
    }

    private String generateCertificateNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = certificateRepository.count() + 1;
        String sequence = String.format("%06d", count);
        return "CERT-" + year + "-" + sequence;
    }
}