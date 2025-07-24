package com.examination.online_examination_server.exception.question;

import com.examination.online_examination_server.constant.VarListt;

public class QuestionInUseException extends RuntimeException {
    private final String errorCode;
    public QuestionInUseException(String message) {
        super(message);
        this.errorCode = VarListt.RES_QUESTION_IN_USE;
    }

    public QuestionInUseException(String message, Throwable cause) {

        super(message, cause);
        this.errorCode = VarListt.RES_QUESTION_IN_USE;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
