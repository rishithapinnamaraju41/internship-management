package com.appticode.controller;

import com.appticode.entity.Certificate;
import com.appticode.entity.Intern;
import com.appticode.service.CertificateService;
import com.appticode.service.InternService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/certificates")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private InternService internService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> generateCertificate(@RequestBody Certificate certificate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (certificate.getIntern() == null || certificate.getIntern().getInternId() == null) {
                response.put("success", false);
                response.put("message", "Intern information is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            Optional<Intern> internOpt = internService.getInternById(certificate.getIntern().getInternId());
            if (!internOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Intern not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            if (certificate.getType() == null || certificate.getType().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Certificate type is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Check if certificate already exists for this intern and type
            List<Certificate> existingCertificates = certificateService.getCertificatesByIntern(internOpt.get().getInternId());
            boolean exists = existingCertificates.stream()
                .anyMatch(c -> c.getType().equals(certificate.getType()));
            
            if (exists) {
                response.put("success", false);
                response.put("message", "Certificate of this type already exists for this intern");
                response.put("statusCode", HttpStatus.CONFLICT.value());
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            certificate.setIntern(internOpt.get());
            Certificate createdCertificate = certificateService.generateCertificate(certificate);
            
            response.put("success", true);
            response.put("message", "Certificate generated successfully");
            response.put("data", createdCertificate);
            response.put("verificationUrl", certificateService.getVerificationUrl(createdCertificate.getVerificationToken()));
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to generate certificate: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{certificateId}")
    public ResponseEntity<Map<String, Object>> getCertificateById(@PathVariable String certificateId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Certificate> certificateOpt = certificateService.getCertificateById(certificateId);
            
            if (certificateOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Certificate retrieved successfully");
                response.put("data", certificateOpt.get());
                response.put("verificationUrl", certificateService.getVerificationUrl(certificateOpt.get().getVerificationToken()));
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Certificate not found with id: " + certificateId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve certificate: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/number/{certificateNumber}")
    public ResponseEntity<Map<String, Object>> getCertificateByNumber(@PathVariable String certificateNumber) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Certificate> certificateOpt = certificateService.getCertificateByNumber(certificateNumber);
            
            if (certificateOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Certificate retrieved successfully");
                response.put("data", certificateOpt.get());
                response.put("verificationUrl", certificateService.getVerificationUrl(certificateOpt.get().getVerificationToken()));
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Certificate not found with number: " + certificateNumber);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve certificate: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}")
    public ResponseEntity<Map<String, Object>> getCertificatesByIntern(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Certificate> certificates = certificateService.getCertificatesByIntern(internId);
            response.put("success", true);
            response.put("message", "Certificates retrieved successfully for intern: " + internId);
            response.put("data", certificates);
            response.put("totalCount", certificates.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve certificates: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> getCertificatesByType(@PathVariable String type) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Certificate> certificates = certificateService.getCertificatesByType(type);
            response.put("success", true);
            response.put("message", "Certificates retrieved successfully for type: " + type);
            response.put("data", certificates);
            response.put("totalCount", certificates.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve certificates: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> getCertificatesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Certificate> certificates = certificateService.getCertificatesByDateRange(startDate, endDate);
            response.put("success", true);
            response.put("message", "Certificates retrieved successfully");
            response.put("data", certificates);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalCount", certificates.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve certificates: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/verify/{verificationToken}")
    public ResponseEntity<Map<String, Object>> verifyCertificate(@PathVariable String verificationToken) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean verified = certificateService.verifyCertificate(verificationToken);
            
            if (verified) {
                Optional<Certificate> certificateOpt = certificateService.getCertificateByVerificationToken(verificationToken);
                response.put("success", true);
                response.put("message", "Certificate verified successfully");
                response.put("data", certificateOpt.orElse(null));
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid verification token");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to verify certificate: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download/{certificateId}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String certificateId) {
        try {
            Optional<Certificate> certificateOpt = certificateService.getCertificateById(certificateId);
            
            if (!certificateOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            byte[] pdfContent = generateCertificatePDF(certificateOpt.get());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate_" + certificateId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{certificateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCertificate(@PathVariable String certificateId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = certificateService.deleteCertificate(certificateId);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Certificate deleted successfully");
                response.put("certificateId", certificateId);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Certificate not found with id: " + certificateId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete certificate: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> getExpiredCertificates() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Certificate> certificates = certificateService.getExpiredCertificates();
            response.put("success", true);
            response.put("message", "Expired certificates retrieved successfully");
            response.put("data", certificates);
            response.put("totalCount", certificates.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve expired certificates: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/valid")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> getValidCertificates() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Certificate> certificates = certificateService.getValidCertificates();
            response.put("success", true);
            response.put("message", "Valid certificates retrieved successfully");
            response.put("data", certificates);
            response.put("totalCount", certificates.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve valid certificates: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Map<String, Object>> getCertificatesSummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Certificate> allCertificates = certificateService.getCertificatesByIntern("all");
            List<Certificate> expiredCertificates = certificateService.getExpiredCertificates();
            List<Certificate> validCertificates = certificateService.getValidCertificates();
            
            long totalCertificates = certificateService.getCertificatesByIntern("all") != null ? 
                certificateService.getCertificatesByIntern("all").size() : 0;
            long expiredCount = expiredCertificates != null ? expiredCertificates.size() : 0;
            long validCount = validCertificates != null ? validCertificates.size() : 0;
            
            response.put("success", true);
            response.put("message", "Certificates summary retrieved successfully");
            response.put("totalCertificates", totalCertificates);
            response.put("expiredCertificates", expiredCount);
            response.put("validCertificates", validCount);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get certificates summary: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private byte[] generateCertificatePDF(Certificate certificate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        Paragraph title = new Paragraph("CERTIFICATE OF COMPLETION", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
        document.add(new Paragraph("This certificate is presented to", contentFont));
        document.add(new Paragraph(" "));
        
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Paragraph name = new Paragraph(certificate.getIntern().getUser().getFirstName() + " " + 
                                      certificate.getIntern().getUser().getLastName(), nameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        document.add(name);
        document.add(new Paragraph(" "));
        
        document.add(new Paragraph("For successfully completing the internship program", contentFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Certificate Number: " + certificate.getCertificateNumber(), contentFont));
        document.add(new Paragraph("Issue Date: " + certificate.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), contentFont));
        
        if (certificate.getValidUntil() != null) {
            document.add(new Paragraph("Valid Until: " + certificate.getValidUntil().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), contentFont));
        }
        
        if (certificate.getGrade() != null) {
            document.add(new Paragraph("Grade: " + certificate.getGrade(), contentFont));
        }

        document.close();
        return out.toByteArray();
    }
}