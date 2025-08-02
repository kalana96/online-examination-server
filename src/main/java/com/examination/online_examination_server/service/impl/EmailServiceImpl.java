package com.examination.online_examination_server.service.impl;

import com.examination.online_examination_server.entity.EmailNotification;
import com.examination.online_examination_server.entity.Exam;
import com.examination.online_examination_server.entity.Student;
import com.examination.online_examination_server.repository.EmailNotificationRepository;
import com.examination.online_examination_server.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailNotificationRepository emailNotificationRepository;


    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    @Async
    public void sendExamNotificationEmail(Exam exam, List<Student> students) {
        log.info("Starting to send exam notification emails for exam: {}", exam.getExamName());

        if (exam.getEmailNotification() == null || !exam.getEmailNotification().getSendNotification()) {
            log.info("Email notification is disabled for exam: {}", exam.getExamName());
            return;
        }

        // Update status to SENDING
        updateEmailNotificationStatus(exam.getId(),
                EmailNotification.EmailStatus.SENDING.toString(), 0, 0);

        int sentCount = 0;
        int failedCount = 0;

        for (Student student : students) {
            try {
                sendEmailToStudent(exam, student);
                sentCount++;
                log.debug("Email sent successfully to student: {}", student.getEmail());
            } catch (Exception e) {
                failedCount++;
                log.error("Failed to send email to student: {}. Error: {}",
                        student.getEmail(), e.getMessage());
            }
        }

        // Update final status
        String finalStatus = determineFinalStatus(sentCount, failedCount, students.size());
        updateEmailNotificationStatus(exam.getId(), finalStatus, sentCount, failedCount);

        log.info("Email sending completed for exam: {}. Sent: {}, Failed: {}",
                exam.getExamName(), sentCount, failedCount);
    }

    private void sendEmailToStudent(Exam exam, Student student) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Prepare email context
        Context context = new Context();
        context.setVariable("studentName", student.getFullName());
        context.setVariable("examName", exam.getExamName());
        context.setVariable("examType", exam.getExamType());
        context.setVariable("examDate", exam.getExamDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        context.setVariable("startTime", exam.getStartTime());
        context.setVariable("endTime", exam.getEndTime());
        context.setVariable("duration", exam.getDuration());
        context.setVariable("maxMarks", exam.getMaxMark());
        context.setVariable("passMarks", exam.getPassMark());
        context.setVariable("instructions", exam.getInstructions());
        context.setVariable("additionalMessage", exam.getEmailNotification().getEmailMessage());
        context.setVariable("loginUrl", frontendUrl + "/student/login");

        // Generate HTML content from template
        String htmlContent = templateEngine.process("email/exam-notification-email", context);
        // Set email properties
        helper.setFrom(fromEmail);
        helper.setTo(student.getEmail());
        helper.setSubject(exam.getEmailNotification().getEmailSubject());
        helper.setText(htmlContent, true);

        // Send email
        mailSender.send(message);
    }

    private String determineFinalStatus(int sentCount, int failedCount, int totalCount) {
        if (sentCount == totalCount) {
            return EmailNotification.EmailStatus.SENT.toString();
        } else if (sentCount > 0) {
            return EmailNotification.EmailStatus.PARTIAL.toString();
        } else {
            return EmailNotification.EmailStatus.FAILED.toString();
        }
    }

    @Override
    public void updateEmailNotificationStatus(Integer examId, String status,
                                              Integer sentCount, Integer failedCount) {
        try {
            emailNotificationRepository.findByExamId(examId)
                    .ifPresent(notification -> {
                        notification.setStatus(EmailNotification.EmailStatus.valueOf(status));
                        notification.setSentCount(sentCount);
                        notification.setFailedCount(failedCount);

                        if (EmailNotification.EmailStatus.SENT.toString().equals(status) ||
                                EmailNotification.EmailStatus.PARTIAL.toString().equals(status)) {
                            notification.setSentAt(LocalDateTime.now());
                        }

                        emailNotificationRepository.save(notification);
                    });
        } catch (Exception e) {
            log.error("Failed to update email notification status for exam: {}", examId, e);
        }
    }
}
