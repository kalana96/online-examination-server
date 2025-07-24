package com.examination.online_examination_server.exception.exam;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class TeacherNotFoundException extends ExamException {
    public TeacherNotFoundException(String message) {

        super(message, VarListt.RES_TEACHER_NOT_FOUND);
    }
}
