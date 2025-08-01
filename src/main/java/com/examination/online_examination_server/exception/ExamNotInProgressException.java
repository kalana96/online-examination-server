package com.examination.online_examination_server.exception;

public class ExamNotInProgressException extends RuntimeException{
    public ExamNotInProgressException(String message) {

        super(message);
    }
}
