package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.Utility.ResponseBuilder;
import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.ExamDTO;
import com.examination.online_examination_server.dto.ExamStatsDTO;
import com.examination.online_examination_server.dto.ResponseDTO;
import com.examination.online_examination_server.service.ExamService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("api/v1/teacher/exam")
public class TeacherExamController {

    @Autowired
    private ExamService examService;

    // ===== ENDPOINTS FOR EXAM MANAGEMENT =====
    /**
     * Schedule a new exam
     * @param examDTO The exam data to be scheduled
     * @return ResponseEntity containing the result of exam scheduling
     */
//    @PostMapping("/scheduleExam")
//    public ResponseEntity<ResponseDTO> scheduleExam(@RequestBody ExamDTO examDTO) {
//        try {
//            log.info("Scheduling new exam: {}", examDTO.getExamName());
//
//            String result = examService.scheduleExam(examDTO);
//            return buildExamResponse(result, examDTO, "scheduled");
//
//        } catch (IllegalArgumentException ex) {
//            log.error("Invalid input data for exam scheduling", ex);
//            return createErrorResponse(VarList.RES_ERROR,
//                    "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception ex) {
//            log.error("Unexpected error during exam scheduling", ex);
//            return createErrorResponse(VarList.RES_ERROR,
//                    "An unexpected error occurred while scheduling exam", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PostMapping("/scheduleExam")
    public ResponseEntity<ResponseDTO> scheduleExam(@Valid @RequestBody ExamDTO examDTO) {
        log.info("Scheduling new exam: {}", examDTO.getExamName());
        ExamDTO scheduledExam = examService.examSchedule(examDTO);
        log.info("Exam '{}' scheduled successfully with ID: {}", examDTO.getExamName(), examDTO.getId());
        return ResponseBuilder.buildSuccessResponse("Exam scheduled successfully", scheduledExam);
    }

    /**
     * Update an existing exam
     * @param examDTO The exam data to be updated (must include exam ID)
     * @return ResponseEntity containing the result of exam update
     */
//    @PutMapping("/updateExam")
//    public ResponseEntity<ResponseDTO> updateExam(@RequestBody ExamDTO examDTO) {
//        try {
//            log.info("Updating exam with ID: {}", examDTO.getId());
//
//            if (examDTO.getId() == null || examDTO.getId() <= 0) {
//                return createErrorResponse(VarList.RES_ERROR,
//                        "Valid exam ID is required for update", HttpStatus.BAD_REQUEST);
//            }
//
//            String result = examService.updateExam(examDTO);
//            return buildExamResponse(result, examDTO, "updated");
//
//        } catch (IllegalArgumentException ex) {
//            log.error("Invalid input data for exam update", ex);
//            return createErrorResponse(VarList.RES_ERROR,
//                    "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception ex) {
//            log.error("Unexpected error during exam update", ex);
//            return createErrorResponse(VarList.RES_ERROR,
//                    "An unexpected error occurred while updating exam", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PutMapping("/updateExam")
    public ResponseEntity<ResponseDTO> updateExam(@RequestBody ExamDTO examDTO) {
        log.info("Updating exam with ID: {}", examDTO.getId());
        ExamDTO result = examService.updateExam(examDTO);
        return ResponseBuilder.buildSuccessResponse("Exam updated successfully", result);
    }

    /**
     * Get exam details by exam ID
     * @param examId The ID of the exam to retrieve
     * @return ResponseEntity containing exam details
     */
    @GetMapping("/getExam/{examId}")
    public ResponseEntity<ResponseDTO> getExamById(@PathVariable Integer examId) {
        try {
            log.info("Fetching exam details for ID: {}", examId);

            if (examId == null || examId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid exam ID is required", HttpStatus.BAD_REQUEST);
            }

            ExamDTO examDTO = examService.getExamById(examId);

            if (examDTO != null) {
                return createSuccessResponse("Exam found successfully", examDTO);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                        "Exam not found with ID: " + examId, HttpStatus.NOT_FOUND);
            }

        } catch (Exception ex) {
            log.error("Error fetching exam by ID {}: ", examId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching exam: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete an exam by ID (soft delete)
     * @param examId The ID of the exam to delete
     * @return ResponseEntity containing the result of exam deletion
     */
    @DeleteMapping("/deleteExam/{examId}")
    public ResponseEntity<ResponseDTO> deleteExam(@PathVariable Integer examId) {
        try {
            log.info("Deleting exam with ID: {}", examId);

            if (examId == null || examId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid exam ID is required", HttpStatus.BAD_REQUEST);
            }

            String result = examService.deleteExam(examId);

            switch (result) {
                case VarList.RES_SUCCESS:
                    return createSuccessResponse("Exam deleted successfully", null);
                case VarList.RES_NO_DATE_FOUND:
                    return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                            "Exam not found with ID: " + examId, HttpStatus.NOT_FOUND);
                default:
                    return createErrorResponse(VarList.RES_ERROR,
                            "Failed to delete exam", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception ex) {
            log.error("Error deleting exam with ID {}: ", examId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error deleting exam: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all exams for a specific teacher
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing list of exams for the teacher
     */
    @GetMapping("/getExamsByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getExamsByTeacher(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching exams for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid teacher ID is required", HttpStatus.BAD_REQUEST);
            }
            List<ExamDTO> examList = examService.getExamsByTeacher(teacherId);

            if (examList.isEmpty()) {
                return createSuccessResponse("No exams found for teacher", examList);
            }
            return createSuccessResponse("Exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching exams for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching exams for teacher: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all exams for a specific class
     * @param classId The ID of the class
     * @return ResponseEntity containing list of exams for the class
     */
    @GetMapping("/getExamsByClass/{classId}")
    public ResponseEntity<ResponseDTO> getExamsByClass(@PathVariable Integer classId) {
        try {
            log.info("Fetching exams for class ID: {}", classId);

            if (classId == null || classId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid class ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getExamsByClass(classId);

            if (examList.isEmpty()) {
                return createSuccessResponse("No exams found for class", examList);
            }

            return createSuccessResponse("Exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching exams for class ID {}: ", classId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching exams for class: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get exams by teacher and class
     * @param teacherId The ID of the teacher
     * @param classId The ID of the class
     * @return ResponseEntity containing list of exams for the teacher and class
     */
    @GetMapping("/getExamsByTeacherAndClass/{teacherId}/{classId}")
    public ResponseEntity<ResponseDTO> getExamsByTeacherAndClass(
            @PathVariable Integer teacherId,
            @PathVariable Integer classId) {
        try {
            log.info("Fetching exams for teacher ID: {} and class ID: {}", teacherId, classId);

            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid teacher ID is required", HttpStatus.BAD_REQUEST);
            }

            if (classId == null || classId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid class ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getExamsByTeacherAndClass(teacherId, classId);

            if (examList.isEmpty()) {
                return createSuccessResponse("No exams found for teacher and class", examList);
            }

            return createSuccessResponse("Exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching exams for teacher ID {} and class ID {}: ", teacherId, classId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching exams: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get upcoming exams for a teacher
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing list of upcoming exams
     */
    @GetMapping("/getUpcomingExamsByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getUpcomingExamsByTeacher(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching upcoming exams for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid teacher ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getUpcomingExamsByTeacher(teacherId);

            if (examList.isEmpty()) {
                return createSuccessResponse("No upcoming exams found for teacher", examList);
            }

            return createSuccessResponse("Upcoming exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching upcoming exams for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching upcoming exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get exam statistics for a teacher
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing exam statistics
     */
    @GetMapping("/getExamStatsByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getExamStatsByTeacher(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching exam statistics for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid teacher ID is required", HttpStatus.BAD_REQUEST);
            }

            ExamStatsDTO examStats = examService.getExamStatsByTeacher(teacherId);

            return createSuccessResponse("Exam statistics fetched successfully", examStats);

        } catch (Exception ex) {
            log.error("Error fetching exam statistics for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching exam statistics: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all active exams
     * @return ResponseEntity containing list of all active exams
     */
    @GetMapping("/getAllExams")
    public ResponseEntity<ResponseDTO> getAllActiveExams() {
        try {
            log.info("Fetching all active exams");

            List<ExamDTO> examList = examService.getAllActiveExams();

            if (examList.isEmpty()) {
                return createSuccessResponse("No active exams found", examList);
            }

            return createSuccessResponse("Active exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching all active exams: ", ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all upcoming exams
     * @return ResponseEntity containing list of upcoming exams
     */
    @GetMapping("/getUpcomingExams")
    public ResponseEntity<ResponseDTO> getUpcomingExams() {
        try {
            log.info("Fetching all upcoming exams");

            List<ExamDTO> examList = examService.getUpcomingExams();

            if (examList.isEmpty()) {
                return createSuccessResponse("No upcoming exams found", examList);
            }

            return createSuccessResponse("Upcoming exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching upcoming exams: ", ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching upcoming exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get today's exams
     * @return ResponseEntity containing list of today's exams
     */
    @GetMapping("/getTodaysExams")
    public ResponseEntity<ResponseDTO> getTodaysExams() {
        try {
            log.info("Fetching today's exams");

            List<ExamDTO> examList = examService.getTodaysExams();

            if (examList.isEmpty()) {
                return createSuccessResponse("No exams scheduled for today", examList);
            }

            return createSuccessResponse("Today's exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching today's exams: ", ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching today's exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search exams by name
     * @param searchTerm The search term
     * @return ResponseEntity containing list of matching exams
     */
    @GetMapping("/searchExams")
    public ResponseEntity<ResponseDTO> searchExamsByName(@RequestParam String searchTerm) {
        try {
            log.info("Searching exams by name: {}", searchTerm);

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Search term is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.searchExamsByName(searchTerm);

            if (examList.isEmpty()) {
                return createSuccessResponse("No exams found matching search term", examList);
            }

            return createSuccessResponse("Exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error searching exams by name '{}': ", searchTerm, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error searching exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get exams by type
     * @param examType The exam type
     * @return ResponseEntity containing list of exams of the specified type
     */
    @GetMapping("/getExamsByType/{examType}")
    public ResponseEntity<ResponseDTO> getExamsByType(@PathVariable String examType) {
        try {
            log.info("Fetching exams by type: {}", examType);

            if (examType == null || examType.trim().isEmpty()) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Exam type is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getExamsByType(examType);

            if (examList.isEmpty()) {
                return createSuccessResponse("No exams found for the specified type", examList);
            }

            return createSuccessResponse("Exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching exams by type '{}': ", examType, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching exams by type: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Build response for exam operations (schedule/update)
     * @param result The result code from exam service
     * @param examDTO The exam data
     * @param operation The operation performed ("scheduled" or "updated")
     * @return ResponseEntity with appropriate response
     */
    private ResponseEntity<ResponseDTO> buildExamResponse(String result, ExamDTO examDTO, String operation) {
        switch (result) {
            case VarList.RES_SUCCESS:
                return createSuccessResponse("Exam " + operation + " successfully", examDTO);
            case VarList.RES_ERROR:
                return createErrorResponse(VarList.RES_ERROR,
                        "Please provide valid exam data", HttpStatus.BAD_REQUEST);
            case VarList.RES_INVALID_INPUT:
                return createErrorResponse(VarList.RES_INVALID_INPUT,
                        "Invalid input data provided", HttpStatus.BAD_REQUEST);
            case VarList.RES_DUPLICATE:
                return createErrorResponse(VarList.RES_DUPLICATE,
                        "An exam with the same name, class, and date/time already exists", HttpStatus.CONFLICT);
            case VarList.RES_CLASS_NOT_FOUND:
                return createErrorResponse(VarList.RES_CLASS_NOT_FOUND,
                        "Class not found or is deleted", HttpStatus.NOT_FOUND);
            case VarList.RES_TEACHER_NOT_FOUND:
                return createErrorResponse(VarList.RES_TEACHER_NOT_FOUND,
                        "Teacher not found or is deleted", HttpStatus.NOT_FOUND);
            case VarList.RES_INVALID_TEACHER_CLASS_ASSOCIATION:
                return createErrorResponse(VarList.RES_INVALID_TEACHER_CLASS_ASSOCIATION,
                        "Teacher is not assigned to the specified class", HttpStatus.BAD_REQUEST);
            case VarList.RES_NO_DATE_FOUND:
                return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                        "Exam not found", HttpStatus.NOT_FOUND);
            default:
                return createErrorResponse(VarList.RES_FAILURE,
                        "Failed to " + operation.replace("d", "") + " exam", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // ===== ENDPOINTS FOR EXAM PUBLISH STATUS MANAGEMENT =====

    /**
     * Update exam publish status
     * @param examId The ID of the exam
     * @param isPublished The publish status to set
     * @return ResponseEntity containing the result of publish status update
     */
    @PutMapping("/updateExamPublishStatus/{examId}")
    public ResponseEntity<ResponseDTO> updateExamPublishStatus(
            @PathVariable Integer examId,
            @RequestParam boolean isPublished) {
        try {
            log.info("Updating publish status for exam ID: {} to {}", examId, isPublished);

            if (examId == null || examId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid exam ID is required", HttpStatus.BAD_REQUEST);
            }

            String result = examService.updateExamPublishStatus(examId, isPublished);
            String action = isPublished ? "published" : "unpublished";

            switch (result) {
                case VarList.RES_SUCCESS:
                    return createSuccessResponse("Exam " + action + " successfully", null);
                case VarList.RES_NO_DATE_FOUND:
                    return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                            "Exam not found with ID: " + examId, HttpStatus.NOT_FOUND);
                case VarList.RES_INVALID_PUBLISH_DATE:
                    return createErrorResponse(VarList.RES_INVALID_PUBLISH_DATE,
                            "Cannot publish exam scheduled for past date", HttpStatus.BAD_REQUEST);
                case VarList.RES_INVALID_INPUT:
                    return createErrorResponse(VarList.RES_INVALID_INPUT,
                            "Invalid input data or exam cannot be published", HttpStatus.BAD_REQUEST);
                default:
                    return createErrorResponse(VarList.RES_ERROR,
                            "Failed to update publish status", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception ex) {
            log.error("Error updating publish status for exam ID {}: ", examId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error updating publish status: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all published exams
     * @return ResponseEntity containing list of all published exams
     */
    @GetMapping("/getAllPublishedExams")
    public ResponseEntity<ResponseDTO> getAllPublishedExams() {
        try {
            log.info("Fetching all published exams");

            List<ExamDTO> examList = examService.getAllPublishedExams();

            if (examList.isEmpty()) {
                return createSuccessResponse("No published exams found", examList);
            }

            return createSuccessResponse("Published exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching all published exams: ", ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching published exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all draft exams
     * @return ResponseEntity containing list of all draft exams
     */
    @GetMapping("/getAllDraftExams")
    public ResponseEntity<ResponseDTO> getAllDraftExams() {
        try {
            log.info("Fetching all draft exams");

            List<ExamDTO> examList = examService.getAllDraftExams();

            if (examList.isEmpty()) {
                return createSuccessResponse("No draft exams found", examList);
            }

            return createSuccessResponse("Draft exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching all draft exams: ", ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching draft exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get published exams by teacher
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing list of published exams for the teacher
     */
    @GetMapping("/getPublishedExamsByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getPublishedExamsByTeacher(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching published exams for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid teacher ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getPublishedExamsByTeacher(teacherId);

            if (examList.isEmpty()) {
                return createSuccessResponse("No published exams found for teacher", examList);
            }

            return createSuccessResponse("Published exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching published exams for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching published exams for teacher: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get published exams by teacher
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing list of published exams for the teacher
     */
    @GetMapping("/getPublishedPastExamsByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getPublishedPastExamsByTeacher(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching published Past exams for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid teacher ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getPublishedPastExamsByTeacher(teacherId);

            if (examList.isEmpty()) {
                return createSuccessResponse("No published exams found for teacher", examList);
            }

            return createSuccessResponse("Published exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching published exams for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching published exams for teacher: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get draft exams by teacher
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing list of draft exams for the teacher
     */
    @GetMapping("/getDraftExamsByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getDraftExamsByTeacher(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching draft exams for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid teacher ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getDraftExamsByTeacher(teacherId);

            if (examList.isEmpty()) {
                return createSuccessResponse("No draft exams found for teacher", examList);
            }

            return createSuccessResponse("Draft exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching draft exams for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching draft exams for teacher: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get published exams by class
     * @param classId The ID of the class
     * @return ResponseEntity containing list of published exams for the class
     */
    @GetMapping("/getPublishedPastExam/{teacherId}")
    public ResponseEntity<ResponseDTO> getPublishedExamsByClass(@PathVariable Integer classId) {
        try {
            log.info("Fetching published exams for class ID: {}", classId);

            if (classId == null || classId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid class ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getPublishedExamsByClass(classId);

            if (examList.isEmpty()) {
                return createSuccessResponse("No published exams found for class", examList);
            }

            return createSuccessResponse("Published exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching published exams for class ID {}: ", classId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching published exams for class: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get draft exams by class
     * @param classId The ID of the class
     * @return ResponseEntity containing list of draft exams for the class
     */
    @GetMapping("/getDraftExamsByClass/{classId}")
    public ResponseEntity<ResponseDTO> getDraftExamsByClass(@PathVariable Integer classId) {
        try {
            log.info("Fetching draft exams for class ID: {}", classId);

            if (classId == null || classId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid class ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getDraftExamsByClass(classId);

            if (examList.isEmpty()) {
                return createSuccessResponse("No draft exams found for class", examList);
            }

            return createSuccessResponse("Draft exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching draft exams for class ID {}: ", classId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching draft exams for class: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get published upcoming exams
     * @return ResponseEntity containing list of published upcoming exams
     */
    @GetMapping("/getPublishedUpcomingExams")
    public ResponseEntity<ResponseDTO> getPublishedUpcomingExams() {
        try {
            log.info("Fetching published upcoming exams");

            List<ExamDTO> examList = examService.getPublishedUpcomingExams();

            if (examList.isEmpty()) {
                return createSuccessResponse("No published upcoming exams found", examList);
            }

            return createSuccessResponse("Published upcoming exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching published upcoming exams: ", ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching published upcoming exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get published exams for today
     * @return ResponseEntity containing list of published exams for today
     */
    @GetMapping("/getPublishedTodaysExams")
    public ResponseEntity<ResponseDTO> getPublishedTodaysExams() {
        try {
            log.info("Fetching published exams for today");

            List<ExamDTO> examList = examService.getPublishedTodaysExams();

            if (examList.isEmpty()) {
                return createSuccessResponse("No published exams scheduled for today", examList);
            }

            return createSuccessResponse("Published today's exams found successfully", examList);

        } catch (Exception ex) {
            log.error("Error fetching published today's exams: ", ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching published today's exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get exam statistics with publish status for a teacher
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing exam statistics with publish status
     */
    @GetMapping("/getExamStatsWithPublishStatus/{teacherId}")
    public ResponseEntity<ResponseDTO> getExamStatsWithPublishStatus(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching exam statistics with publish status for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid teacher ID is required", HttpStatus.BAD_REQUEST);
            }

            ExamStatsDTO examStats = examService.getExamStatsWithPublishStatusByTeacher(teacherId);

            return createSuccessResponse("Exam statistics with publish status fetched successfully", examStats);

        } catch (Exception ex) {
            log.error("Error fetching exam statistics with publish status for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching exam statistics: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if an exam can be published
     * @param examId The ID of the exam
     * @return ResponseEntity containing whether the exam can be published
     */
    @GetMapping("/canPublishExam/{examId}")
    public ResponseEntity<ResponseDTO> canPublishExam(@PathVariable Integer examId) {
        try {
            log.info("Checking if exam ID: {} can be published", examId);

            if (examId == null || examId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid exam ID is required", HttpStatus.BAD_REQUEST);
            }

            boolean canPublish = examService.canPublishExam(examId);

            Map<String, Boolean> response = new HashMap<>();
            response.put("canPublish", canPublish);

            String message = canPublish ? "Exam can be published" : "Exam cannot be published";
            return createSuccessResponse(message, response);

        } catch (Exception ex) {
            log.error("Error checking if exam ID {} can be published: ", examId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error checking publish eligibility: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get publish status summary for a teacher
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing publish status summary
     */
    @GetMapping("/getPublishStatusSummary/{teacherId}")
    public ResponseEntity<ResponseDTO> getPublishStatusSummary(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching publish status summary for teacher ID: {}", teacherId);

            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid teacher ID is required", HttpStatus.BAD_REQUEST);
            }

            Map<String, Long> publishStatusSummary = examService.getPublishStatusSummary(teacherId);

            return createSuccessResponse("Publish status summary fetched successfully", publishStatusSummary);

        } catch (Exception ex) {
            log.error("Error fetching publish status summary for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching publish status summary: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    private ResponseEntity<ResponseDTO> createSuccessResponse(String message, Object content) {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setCode(VarList.RES_SUCCESS);
        responseDTO.setMessage(message);
        responseDTO.setContent(content);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    private ResponseEntity<ResponseDTO> createErrorResponse(String code, String message, HttpStatus status) {
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setCode(code);
        responseDTO.setMessage(message);
        responseDTO.setContent(null);
        return new ResponseEntity<>(responseDTO, status);
    }


}
