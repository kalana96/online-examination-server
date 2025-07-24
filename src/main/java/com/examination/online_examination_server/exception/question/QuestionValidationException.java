package com.examination.online_examination_server.exception.question;

import com.examination.online_examination_server.constant.VarListt;

public class QuestionValidationException extends RuntimeException {
    private final String errorCode;
    public QuestionValidationException(String message) {
        super(message);
        this.errorCode = VarListt.RES_INVALID_INPUT;
    }

    public QuestionValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = VarListt.RES_INVALID_INPUT;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
