package com.appticode.controller;

import com.appticode.entity.Attendance;
import com.appticode.entity.Intern;
import com.appticode.service.AttendanceService;
import com.appticode.service.InternService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private InternService internService;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('INTERN')")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody Attendance attendance) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (attendance.getIntern() == null || attendance.getIntern().getInternId() == null) {
                response.put("success", false);
                response.put("message", "Intern information is required");
                response.put("statusCode", HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            Optional<Intern> internOpt = internService.getInternById(attendance.getIntern().getInternId());
            if (!internOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Intern not found");
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            LocalDate today = LocalDate.now();
            if (attendanceService.existsByInternIdAndDate(attendance.getIntern().getInternId(), today)) {
                response.put("success", false);
                response.put("message", "Attendance already marked for today");
                response.put("statusCode", HttpStatus.CONFLICT.value());
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            attendance.setIntern(internOpt.get());
            Attendance createdAttendance = attendanceService.checkIn(attendance);
            
            response.put("success", true);
            response.put("message", "Check-in successful");
            response.put("data", createdAttendance);
            response.put("checkInTime", createdAttendance.getCheckInTime());
            response.put("status", createdAttendance.getStatus());
            if ("LATE".equals(createdAttendance.getStatus())) {
                response.put("lateMinutes", createdAttendance.getLateMinutes());
                response.put("warning", "You are late by " + createdAttendance.getLateMinutes() + " minutes");
            }
            response.put("statusCode", HttpStatus.CREATED.value());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to check-in: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasRole('INTERN')")
    public ResponseEntity<Map<String, Object>> checkOut(
            @RequestParam String attendanceId,
            @RequestParam String checkOutMethod,
            @RequestParam(required = false) String location) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Attendance> attendanceOpt = attendanceService.checkOut(attendanceId, checkOutMethod, location);
            
            if (attendanceOpt.isPresent()) {
                Attendance attendance = attendanceOpt.get();
                response.put("success", true);
                response.put("message", "Check-out successful");
                response.put("data", attendance);
                response.put("checkInTime", attendance.getCheckInTime());
                response.put("checkOutTime", attendance.getCheckOutTime());
                response.put("workHours", attendance.getWorkHours());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Attendance record not found with id: " + attendanceId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to check-out: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{attendanceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('INTERN')")
    public ResponseEntity<Map<String, Object>> getAttendanceById(@PathVariable String attendanceId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Attendance> attendanceOpt = attendanceService.getAttendanceById(attendanceId);
            
            if (attendanceOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Attendance retrieved successfully");
                response.put("data", attendanceOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Attendance not found with id: " + attendanceId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve attendance: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('INTERN')")
    public ResponseEntity<Map<String, Object>> getAttendanceByIntern(@PathVariable String internId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Attendance> attendances = attendanceService.getAttendanceByIntern(internId);
            response.put("success", true);
            response.put("message", "Attendance records retrieved successfully");
            response.put("data", attendances);
            response.put("totalCount", attendances.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve attendance: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}/range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('INTERN')")
    public ResponseEntity<Map<String, Object>> getAttendanceByDateRange(
            @PathVariable String internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Attendance> attendances = attendanceService.getAttendanceByDateRange(internId, startDate, endDate);
            response.put("success", true);
            response.put("message", "Attendance records retrieved successfully");
            response.put("data", attendances);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalCount", attendances.size());
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve attendance: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/intern/{internId}/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR') or hasRole('INTERN')")
    public ResponseEntity<Map<String, Object>> getAttendanceSummary(
            @PathVariable String internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Intern> internOpt = internService.getInternById(internId);
            if (!internOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Intern not found with id: " + internId);
                response.put("statusCode", HttpStatus.NOT_FOUND.value());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            long presentDays = attendanceService.countPresentDays(internId, startDate, endDate);
            long lateDays = attendanceService.countLateDays(internId, startDate, endDate);
            double totalWorkHours = attendanceService.getTotalWorkHours(internId, startDate, endDate);
            
            Intern intern = internOpt.get();
            
            response.put("success", true);
            response.put("message", "Attendance summary retrieved successfully");
            response.put("internId", internId);
            response.put("internName", intern.getUser().getFirstName() + " " + intern.getUser().getLastName());
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("totalDays", totalDays);
            response.put("presentDays", presentDays);
            response.put("absentDays", totalDays - presentDays);
            response.put("lateDays", lateDays);
            response.put("attendancePercentage", (presentDays * 100.0) / totalDays);
            response.put("totalWorkHours", totalWorkHours);
            response.put("averageWorkHours", totalWorkHours / totalDays);
            response.put("statusCode", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get attendance summary: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('INTERN')")
    public ResponseEntity<Map<String, Object>> getTodayAttendance() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get current intern ID from security context (simplified - you'll need to implement this)
            String internId = getCurrentInternId();
            Optional<Attendance> attendanceOpt = attendanceService.getTodayAttendance(internId);
            
            if (attendanceOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Today's attendance retrieved successfully");
                response.put("data", attendanceOpt.get());
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", true);
                response.put("message", "No attendance marked for today");
                response.put("checkedIn", false);
                response.put("statusCode", HttpStatus.OK.value());
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve today's attendance: " + e.getMessage());
            response.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/report/{internId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ResponseEntity<byte[]> generateAttendanceReport(
            @PathVariable String internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            byte[] report = generateAttendanceReportPDF(internId, startDate, endDate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report_" + internId + "_" + startDate + "_to_" + endDate + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private byte[] generateAttendanceReportPDF(String internId, LocalDate startDate, LocalDate endDate) throws Exception {
        Optional<Intern> internOpt = internService.getInternById(internId);
        if (!internOpt.isPresent()) {
            throw new Exception("Intern not found");
        }
        
        Intern intern = internOpt.get();
        List<Attendance> attendances = attendanceService.getAttendanceByDateRange(internId, startDate, endDate);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Attendance Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // Intern Info
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        document.add(new Paragraph("Intern Name: " + intern.getUser().getFirstName() + " " + intern.getUser().getLastName(), infoFont));
        document.add(new Paragraph("Intern ID: " + intern.getInternId(), infoFont));
        document.add(new Paragraph("Department: " + (intern.getDepartment() != null ? intern.getDepartment() : "N/A"), infoFont));
        document.add(new Paragraph("Period: " + startDate + " to " + endDate, infoFont));
        document.add(new Paragraph(" "));

        // Summary
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long presentDays = attendanceService.countPresentDays(internId, startDate, endDate);
        long lateDays = attendanceService.countLateDays(internId, startDate, endDate);
        double totalWorkHours = attendanceService.getTotalWorkHours(internId, startDate, endDate);
        
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(50);
        
        addSummaryCell(summaryTable, "Total Days:", String.valueOf(totalDays));
        addSummaryCell(summaryTable, "Present Days:", String.valueOf(presentDays));
        addSummaryCell(summaryTable, "Absent Days:", String.valueOf(totalDays - presentDays));
        addSummaryCell(summaryTable, "Late Days:", String.valueOf(lateDays));
        addSummaryCell(summaryTable, "Attendance Percentage:", String.format("%.2f%%", (presentDays * 100.0) / totalDays));
        addSummaryCell(summaryTable, "Total Work Hours:", String.format("%.2f", totalWorkHours));
        
        document.add(summaryTable);
        document.add(new Paragraph(" "));

        // Attendance Details Table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15, 15, 15, 15, 20, 20});
        
        // Headers
        addTableHeader(table, "Date");
        addTableHeader(table, "Check In");
        addTableHeader(table, "Check Out");
        addTableHeader(table, "Status");
        addTableHeader(table, "Work Hours");
        addTableHeader(table, "Late Minutes");

        // Data
        for (Attendance attendance : attendances) {
            table.addCell(attendance.getDate().toString());
            table.addCell(attendance.getCheckInTime() != null ? attendance.getCheckInTime().toString() : "-");
            table.addCell(attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toString() : "-");
            table.addCell(attendance.getStatus() != null ? attendance.getStatus() : "-");
            table.addCell(String.format("%.2f", attendance.getWorkHours()));
            table.addCell(String.valueOf(attendance.getLateMinutes()));
        }

        document.add(table);
        document.close();
        
        return out.toByteArray();
    }

    private void addSummaryCell(PdfPTable table, String label, String value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 12);
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, font));
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, font));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String header) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        PdfPCell cell = new PdfPCell(new Paragraph(header, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private String getCurrentInternId() {
        // This should be implemented to get the current logged-in intern's ID
        // For now, return a placeholder or implement based on your security context
        return "INT001";
    }
}