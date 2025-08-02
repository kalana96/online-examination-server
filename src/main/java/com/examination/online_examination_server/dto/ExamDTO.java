package com.examination.online_examination_server.dto;

import com.examination.online_examination_server.entity.Exam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamDTO {

    private Integer id;
    private String examName;
    private String examType;
    private LocalDate examDate;
    private Integer duration; // Duration in minutes
    private Integer maxMark;
    private Integer passMark;
    private String instructions;
    private String startTime; // Format: "HH:mm"
    private String endTime; // Format: "HH:mm"
    private Integer studentCount;
    private String proctoringStatus = "disabled"; // "enabled" or "disabled"
    private boolean isPublished ; // Publish status flag
    private LocalDateTime publishedAt; // Timestamp when exam was published
    private Integer classId;
    private Integer teacherId;

    private TeacherDTO teacher; // Nested teacher information
    private ClassDTO clazz; // Nested classes information

    private Long registeredStudentCount = 0L;
    private Long attemptCount = 0L;

    //field for email notification
//    private Boolean sendEmailNotification = false;

    private EmailNotificationDTO emailNotification;

    private Boolean isRandomizeQuestions = false;
    private Boolean isRandomizeOptions = false;
    private Boolean showResultsImmediately = false;
    private Boolean allowReview = true;
    private Exam.ExamStatus status = Exam.ExamStatus.DRAFT;
}