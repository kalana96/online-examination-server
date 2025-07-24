package com.examination.online_examination_server.exception.exam;

import lombok.Getter;

@Getter
public class ExamException extends RuntimeException{
    private final String errorCode;

    public ExamException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ExamException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }
}
