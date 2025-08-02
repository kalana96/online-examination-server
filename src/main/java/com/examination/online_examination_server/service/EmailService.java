package com.examination.online_examination_server.service;


import com.examination.online_examination_server.entity.Exam;
import com.examination.online_examination_server.entity.Student;

import java.util.List;

//@Service
public interface  EmailService {
    void sendExamNotificationEmail(Exam exam, List<Student> students);
    void updateEmailNotificationStatus(Integer examId, String status, Integer sentCount, Integer failedCount);
}
