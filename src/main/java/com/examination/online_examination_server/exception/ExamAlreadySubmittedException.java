package com.examination.online_examination_server.exception;

public class ExamAlreadySubmittedException extends RuntimeException{
    public ExamAlreadySubmittedException(String message) {
        super(message);
    }

    public ExamAlreadySubmittedException(String message, Throwable cause) {

        super(message, cause);
    }
}
