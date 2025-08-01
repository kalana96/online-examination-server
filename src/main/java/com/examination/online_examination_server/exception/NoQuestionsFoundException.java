package com.examination.online_examination_server.exception;

public class NoQuestionsFoundException extends RuntimeException{
    public NoQuestionsFoundException(String message) {

        super(message);
    }
}
