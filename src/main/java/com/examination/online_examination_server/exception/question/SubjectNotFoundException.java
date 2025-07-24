package com.examination.online_examination_server.exception.question;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class SubjectNotFoundException extends RuntimeException {
    private final String errorCode;
    public SubjectNotFoundException(String message) {
        super(message);
        this.errorCode = VarListt.RES_SUBJECT_NOT_FOUND;
    }
    public SubjectNotFoundException(String message, Throwable cause) {

        super(message, cause);
        this.errorCode = VarListt.RES_SUBJECT_NOT_FOUND;
    }
    public String getErrorCode() {
        return errorCode;
    }
}
