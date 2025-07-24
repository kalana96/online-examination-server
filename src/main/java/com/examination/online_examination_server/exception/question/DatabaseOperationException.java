package com.examination.online_examination_server.exception.question;

import com.examination.online_examination_server.constant.VarListt;

public class DatabaseOperationException extends RuntimeException {
    private final String errorCode;
    public DatabaseOperationException(String message) {
        super(message);
        this.errorCode = VarListt.RES_ERROR;
    }
    public DatabaseOperationException(String message, Throwable cause) {

        super(message, cause);
        this.errorCode = VarListt.RES_ERROR;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
