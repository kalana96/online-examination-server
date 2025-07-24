package com.examination.online_examination_server.exception.exam;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class InvalidTeacherClassAssociationException extends ExamException {
    public InvalidTeacherClassAssociationException(String message) {
        super(message, VarListt.RES_INVALID_TEACHER_CLASS_ASSOCIATION);
    }
}
