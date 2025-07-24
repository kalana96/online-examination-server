package com.examination.online_examination_server.exception.exam;

import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.exception.exam.ExamException;

public class PublishedExamModificationException extends ExamException {
    public PublishedExamModificationException(String message) {
        super(message, VarListt.RES_PUBLISHED_EXAM_MODIFICATION);
    }
}
