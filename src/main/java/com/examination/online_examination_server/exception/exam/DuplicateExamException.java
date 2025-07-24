package com.examination.online_examination_server.exception.exam;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class DuplicateExamException extends ExamException {
    public DuplicateExamException(String message) {

        super(message, VarListt.RES_DUPLICATE);
    }
}
