package com.examination.online_examination_server.exception;

public class MaxAttemptsExceededException extends RuntimeException{
    public MaxAttemptsExceededException(String message) {

        super(message);
    }
}
