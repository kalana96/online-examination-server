package com.examination.online_examination_server.exception.exam;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class ClassNotFoundException extends ExamException {
    public ClassNotFoundException(String message) {

        super(message, VarListt.RES_CLASS_NOT_FOUND);
    }
}
