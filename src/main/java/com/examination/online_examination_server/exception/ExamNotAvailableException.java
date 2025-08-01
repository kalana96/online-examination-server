package com.examination.online_examination_server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ExamNotAvailableException extends RuntimeException{
    public ExamNotAvailableException(String message) {

        super(message);
    }
}

