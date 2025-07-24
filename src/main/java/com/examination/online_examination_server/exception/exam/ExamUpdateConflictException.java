package com.examination.online_examination_server.exception.exam;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class ExamUpdateConflictException extends ExamException {
    public ExamUpdateConflictException(String message) {

        super(message, VarListt.RES_CONFLICT);
    }
}
