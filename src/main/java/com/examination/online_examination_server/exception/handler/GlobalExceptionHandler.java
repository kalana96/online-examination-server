package com.examination.online_examination_server.exception.handler;

import com.examination.online_examination_server.Utility.ResponseBuilder;
import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.dto.ResponseDTO;
import com.examination.online_examination_server.exception.*;
import com.examination.online_examination_server.exception.exam.*;
import com.examination.online_examination_server.exception.exam.ClassNotFoundException;
import com.examination.online_examination_server.exception.question.DatabaseOperationException;
import com.examination.online_examination_server.exception.question.QuestionAlreadyExistsException;
import com.examination.online_examination_server.exception.question.QuestionValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.format.DateTimeParseException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ExamUpdateConflictException.class)
    public ResponseEntity<ResponseDTO> handleExamUpdateConflictException(ExamUpdateConflictException ex) {
        log.error("Exam update conflict: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PublishedExamModificationException.class)
    public ResponseEntity<ResponseDTO> handlePublishedExamModificationException(PublishedExamModificationException ex) {
        log.error("Published exam modification attempt: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ExamValidationException.class)
    public ResponseEntity<ResponseDTO> handleExamValidationException(
            ExamValidationException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateExamException.class)
    public ResponseEntity<ResponseDTO> handleDuplicateExamException(
            DuplicateExamException ex, WebRequest request) {
        log.error("Duplicate exam error: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ClassNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleClassNotFoundException(
            ClassNotFoundException ex, WebRequest request) {
        log.error("Class not found: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TeacherNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleTeacherNotFoundException(
            TeacherNotFoundException ex, WebRequest request) {
        log.error("Teacher not found: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidTeacherClassAssociationException.class)
    public ResponseEntity<ResponseDTO> handleInvalidTeacherClassAssociationException(
            InvalidTeacherClassAssociationException ex, WebRequest request) {
        log.error("Invalid teacher-class association: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExamNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleExamNotFoundException(
            ExamNotFoundException ex, WebRequest request) {
        log.error("Exam not found: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExamDatabaseException.class)
    public ResponseEntity<ResponseDTO> handleExamDatabaseException(
            ExamDatabaseException ex, WebRequest request) {
        log.error("Database error: {}", ex.getMessage(), ex.getCause());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExamException.class)
    public ResponseEntity<ResponseDTO> handleGenericExamException(
            ExamException ex, WebRequest request) {
        log.error("Exam error: {}", ex.getMessage(), ex);
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResponseDTO> handleDataAccessException(
            DataAccessException ex, WebRequest request) {
        log.error("Database access error: {}", ex.getMessage(), ex);
        return ResponseBuilder.buildErrorResponse(VarListt.RES_ERROR, "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(VarListt.RES_INVALID_INPUT, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ResponseDTO> handleDateTimeParseException(
            DateTimeParseException ex, WebRequest request) {
        log.error("Date/time parsing error: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(VarListt.RES_INVALID_INPUT, "Invalid date or time format", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseBuilder.buildErrorResponse(VarListt.RES_FAILURE, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }



    //Question Exception
    @ExceptionHandler(QuestionValidationException.class)
    public ResponseEntity<ResponseDTO> handleQuestionValidationException(
            QuestionValidationException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(QuestionAlreadyExistsException.class)
    public ResponseEntity<ResponseDTO> handleQuestionAlreadyExistsException(
            QuestionAlreadyExistsException ex, WebRequest request) {
        log.error("Duplicate Question error: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<ResponseDTO> handleDatabaseOperationException(
            DatabaseOperationException ex, WebRequest request) {
        log.error("Database error: {}", ex.getMessage(), ex.getCause());
        return ResponseBuilder.buildErrorResponse(ex.getErrorCode(), "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR);
    }






    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDTO> ResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Not Found", ex.getMessage(), ex.getCause());
        return ResponseBuilder.buildErrorResponse(VarListt.RES_NO_DATE_FOUND, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ResponseDTO> DuplicateResourceException(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Duplicate Resource", ex.getMessage(), ex.getCause());
        return ResponseBuilder.buildErrorResponse(VarListt.RES_DUPLICATE, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }




    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleStudentNotFoundException(
            StudentNotFoundException ex, WebRequest request) {
        log.error("Student not found: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(VarListt.RES_FAILURE, ex.getMessage(), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(ExamsNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleExamsNotFoundException(
            ExamsNotFoundException ex, WebRequest request) {
        log.error("Exam not found: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(VarListt.RES_FAILURE, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExamRegistrationException.class)
    public ResponseEntity<ResponseDTO> handleExamRegistrationException(
            ExamRegistrationException ex, WebRequest request) {
        log.error("Exam registration error: {}", ex.getMessage());
        return ResponseBuilder.buildErrorResponse(VarListt.RES_FAILURE, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }



}
