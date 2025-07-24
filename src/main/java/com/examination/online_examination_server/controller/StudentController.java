package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.*;
import com.examination.online_examination_server.service.ClassManagementService;
import com.examination.online_examination_server.service.ExamService;
import com.examination.online_examination_server.service.StudentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("api/v1/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private ResponseDTO responseDTO;

    @Autowired
    private ClassManagementService classManagementService;

    @Autowired
    private ExamService examService;

    // Update an existing student
    @PutMapping(value = "/updateStudentProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> updateStudent(
            @RequestParam("student") String studentJson,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto) {
        try {
            StudentDTO studentDTO = parseStudentJson(studentJson);

            // Validate profile photo
            String fileValidationResult = validateAndProcessFile(profilePhoto, studentDTO);
            if (fileValidationResult != null) {
                return createErrorResponse(VarList.RES_ERROR, fileValidationResult, HttpStatus.BAD_REQUEST);
            }
            // Update the student
            String result = studentService.updateStudentProfile(studentDTO);
            return buildStudentUpdateResponse(result, studentDTO);

        } catch (JsonProcessingException ex) {
            log.error("JSON parsing error", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid JSON format: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException ex) {
            log.error("File processing error", ex);
            return createErrorResponse(VarList.RES_ERROR, "Error processing file: " + ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid input data", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Unexpected error during student update", ex);
            return createErrorResponse(VarList.RES_ERROR, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Methode toConvert JSON string to StudentDTO
    private StudentDTO parseStudentJson(String studentJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.readValue(studentJson, StudentDTO.class);
    }

    // Methode to Validate profile photo
    private String validateAndProcessFile(MultipartFile profilePhoto, StudentDTO studentDTO) throws IOException {
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            String contentType = profilePhoto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return "Only image files are allowed";
            }
            if (profilePhoto.getSize() > 5 * 1024 * 1024) {
                log.error("File size exceeds 5MB limit");
                return "File size should not exceed 5MB";
            }
            studentDTO.setProfilePhoto(profilePhoto.getBytes());
        } else {
            studentDTO.setProfilePhoto(null);
        }
        return null;
    }

    // Helper method to build update Student response
    private ResponseEntity<ResponseDTO> buildStudentUpdateResponse(String result, StudentDTO studentDTO) {
        switch (result) {
            case VarList.RES_SUCCESS:
                return createSuccessResponse("Student updated successfully", studentDTO);
            case VarList.RES_ERROR:
                return createErrorResponse(VarList.RES_ERROR, "Please enter student data", HttpStatus.BAD_REQUEST);
            case VarList.RES_NO_DATE_FOUND:
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Student not found or is deleted", HttpStatus.NOT_FOUND);
            case VarList.RES_DUPLICATE:
                return createErrorResponse(VarList.RES_DUPLICATE, "Registration number already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_NIC:
                return createErrorResponse(VarList.RES_DUPLICATE_NIC, "NIC already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_EMAIL:
                return createErrorResponse(VarList.RES_DUPLICATE_EMAIL, "Email already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_USERNAME:
                return createErrorResponse(VarList.RES_DUPLICATE_USERNAME, "Username already exists", HttpStatus.CONFLICT);
            case VarList.RES_CLASS_NOT_FOUND:
                return createErrorResponse(VarList.RES_CLASS_NOT_FOUND, "Class not found", HttpStatus.BAD_REQUEST);
            case VarList.RES_NO_SUBJECTS:
                return createErrorResponse(VarList.RES_NO_SUBJECTS, "Student must have at least one subject", HttpStatus.BAD_REQUEST);
            case VarList.RES_INVALID_INPUT:
                return createErrorResponse(VarList.RES_INVALID_INPUT, "One or more subject IDs are invalid", HttpStatus.BAD_REQUEST);
            default:
                log.error("Student update failed with result code: {}", result);
                return createErrorResponse(VarList.RES_FAILURE, "Failed to update student", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // Search for a specific student by ID
    @GetMapping("getStudent/{id}")
    public ResponseEntity getStudentById(@PathVariable int id) {
        try {
            StudentDTO studentDTO = studentService.getStudentById(id); // Fetch student using studentService
            if (studentDTO != null) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Student found successfully");
                responseDTO.setContent(studentDTO);
                return new ResponseEntity(responseDTO, HttpStatus.OK);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("Student Not Found for this ID");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.NOT_FOUND); //404
            }
        } catch (Exception ex) {
            log.error("Error fetching student by ID: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search for a specific student by userName
    @GetMapping("/getStudentByEmail/{email}")
    public ResponseEntity GetStudentByEmail(@PathVariable String email) {
        try {
            StudentDTO studentDTO = studentService.getStudentByEmail(email); // Fetch student using studentService
            if (studentDTO != null) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Student found successfully");
                responseDTO.setContent(studentDTO);
                return new ResponseEntity(responseDTO, HttpStatus.OK);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                log.error("Student Not Found for this email");
                responseDTO.setMessage("Student Not Found for this email");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.NOT_FOUND); //404
            }
        } catch (Exception ex) {
            log.error("Error fetching student by email: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get all classes associated with a specific student by student ID
     *
     * @param studentId The ID of the student
     * @return ResponseEntity containing list of classes associated with the student
     */
    @GetMapping("/getClassesByStudent/{studentId}")
    public ResponseEntity<ResponseDTO> getClassesByStudent(@PathVariable Integer studentId) {
        try {
            log.info("Fetching classes for student ID: {}", studentId);

            List<ClassDTO> classList = classManagementService.getClassesByStudent(studentId);

            if (!classList.isEmpty()) {
                return createSuccessResponse("Classes found successfully for the student", classList);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                        "No classes found for student with ID: " + studentId, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching classes for student ID {}: ", studentId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching classes for student: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    // EXAM ENDPOINTS

    /**
     * Get upcoming exams for a student
     * @param studentId The ID of the student
     * @return ResponseEntity containing list of upcoming exams
     */
    @GetMapping("/exam/getUpcomingExamsByStudent/{studentId}")
    public ResponseEntity<ResponseDTO> getUpcomingExamsByStudent(@PathVariable Integer studentId) {
        try {
            log.info("Fetching upcoming exams for studentId ID: {}", studentId);

            if (studentId == null || studentId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid studentId ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getUpcomingExamsForStudent(studentId);

            if (examList.isEmpty()) {
                log.info("Upcoming exams not found for studentId ID: {}", studentId);
                return createSuccessResponse("No upcoming exams found for studentId", examList);
            }
            log.info("Upcoming exams found successfully for studentId ID: {}", studentId);
            return createSuccessResponse("Upcoming exams found successfully", examList);


        } catch (Exception ex) {
            log.error("Error fetching upcoming exams for teacher ID {}: ", studentId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching upcoming exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // EXAM ENDPOINTS

    /**
     * Get today exams for a exam who has registration approved student
     * @param studentId The ID of the student
     * @return ResponseEntity containing list of today exams
     */
    @GetMapping("/exam/getTodayExamsByStudent/{studentId}")
    public ResponseEntity<ResponseDTO> getTodayExamsByStudent(@PathVariable Integer studentId) {
        try {
            log.info("Fetching today exams for studentId ID: {}", studentId);

            if (studentId == null || studentId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Valid studentId ID is required", HttpStatus.BAD_REQUEST);
            }

            List<ExamDTO> examList = examService.getTodayExamsForRegisteredStudent(studentId);

            if (examList.isEmpty()) {
                log.info("today exams not found for studentId ID: {}", studentId);
                return createSuccessResponse("No today exams found for studentId", examList);
            }
            log.info("today exams found successfully for studentId ID: {}", studentId);
            return createSuccessResponse("today exams found successfully", examList);


        } catch (Exception ex) {
            log.error("Error fetching today exams for teacher ID {}: ", studentId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching today exams: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get exam details by exam ID
     * @param examId The ID of the exam to retrieve
     * @return ResponseEntity containing exam details
     */
    @GetMapping("/exam/getExam/{examId}")
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



    // Helper methods for response creation
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