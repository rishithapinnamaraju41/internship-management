package com.appticode.service;

import com.appticode.entity.Mentor;
import com.appticode.entity.Timesheet;
import com.appticode.entity.User;
import com.appticode.repository.MentorRepository;
import com.appticode.repository.TimesheetRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TimesheetService {

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private MentorRepository mentorRepository;
    
    @Autowired
    private CustomIdGenerator idGenerator;

    public Timesheet createTimesheet(Timesheet timesheet) {
        timesheet.setTimesheetId(idGenerator.generateTimesheetId());
        timesheet.setStatus("PENDING");
        timesheet.setCreatedAt(LocalDateTime.now());
        return timesheetRepository.save(timesheet);
    }

    public Timesheet updateTimesheet(String timesheetId, Timesheet timesheetDetails) {

        Timesheet timesheet = timesheetRepository.findById(timesheetId).orElse(null);

        if (timesheet == null) {
            return null;
        }

        if (!"PENDING".equals(timesheet.getStatus())) {
            return null;
        }

        if (timesheetDetails.getHoursWorked() != null)
            timesheet.setHoursWorked(timesheetDetails.getHoursWorked());

        if (timesheetDetails.getDescription() != null)
            timesheet.setDescription(timesheetDetails.getDescription());

        if (timesheetDetails.getTaskName() != null)
            timesheet.setTaskName(timesheetDetails.getTaskName());

        timesheet.setUpdatedAt(LocalDateTime.now());

        return timesheetRepository.save(timesheet);
    }
    public Timesheet approveTimesheet(String timesheetId, String mentorId) {

        Timesheet timesheet = timesheetRepository.findById(timesheetId).orElse(null);

        if (timesheet == null) {
            return null;
        }

        Mentor mentor = mentorRepository.findById(mentorId).orElse(null);

        if (mentor == null) {
            return null;
        }

        timesheet.setStatus("APPROVED");
        timesheet.setApprovedBy(mentor);
        timesheet.setApprovedAt(LocalDateTime.now());
        timesheet.setUpdatedAt(LocalDateTime.now());

        return timesheetRepository.save(timesheet);
    }

    public Timesheet rejectTimesheet(String timesheetId,
            String mentorId,
            String rejectionReason) {
		
		Timesheet timesheet = timesheetRepository.findById(timesheetId).orElse(null);
		
		if (timesheet == null) {
		return null;
		}
		
		Mentor mentor = mentorRepository.findById(mentorId).orElse(null);
		
		if (mentor == null) {
		return null;
		}
		
		timesheet.setStatus("REJECTED");
		timesheet.setApprovedBy(mentor);
		timesheet.setApprovedAt(LocalDateTime.now());
		timesheet.setRejectionReason(rejectionReason);
		timesheet.setUpdatedAt(LocalDateTime.now());
		
		return timesheetRepository.save(timesheet);
	}
    public Timesheet getTimesheetById(String timesheetId) {
        return timesheetRepository.findById(timesheetId).orElse(null);
    }

    public Timesheet getTimesheetByInternAndDate(String internId, LocalDate date) {
        return timesheetRepository.findByInternIdAndDate(internId, date);
    }

    public List<Timesheet> getTimesheetsByIntern(String internId) {
        return timesheetRepository.findByInternId(internId);
    }

    public List<Timesheet> getTimesheetsByInternAndDateRange(String internId, LocalDate startDate, LocalDate endDate) {
        return timesheetRepository.findByInternIdAndDateBetween(internId, startDate, endDate);
    }

    public List<Timesheet> getPendingTimesheets() {
        return timesheetRepository.findPendingTimesheets();
    }

    public List<Timesheet> getTimesheetsByStatus(String status) {
        return timesheetRepository.findByStatus(status);
    }

    public boolean deleteTimesheet(String timesheetId) {
        Optional<Timesheet> timesheetOpt = timesheetRepository.findById(timesheetId);
        if (timesheetOpt.isPresent()) {
            Timesheet timesheet = timesheetOpt.get();
            if ("PENDING".equals(timesheet.getStatus())) {
                timesheetRepository.delete(timesheet);
                return true;
            }
        }
        return false;
    }

    public Double getTotalApprovedHours(String internId, LocalDate startDate, LocalDate endDate) {
        return timesheetRepository.getTotalApprovedHours(internId, startDate, endDate);
    }

    public boolean existsByInternIdAndDate(String internId, LocalDate date) {
        return timesheetRepository.existsByInternIdAndDate(internId, date);
    }
}