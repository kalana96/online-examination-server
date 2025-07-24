package com.examination.online_examination_server.exception.question;

import com.examination.online_examination_server.constant.VarListt;

public class QuestionNotFoundException extends RuntimeException {
    private final String errorCode;
    public QuestionNotFoundException(String message) {
        super(message);
        this.errorCode = VarListt.RES_QUESTION_NOT_FOUND;
    }

    public QuestionNotFoundException(String message, Throwable cause) {

        super(message, cause);
        this.errorCode = VarListt.RES_QUESTION_NOT_FOUND;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
