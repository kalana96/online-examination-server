package com.examination.online_examination_server.exception.question;

import lombok.Getter;

@Getter
public class QuestionException extends RuntimeException{
    private final String errorCode;

    public QuestionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public QuestionException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }
}
