package com.appticode.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.appticode.entity.Mentor;
import com.appticode.repository.MentorRepository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CustomIdGenerator {

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    @Autowired
    private MentorRepository mentorRepository;

    private String generateId(String prefix, int length) {
        AtomicLong counter = counters.computeIfAbsent(prefix, k -> new AtomicLong(0));
        long sequence = counter.incrementAndGet();
        String format = "%0" + length + "d";
        return prefix + String.format(format, sequence);
    }

    public String generateUserId() {
        return generateId("USR", 3);
    }

    public String generateInternId() {
        return generateId("INT", 3);
    }

    public String generateMentorId() {

        Mentor lastMentor = mentorRepository.findTopByOrderByMentorIdDesc();

        if (lastMentor == null) {
            return "MENT001";
        }

        String lastId = lastMentor.getMentorId(); // e.g. MENT007

        int number = Integer.parseInt(lastId.substring(4));

        return String.format("MENT%03d", number + 1);
    }

    public String generateTaskId() {
        return generateId("TASK", 3);
    }

    public String generateBatchId() {
        return generateId("BATCH", 3);
    }

    public String generateSubmissionId() {
        return generateId("SUB", 3);
    }

    public String generateTimesheetId() {
        return generateId("TS", 3);
    }

    public String generateAttendanceId() {
        return generateId("ATT", 3);
    }

    public String generateLeaveId() {
        return generateId("LV", 3);
    }

    public String generateCertificateId() {
        return generateId("CERT", 3);
    }

    public String generateNotificationId() {
        return generateId("NOTIF", 3);
    }

    public String generateAnalyticsId() {
        return generateId("AI", 3);
    }

    public String generateAuditId() {
        return generateId("AUD", 3);
    }

    public String generateRoleId() {
        return generateId("ROL", 3);
    }

    public String generatePermissionId() {
        return generateId("PER", 3);
    }
}