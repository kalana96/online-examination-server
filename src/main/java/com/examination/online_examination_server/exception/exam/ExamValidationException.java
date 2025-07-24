package com.examination.online_examination_server.exception.exam;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class ExamValidationException extends ExamException {
    public ExamValidationException(String message) {

        super(message, VarListt.RES_INVALID_INPUT);
    }

    public ExamValidationException(String message, Throwable cause) {
        super(message, VarListt.RES_INVALID_INPUT, cause);
    }
}
