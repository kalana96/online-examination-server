package com.examination.online_examination_server.exception.question;

import com.examination.online_examination_server.constant.VarListt;
public class QuestionAlreadyExistsException extends RuntimeException {
    private final String errorCode;

    public QuestionAlreadyExistsException(String message) {
        super(message);
        this.errorCode = VarListt.RES_DUPLICATE;
    }

    public QuestionAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = VarListt.RES_DUPLICATE;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
