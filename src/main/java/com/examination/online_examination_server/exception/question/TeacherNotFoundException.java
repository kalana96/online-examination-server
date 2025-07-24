package com.examination.online_examination_server.exception.question;

import com.examination.online_examination_server.constant.VarListt;

public class TeacherNotFoundException extends RuntimeException {
    private final String errorCode;
    public TeacherNotFoundException(String message) {
        super(message);
        this.errorCode = VarListt.RES_TEACHER_NOT_FOUND;
    }

    public TeacherNotFoundException(String message, Throwable cause) {

        super(message, cause);
        this.errorCode = VarListt.RES_TEACHER_NOT_FOUND;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
