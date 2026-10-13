package com.appticode.service;

import com.appticode.entity.LeaveRequest;
import com.appticode.repository.LeaveRepository;
import com.appticode.utils.CustomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.appticode.entity.Mentor;
import com.appticode.repository.MentorRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private CustomIdGenerator idGenerator;

    public LeaveRequest applyLeave(LeaveRequest leaveRequest) {
        leaveRequest.setLeaveId(idGenerator.generateLeaveId());
        leaveRequest.setStatus("PENDING");
        leaveRequest.setCreatedAt(LocalDateTime.now());
        
        // Calculate total days
        if (leaveRequest.getStartDate() != null && leaveRequest.getEndDate() != null) {
            long totalDays = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
            leaveRequest.setTotalDays((int) totalDays);
        }
        
        return leaveRepository.save(leaveRequest);
    }


    public Optional<LeaveRequest> approveLeave(String leaveId, String mentorId) {

        Optional<LeaveRequest> leaveOpt = leaveRepository.findById(leaveId);
        Optional<Mentor> mentorOpt = mentorRepository.findById(mentorId);

        if (leaveOpt.isPresent() && mentorOpt.isPresent()) {

            LeaveRequest leave = leaveOpt.get();

            leave.setStatus("APPROVED");
            leave.setApprovedBy(mentorOpt.get());
            leave.setApprovedAt(LocalDateTime.now());
            leave.setUpdatedAt(LocalDateTime.now());

            return Optional.of(leaveRepository.save(leave));
        }

        return Optional.empty();
    }

    public Optional<LeaveRequest> getLeaveById(String leaveId) {
        return leaveRepository.findById(leaveId);
    }

    public List<LeaveRequest> getLeavesByIntern(String internId) {
        return leaveRepository.findByInternId(internId);
    }

    public List<LeaveRequest> getLeavesByStatus(String status) {
        return leaveRepository.findByStatus(status);
    }

    public List<LeaveRequest> getPendingLeaves() {
        return leaveRepository.findByStatus("PENDING");
    }

    public Optional<LeaveRequest> rejectLeave(String leaveId,
            String mentorId,
            String rejectionReason) {

			Optional<LeaveRequest> leaveOpt = leaveRepository.findById(leaveId);
			Optional<Mentor> mentorOpt = mentorRepository.findById(mentorId);
			
			if (leaveOpt.isPresent() && mentorOpt.isPresent()) {
			
			LeaveRequest leave = leaveOpt.get();
			
			leave.setStatus("REJECTED");
			leave.setApprovedBy(mentorOpt.get());
			leave.setApprovedAt(LocalDateTime.now());
			leave.setRejectionReason(rejectionReason);
			leave.setUpdatedAt(LocalDateTime.now());
			
			return Optional.of(leaveRepository.save(leave));
			}
			
			return Optional.empty();
	}

    public boolean cancelLeave(String leaveId) {

        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElse(null);

        if (leave == null) {
            return false;
        }

        if (!"PENDING".equalsIgnoreCase(leave.getStatus())) {
            return false;
        }

        leave.setStatus("CANCELLED");
        leave.setUpdatedAt(LocalDateTime.now());

        leaveRepository.save(leave);

        return true;
    }
    public Long getApprovedLeaveCount(String internId, LocalDate startDate, LocalDate endDate) {
        return leaveRepository.countApprovedLeavesInRange(internId, startDate, endDate);
    }

    public List<LeaveRequest> getPendingLeavesByIntern(String internId) {
        return leaveRepository.findPendingLeavesByIntern(internId);
    }

    public List<LeaveRequest> getApprovedLeavesOnDate(String internId, LocalDate date) {
        return leaveRepository.findApprovedLeavesOnDate(internId, date);
    }

    public boolean hasOverlappingLeave(String internId, LocalDate startDate, LocalDate endDate) {
        List<LeaveRequest> existingLeaves = leaveRepository.findByInternId(internId);
        return existingLeaves.stream()
            .filter(l -> l.getStatus().equals("APPROVED") || l.getStatus().equals("PENDING"))
            .anyMatch(l -> !(endDate.isBefore(l.getStartDate()) || startDate.isAfter(l.getEndDate())));
    }

    public boolean existsByInternIdAndDateRange(String internId, LocalDate startDate, LocalDate endDate) {
        return hasOverlappingLeave(internId, startDate, endDate);
    }
}