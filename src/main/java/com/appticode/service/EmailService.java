package com.appticode.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username:no-reply@appticode.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public boolean sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }

    public boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email: " + e.getMessage());
            return false;
        }
    }

    public boolean sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.addAttachment("attachment", new File(attachmentPath));
            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Failed to send email with attachment: " + e.getMessage());
            return false;
        }
    }

    public boolean sendWelcomeEmail(String to, String name) {
        try {
            String htmlContent;
            if (templateEngine != null) {
                Context context = new Context();
                context.setVariable("name", name);
                context.setVariable("loginUrl", baseUrl + "/api/auth/login");
                htmlContent = templateEngine.process("welcome-email", context);
            } else {
                htmlContent = getDefaultWelcomeEmail(name);
            }
            return sendHtmlEmail(to, "Welcome to Internship Management System", htmlContent);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
            return false;
        }
    }

    public boolean sendPasswordResetEmail(String to, String token) {
        try {
            String htmlContent;
            if (templateEngine != null) {
                Context context = new Context();
                context.setVariable("token", token);
                context.setVariable("resetUrl", frontendUrl + "/reset-password?token=" + token);
                htmlContent = templateEngine.process("password-reset-email", context);
            } else {
                htmlContent = getDefaultPasswordResetEmail(token);
            }
            return sendHtmlEmail(to, "Password Reset Request", htmlContent);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            return false;
        }
    }

    public boolean sendTaskAssignmentEmail(String to, String taskTitle, String dueDate) {
        try {
            String htmlContent;
            if (templateEngine != null) {
                Context context = new Context();
                context.setVariable("taskTitle", taskTitle);
                context.setVariable("dueDate", dueDate);
                context.setVariable("taskUrl", frontendUrl + "/tasks");
                htmlContent = templateEngine.process("task-assignment-email", context);
            } else {
                htmlContent = getDefaultTaskAssignmentEmail(taskTitle, dueDate);
            }
            return sendHtmlEmail(to, "New Task Assigned: " + taskTitle, htmlContent);
        } catch (Exception e) {
            System.err.println("Failed to send task assignment email: " + e.getMessage());
            return false;
        }
    }

    public boolean sendCertificateEmail(String to, String certificateNumber) {
        try {
            String htmlContent;
            if (templateEngine != null) {
                Context context = new Context();
                context.setVariable("certificateNumber", certificateNumber);
                context.setVariable("certificateUrl", frontendUrl + "/certificates/" + certificateNumber);
                htmlContent = templateEngine.process("certificate-email", context);
            } else {
                htmlContent = getDefaultCertificateEmail(certificateNumber);
            }
            return sendHtmlEmail(to, "Your Internship Certificate is Ready", htmlContent);
        } catch (Exception e) {
            System.err.println("Failed to send certificate email: " + e.getMessage());
            return false;
        }
    }

    public void sendBulkEmail(List<String> recipients, String subject, String template, Map<String, Object> variables) {
        for (String recipient : recipients) {
            try {
                String htmlContent;
                if (templateEngine != null) {
                    Context context = new Context();
                    variables.forEach(context::setVariable);
                    htmlContent = templateEngine.process(template, context);
                } else {
                    htmlContent = getDefaultBulkEmailContent(subject, variables);
                }
                sendHtmlEmail(recipient, subject, htmlContent);
            } catch (Exception e) {
                System.err.println("Failed to send bulk email to " + recipient + ": " + e.getMessage());
            }
        }
    }

    public boolean sendLeaveApprovalEmail(String to, String internName, String startDate, String endDate) {
        try {
            String htmlContent;
            if (templateEngine != null) {
                Context context = new Context();
                context.setVariable("internName", internName);
                context.setVariable("startDate", startDate);
                context.setVariable("endDate", endDate);
                htmlContent = templateEngine.process("leave-approval-email", context);
            } else {
                htmlContent = getDefaultLeaveApprovalEmail(internName, startDate, endDate);
            }
            return sendHtmlEmail(to, "Leave Request Approved", htmlContent);
        } catch (Exception e) {
            System.err.println("Failed to send leave approval email: " + e.getMessage());
            return false;
        }
    }

    public boolean sendTaskGradedEmail(String to, String taskTitle, Integer obtainedPoints, Integer totalPoints, String feedback) {
        try {
            String htmlContent;
            if (templateEngine != null) {
                Context context = new Context();
                context.setVariable("taskTitle", taskTitle);
                context.setVariable("obtainedPoints", obtainedPoints);
                context.setVariable("totalPoints", totalPoints);
                context.setVariable("feedback", feedback);
                context.setVariable("percentage", (obtainedPoints * 100.0) / totalPoints);
                htmlContent = templateEngine.process("task-graded-email", context);
            } else {
                htmlContent = getDefaultTaskGradedEmail(taskTitle, obtainedPoints, totalPoints, feedback);
            }
            return sendHtmlEmail(to, "Task Graded: " + taskTitle, htmlContent);
        } catch (Exception e) {
            System.err.println("Failed to send task graded email: " + e.getMessage());
            return false;
        }
    }

    // Default HTML templates (when Thymeleaf is not available)
    private String getDefaultWelcomeEmail(String name) {
        return "<html><body>" +
               "<h2>Welcome " + name + "!</h2>" +
               "<p>Thank you for registering with Internship Management System.</p>" +
               "<p>You can now login to your account.</p>" +
               "<a href='" + baseUrl + "/api/auth/login'>Click here to login</a>" +
               "<br><br>" +
               "<p>Best Regards,<br>Internship Management Team</p>" +
               "</body></html>";
    }

    private String getDefaultPasswordResetEmail(String token) {
        return "<html><body>" +
               "<h2>Password Reset Request</h2>" +
               "<p>You requested to reset your password. Click the link below to reset it:</p>" +
               "<a href='" + frontendUrl + "/reset-password?token=" + token + "'>Reset Password</a>" +
               "<p>If you did not request this, please ignore this email.</p>" +
               "<br>" +
               "<p>Best Regards,<br>Internship Management Team</p>" +
               "</body></html>";
    }

    private String getDefaultTaskAssignmentEmail(String taskTitle, String dueDate) {
        return "<html><body>" +
               "<h2>New Task Assigned: " + taskTitle + "</h2>" +
               "<p>You have been assigned a new task.</p>" +
               "<p><strong>Due Date:</strong> " + dueDate + "</p>" +
               "<a href='" + frontendUrl + "/tasks'>View Task</a>" +
               "<br><br>" +
               "<p>Best Regards,<br>Internship Management Team</p>" +
               "</body></html>";
    }

    private String getDefaultCertificateEmail(String certificateNumber) {
        return "<html><body>" +
               "<h2>Congratulations!</h2>" +
               "<p>Your internship certificate is ready.</p>" +
               "<p><strong>Certificate Number:</strong> " + certificateNumber + "</p>" +
               "<a href='" + frontendUrl + "/certificates/" + certificateNumber + "'>Download Certificate</a>" +
               "<br><br>" +
               "<p>Best Regards,<br>Internship Management Team</p>" +
               "</body></html>";
    }

    private String getDefaultBulkEmailContent(String subject, Map<String, Object> variables) {
        return "<html><body>" +
               "<h2>" + subject + "</h2>" +
               "<p>" + variables.toString() + "</p>" +
               "<br>" +
               "<p>Best Regards,<br>Internship Management Team</p>" +
               "</body></html>";
    }

    private String getDefaultLeaveApprovalEmail(String internName, String startDate, String endDate) {
        return "<html><body>" +
               "<h2>Leave Request Approved</h2>" +
               "<p>Dear " + internName + ",</p>" +
               "<p>Your leave request from " + startDate + " to " + endDate + " has been approved.</p>" +
               "<br>" +
               "<p>Best Regards,<br>Internship Management Team</p>" +
               "</body></html>";
    }

    private String getDefaultTaskGradedEmail(String taskTitle, Integer obtainedPoints, Integer totalPoints, String feedback) {
        double percentage = (obtainedPoints * 100.0) / totalPoints;
        return "<html><body>" +
               "<h2>Task Graded: " + taskTitle + "</h2>" +
               "<p><strong>Score:</strong> " + obtainedPoints + "/" + totalPoints + " (" + String.format("%.2f", percentage) + "%)</p>" +
               "<p><strong>Feedback:</strong> " + (feedback != null ? feedback : "No feedback provided") + "</p>" +
               "<br>" +
               "<p>Best Regards,<br>Internship Management Team</p>" +
               "</body></html>";
    }
}