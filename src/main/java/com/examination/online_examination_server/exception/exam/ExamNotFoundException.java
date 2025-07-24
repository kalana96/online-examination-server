package com.examination.online_examination_server.exception.exam;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class ExamNotFoundException extends ExamException {
    public ExamNotFoundException(String message) {

        super(message, VarListt.RES_NO_DATE_FOUND);
    }
}
