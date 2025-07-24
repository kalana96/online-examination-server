package com.examination.online_examination_server.exception;

public class ExamsNotFoundException extends RuntimeException{
    public ExamsNotFoundException(String message) {
        super(message);
    }
}
