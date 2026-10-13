package com.appticode.service;

import com.appticode.entity.Attendance;
import com.appticode.repository.AttendanceRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CustomIdGenerator idGenerator;

    public Attendance checkIn(Attendance attendance) {
        attendance.setAttendanceId(idGenerator.generateAttendanceId());
        attendance.setDate(LocalDate.now());
        attendance.setCheckInTime(LocalTime.now());
        attendance.setCreatedAt(LocalDateTime.now());
        
        // Check if late
        LocalTime officeStartTime = LocalTime.of(9, 0);
        if (attendance.getCheckInTime().isAfter(officeStartTime)) {
            attendance.setStatus("LATE");
            long lateMinutes = ChronoUnit.MINUTES.between(officeStartTime, attendance.getCheckInTime());
            attendance.setLateMinutes((int) lateMinutes);
        } else {
            attendance.setStatus("PRESENT");
        }
        
        attendance.setWorkHours(0.0);
        return attendanceRepository.save(attendance);
    }

    public Optional<Attendance> checkOut(String attendanceId, String checkOutMethod, String location) {
        Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);
        if (attendanceOpt.isPresent()) {
            Attendance attendance = attendanceOpt.get();
            attendance.setCheckOutTime(LocalTime.now());
            attendance.setCheckOutMethod(checkOutMethod);
            if (location != null) {
                attendance.setLocation(location);
            }
            
            // Calculate work hours
            if (attendance.getCheckInTime() != null) {
                long minutes = ChronoUnit.MINUTES.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
                attendance.setWorkHours(minutes / 60.0);
            }
            
            attendance.setUpdatedAt(LocalDateTime.now());
            return Optional.of(attendanceRepository.save(attendance));
        }
        return Optional.empty();
    }

    public Optional<Attendance> getAttendanceById(String attendanceId) {
        return attendanceRepository.findById(attendanceId);
    }

    public List<Attendance> getAttendanceByIntern(String internId) {
        return attendanceRepository.findByInternId(internId);
    }

    public List<Attendance> getAttendanceByDateRange(String internId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByInternIdAndDateBetween(internId, startDate, endDate);
    }

    public Optional<Attendance> getAttendanceByInternAndDate(String internId, LocalDate date) {
        return attendanceRepository.findByInternIdAndDate(internId, date);
    }

    public Optional<Attendance> getTodayAttendance(String internId) {
        return attendanceRepository.findByInternIdAndDate(internId, LocalDate.now());
    }

    public Long countPresentDays(String internId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.countPresentDays(internId, startDate, endDate);
    }

    public Long countLateDays(String internId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.countLateDays(internId, startDate, endDate);
    }

    public Double getTotalWorkHours(String internId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.getTotalWorkHours(internId, startDate, endDate);
    }

    public Double getAverageWorkHours(String internId) {
        return attendanceRepository.getAverageWorkHours(internId);
    }

    public List<Object[]> getDailyAttendanceSummary(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.getDailyAttendanceSummary(startDate, endDate);
    }

    public boolean existsByInternIdAndDate(String internId, LocalDate date) {
        return attendanceRepository.existsByInternIdAndDate(internId, date);
    }

    // FIXED: Changed parameter type from LocalDateTime to LocalTime
    public boolean updateCheckOut(String attendanceId, LocalTime checkOutTime, Double workHours, String status) {
        try {
            attendanceRepository.updateCheckOut(attendanceId, checkOutTime, workHours, status);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}