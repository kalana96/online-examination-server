package com.examination.online_examination_server.exception.exam;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class ExamDatabaseException extends ExamException {
    public ExamDatabaseException(String message, Throwable cause) {
        super(message, VarListt.RES_ERROR, cause);
    }
}
