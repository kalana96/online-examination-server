package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.*;
import com.examination.online_examination_server.service.*;
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

//@CrossOrigin
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("api/v1/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private ClassManagementService classManagementService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private GradeService gradeService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ResponseDTO responseDTO;


    // Save a new teacher
    @PostMapping(value = "/saveTeacher", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveTeacher(
            @RequestParam("teacher") String teacherJson,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        try {
            // Convert JSON string to TeacherDTO with proper date handling
            TeacherDTO teacherDTO = parseTeacherJson(teacherJson);

            // Validate profile photo
            String fileValidationResult = validateAndProcessFile(profilePhoto, teacherDTO);
            if (fileValidationResult != null) {
                return createErrorResponse(VarList.RES_ERROR, fileValidationResult, HttpStatus.BAD_REQUEST);
            }

            String result = teacherService.saveTeacher(teacherDTO);  // Save the teacher
            return buildResponse(result, teacherDTO);

        } catch (JsonProcessingException ex) {
            return createErrorResponse(VarList.RES_ERROR, "Invalid JSON format: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException ex) {
            return createErrorResponse(VarList.RES_ERROR, "Error processing file: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid input data", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return createErrorResponse(VarList.RES_ERROR, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Method to convert JSON string to TeacherDTO
    private TeacherDTO parseTeacherJson(String teacherJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.readValue(teacherJson, TeacherDTO.class);
    }

    // Method to validate profile photo
    private String validateAndProcessFile(MultipartFile profilePhoto, TeacherDTO teacherDTO) throws IOException {
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            String contentType = profilePhoto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return "Only image files are allowed";
            }
            if (profilePhoto.getSize() > 5 * 1024 * 1024) {
                log.error("File size exceeds 5MB limit");
                return "File size should not exceed 5MB";
            }
            teacherDTO.setProfilePhoto(profilePhoto.getBytes());
        } else {
            teacherDTO.setProfilePhoto(null);
        }
        return null;
    }

    private ResponseEntity<ResponseDTO> buildResponse(String result, TeacherDTO teacherDTO) {
        switch (result) {
            case VarList.RES_SUCCESS:
                return createSuccessResponse("Teacher saved successfully", teacherDTO);
            case VarList.RES_ERROR:
                return createErrorResponse(VarList.RES_ERROR, "Please enter Teacher Data", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE:
                return createErrorResponse(VarList.RES_DUPLICATE,
                        "Teacher code already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_NIC:
                return createErrorResponse(VarList.RES_DUPLICATE_NIC,
                        "Teacher NIC duplicate", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_EMAIL:
                return createErrorResponse(VarList.RES_DUPLICATE_EMAIL,
                        "Teacher Email duplicate", HttpStatus.CONFLICT);
            case VarList.RES_PASSWORD_ALREADY_EXISTS:
                return createErrorResponse(VarList.RES_PASSWORD_ALREADY_EXISTS,
                        "Password already exists in the system", HttpStatus.CONFLICT);
            case VarList.RES_CLASS_NOT_FOUND:
                return createErrorResponse(VarList.RES_CLASS_NOT_FOUND,
                        "Teacher must have at least one Class", HttpStatus.BAD_REQUEST);
            case VarList.RES_SUBJECT_NOT_FOUND:
                return createErrorResponse(VarList.RES_SUBJECT_NOT_FOUND,
                        "Subject not found", HttpStatus.BAD_REQUEST);
            case VarList.RES_INVALID_INPUT:
                return createErrorResponse(VarList.RES_INVALID_INPUT,
                        "One or more class IDs are invalid", HttpStatus.BAD_REQUEST);
            default:
                return createErrorResponse(VarList.RES_FAILURE,
                        "Failed to save teacher", HttpStatus.BAD_REQUEST);
        }
    }

    // Update an existing teacher
    @PutMapping(value = "/updateTeacher", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> updateTeacher(
            @RequestParam("teacher") String teacherJson,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto) {
        try {
            TeacherDTO teacherDTO = parseTeacherJson(teacherJson);

            // Validate profile photo
            String fileValidationResult = validateAndProcessFile(profilePhoto, teacherDTO);
            if (fileValidationResult != null) {
                return createErrorResponse(VarList.RES_ERROR, fileValidationResult, HttpStatus.BAD_REQUEST);
            }
            // Update the teacher
            String result = teacherService.updateTeacher(teacherDTO);
            return buildUpdateResponse(result, teacherDTO);

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
            log.error("Unexpected error during teacher update", ex);
            return createErrorResponse(VarList.RES_ERROR, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<ResponseDTO> buildUpdateResponse(String result, TeacherDTO teacherDTO) {
        switch (result) {
            case VarList.RES_SUCCESS:
                return createSuccessResponse("Teacher updated successfully", teacherDTO);
            case VarList.RES_ERROR:
                return createErrorResponse(VarList.RES_ERROR, "Please enter teacher data", HttpStatus.BAD_REQUEST);
            case VarList.RES_NO_DATE_FOUND:
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Teacher not found or is deleted", HttpStatus.NOT_FOUND);
            case VarList.RES_DUPLICATE:
                return createErrorResponse(VarList.RES_DUPLICATE, "Teacher code already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_NIC:
                return createErrorResponse(VarList.RES_DUPLICATE_NIC, "NIC already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_EMAIL:
                return createErrorResponse(VarList.RES_DUPLICATE_EMAIL, "Email already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_USERNAME:
                return createErrorResponse(VarList.RES_DUPLICATE_USERNAME, "Username already exists", HttpStatus.CONFLICT);
            case VarList.RES_CLASS_NOT_FOUND:
                return createErrorResponse(VarList.RES_CLASS_NOT_FOUND, "Teacher must have at least one class", HttpStatus.BAD_REQUEST);
            case VarList.RES_SUBJECT_NOT_FOUND:
                return createErrorResponse(VarList.RES_SUBJECT_NOT_FOUND, "Subject not found", HttpStatus.BAD_REQUEST);
            case VarList.RES_INVALID_INPUT:
                return createErrorResponse(VarList.RES_INVALID_INPUT, "One or more class IDs are invalid", HttpStatus.BAD_REQUEST);
            default:
                log.error("Teacher update failed with result code: {}", result);
                return createErrorResponse(VarList.RES_FAILURE, "Failed to update teacher", HttpStatus.INTERNAL_SERVER_ERROR);
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

    // Get all teachers
    @GetMapping("/getAllTeachers")
    public ResponseEntity getAllTeachers() {
        try {
            List<TeacherDTO> teacherList = teacherService.getAllTeachers(); // Fetch all teachers
            responseDTO.setCode(VarList.RES_SUCCESS);
            responseDTO.setMessage("Teachers fetched successfully");
            responseDTO.setContent(teacherList);
            return new ResponseEntity(responseDTO, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error fetching all teachers: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a teacher by ID
    @DeleteMapping("/deleteTeacher/{id}")
    public ResponseEntity deleteTeacher(@PathVariable int id) {
        try {
            String res = teacherService.deleteTeacher(id); // Delete teacher using teacherService
            if (res.equals("00")) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("No Teacher Available for this ID");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(e.getMessage());
            responseDTO.setContent(e);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search for a specific teacher by ID
    @GetMapping("/searchTeacher/{id}")
    public ResponseEntity searchTeacher(@PathVariable int id) {
        try {
            TeacherDTO teacherDTO = teacherService.getTeacherById(id); // Fetch teacher using teacherService
            if (teacherDTO != null) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Teacher found successfully");
                responseDTO.setContent(teacherDTO);
                return new ResponseEntity(responseDTO, HttpStatus.OK);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("Teacher Not Found for this ID");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.NOT_FOUND); //404
            }
        } catch (Exception ex) {
            log.error("Error fetching teacher by ID: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get teachers by class ID
    @GetMapping("/getTeachersByClass/{classId}")
    public ResponseEntity getTeachersByClass(@PathVariable Integer classId) {
        try {
            List<TeacherDTO> teacherList = teacherService.getTeachersByClass(classId);
            if (!teacherList.isEmpty()) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Teachers found successfully for the class");
                responseDTO.setContent(teacherList);
                return new ResponseEntity(responseDTO, HttpStatus.OK);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("No Teachers found for this class");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching teachers by class ID: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get teachers by subject ID
    @GetMapping("/getTeachersBySubject/{subjectId}")
    public ResponseEntity getTeachersBySubject(@PathVariable Integer subjectId) {
        try {
            List<TeacherDTO> teacherList = teacherService.getTeachersBySubject(subjectId);
            if (!teacherList.isEmpty()) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Teachers found successfully for the subject");
                responseDTO.setContent(teacherList);
                return new ResponseEntity(responseDTO, HttpStatus.OK);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("No Teachers found for this subject");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching teachers by subject ID: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all classes
    @GetMapping("/getAllClasses")
    public ResponseEntity<ResponseDTO> getAllClasses() {
        try {
            List<ClassDTO> classList = classManagementService.getAllClasses();
            return createSuccessResponse("Classes fetched successfully", classList);
        } catch (Exception ex) {
            log.error("Error fetching all classes: ", ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching classes: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all subjects
    @GetMapping("/getAllSubjects")
    public ResponseEntity<ResponseDTO> getAllSubjects() {
        try {
            List<SubjectDTO> subjectList = subjectService.GetAllSubjects();
            return createSuccessResponse("Subjects fetched successfully", subjectList);
        } catch (Exception ex) {
            log.error("Error fetching all classes: ", ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching subjects: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all Grades
    @GetMapping("/getAllGrades")
    public ResponseEntity GetAllGrades() {
        try {
            List<GradeDTO> gradeList = gradeService.getAllGrades();
            responseDTO.setCode(VarList.RES_SUCCESS);
            responseDTO.setMessage("Success");
            responseDTO.setContent(gradeList);
            return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search for a specific class by ID
    @GetMapping("/searchClass/{id}")
    public ResponseEntity<ResponseDTO> searchClass(@PathVariable int id) {
        try {
            ClassDTO classDTO = classManagementService.getClassById(id);
            if (classDTO != null) {
                return createSuccessResponse("Class found successfully", classDTO);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Class not found for this ID", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching class by ID {}: ", id, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching class: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update an existing class
    @PutMapping(value = "/updateClass", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> updateClass(@RequestBody String classJson) {
        try {
            ClassDTO classDTO = parseClassJson(classJson);
            String result = classManagementService.updateClass(classDTO);
            return buildClassUpdateResponse(result, classDTO);

        } catch (JsonProcessingException ex) {
            log.error("JSON parsing error", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid JSON format: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid input data", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Unexpected error during class update", ex);
            return createErrorResponse(VarList.RES_ERROR, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Method to convert JSON string to ClassDTO
    private ClassDTO parseClassJson(String classJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.readValue(classJson, ClassDTO.class);
    }

    // Helper method to build update class response
    private ResponseEntity<ResponseDTO> buildClassUpdateResponse(String result, ClassDTO classDTO) {
        switch (result) {
            case VarList.RES_SUCCESS:
                return createSuccessResponse("Class updated successfully", classDTO);
            case VarList.RES_ERROR:
                return createErrorResponse(VarList.RES_ERROR, "Please enter valid class data", HttpStatus.BAD_REQUEST);
            case VarList.RES_NO_DATE_FOUND:
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Class not found or is deleted", HttpStatus.NOT_FOUND);
            case VarList.RES_DUPLICATE:
                return createErrorResponse(VarList.RES_DUPLICATE, "Class with same name, date and time already exists", HttpStatus.CONFLICT);
            case VarList.RES_GRADE_NOT_FOUND:
                return createErrorResponse(VarList.RES_GRADE_NOT_FOUND, "Grade not found", HttpStatus.BAD_REQUEST);
            case VarList.RES_NO_SUBJECTS:
                return createErrorResponse(VarList.RES_NO_SUBJECTS, "Subject not found", HttpStatus.BAD_REQUEST);
            default:
                log.error("Class update failed with result code: {}", result);
                return createErrorResponse(VarList.RES_FAILURE, "Failed to update class", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // ===== STUDENT MANAGEMENT =====

    //     Save a new student
    @PostMapping(value = "/saveStudent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> SaveStudent(
            @RequestParam("student") String studentJson,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        try {
            // Convert JSON string to StudentDTO with proper date handling
            StudentDTO studentDTO = parseStudentJson(studentJson);

            // Validate profile photo
            String fileValidationResult = validateAndProcessFile(profilePhoto, studentDTO);
            if (fileValidationResult != null) {
                return createErrorResponse(VarList.RES_ERROR, fileValidationResult, HttpStatus.BAD_REQUEST);
            }

            String result = studentService.saveStudent(studentDTO);  // Save the student
            return buildStudentSaveResponse(result, studentDTO);

        } catch (JsonProcessingException ex) {
            return createErrorResponse(VarList.RES_ERROR, "Invalid JSON format: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException ex) {
            return createErrorResponse(VarList.RES_ERROR, "Error processing file: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid input data", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
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

    // Helper method to build Save Student response
    private ResponseEntity<ResponseDTO> buildStudentSaveResponse(String result, StudentDTO studentDTO) {
        switch (result) {
            case VarList.RES_SUCCESS:
                return createSuccessResponse("Student saved successfully", studentDTO);
            case VarList.RES_ERROR:
                return createErrorResponse(VarList.RES_ERROR, "Please enter Student Data", HttpStatus.CONFLICT);// 409
            case VarList.RES_DUPLICATE:
                return createErrorResponse(VarList.RES_DUPLICATE,
                        "Student registration number already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_NIC:
                return createErrorResponse(VarList.RES_DUPLICATE_NIC,
                        "Student NIC duplicate", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_EMAIL:
                return createErrorResponse(VarList.RES_DUPLICATE_EMAIL,
                        "Student Email duplicate", HttpStatus.CONFLICT);
            case VarList.RES_NO_SUBJECTS:
                return createErrorResponse(VarList.RES_CLASS_NOT_FOUND,
                        "Student must have at least one Class", HttpStatus.BAD_REQUEST);
            case VarList.RES_CLASS_NOT_FOUND:
                return createErrorResponse(VarList.RES_CLASS_NOT_FOUND,
                        "Class not found", HttpStatus.BAD_REQUEST);
            case VarList.RES_INVALID_INPUT:
                return createErrorResponse(VarList.RES_INVALID,
                        "One or more subject IDs are invalid", HttpStatus.BAD_REQUEST);
            default:
                return createErrorResponse(VarList.RES_FAILURE,
                        "Failed to save student", HttpStatus.BAD_REQUEST);
        }
    }

    // Search for a specific student by calss
    @GetMapping("/getStudentByClass/{classId}")
    public ResponseEntity GetStudentByClass(@PathVariable Integer classId) {
        try {
            List<StudentDTO> studentList = studentService.getStudentsByClass(classId); // Fetch student using studentService
            if (studentList != null) {
                log.error("Students found successfully");
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Students found successfully");
                responseDTO.setContent(studentList);
                return new ResponseEntity(responseDTO, HttpStatus.OK);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                log.error("Students Not Found for this class");
                responseDTO.setMessage("Students Not Found for this class");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.NOT_FOUND); //404
            }
        } catch (Exception ex) {
            log.error("Error fetching student by class: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }// Search for a specific student by ID

    @GetMapping("/getStudentProfile/{id}")
    public ResponseEntity GetStudentProfile(@PathVariable int id) {
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


    // Update an existing student
    @PutMapping(value = "/editStudent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
            String result = studentService.updateStudent(studentDTO);
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


    // ===== ENDPOINTS FOR GETTING CLASSES BY TEACHER =====

    /**
     * Get all classes associated with a specific teacher by teacher ID
     *
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing list of classes associated with the teacher
     */
    @GetMapping("/getClassesByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getClassesByTeacher(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching classes for teacher ID: {}", teacherId);

            List<ClassDTO> classList = classManagementService.getClassesByTeacher(teacherId);

            if (!classList.isEmpty()) {
                return createSuccessResponse("Classes found successfully for the teacher", classList);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                        "No classes found for teacher with ID: " + teacherId, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching classes for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching classes for teacher: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get classes associated with a teacher by teacher code
     *
     * @param teacherCode The teacher code
     * @return ResponseEntity containing list of classes associated with the teacher
     */
    @GetMapping("/getClassesByTeacherCode/{teacherCode}")
    public ResponseEntity<ResponseDTO> getClassesByTeacherCode(@PathVariable String teacherCode) {
        try {
            log.info("Fetching classes for teacher code: {}", teacherCode);

            List<ClassDTO> classList = classManagementService.getClassesByTeacherCode(teacherCode);

            if (!classList.isEmpty()) {
                return createSuccessResponse("Classes found successfully for the teacher", classList);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                        "No classes found for teacher with code: " + teacherCode, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching classes for teacher code {}: ", teacherCode, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching classes for teacher: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get classes associated with a teacher with optional filters
     *
     * @param teacherId The ID of the teacher
     * @param gradeId   Optional grade filter
     * @param subjectId Optional subject filter
     * @return ResponseEntity containing filtered list of classes associated with the teacher
     */
    @GetMapping("/getClassesByTeacherWithFilters/{teacherId}")
    public ResponseEntity<ResponseDTO> getClassesByTeacherWithFilters(
            @PathVariable Integer teacherId,
            @RequestParam(required = false) Integer gradeId,
            @RequestParam(required = false) Integer subjectId) {
        try {
            log.info("Fetching classes for teacher ID: {} with filters - Grade: {}, Subject: {}",
                    teacherId, gradeId, subjectId);

            List<ClassDTO> classList = classManagementService.getClassesByTeacherWithFilters(
                    teacherId, gradeId, subjectId);

            if (!classList.isEmpty()) {
                String message = String.format("Classes found successfully for teacher (ID: %d)", teacherId);
                if (gradeId != null) message += " in grade " + gradeId;
                if (subjectId != null) message += " for subject " + subjectId;

                return createSuccessResponse(message, classList);
            } else {
                String message = "No classes found for teacher with applied filters";
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, message, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching filtered classes for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching classes for teacher: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get classes by teacher with pagination support
     *
     * @param teacherId The ID of the teacher
     * @param page      Page number (0-based)
     * @param size      Page size
     * @return ResponseEntity containing paginated list of classes
     */
    @GetMapping("/getClassesByTeacherPaginated/{teacherId}")
    public ResponseEntity<ResponseDTO> getClassesByTeacherPaginated(
            @PathVariable Integer teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Fetching paginated classes for teacher ID: {} - Page: {}, Size: {}",
                    teacherId, page, size);

            List<ClassDTO> allClasses = classManagementService.getClassesByTeacher(teacherId);

            // Simple pagination logic
            int totalElements = allClasses.size();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            if (startIndex >= totalElements) {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                        "Page not found", HttpStatus.NOT_FOUND);
            }

            List<ClassDTO> paginatedClasses = allClasses.subList(startIndex, endIndex);

            // Create pagination info (you might want to create a proper pagination response DTO)
            String paginationInfo = String.format("Page %d of %d (Total: %d classes)",
                    page + 1, (int) Math.ceil((double) totalElements / size), totalElements);

            return createSuccessResponse("Classes fetched successfully - " + paginationInfo, paginatedClasses);

        } catch (Exception ex) {
            log.error("Error fetching paginated classes for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching classes for teacher: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // ===== ENDPOINTS FOR GETTING CLASSES WITH STUDENT COUNT BY TEACHER =====

    /**
     * Get classes with student count for a specific teacher
     *
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing list of classes with student counts
     */
    @GetMapping("/getClassesWithStudentCountByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getClassesWithStudentCountByTeacher(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching classes with student count for teacher ID: {}", teacherId);

            // Validate teacher ID
            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Invalid teacher ID provided", HttpStatus.BAD_REQUEST);
            }

            List<ClassWithStudentCountDTO> classesWithStudentCount =
                    classManagementService.getClassesWithStudentCountByTeacher(teacherId);

            if (!classesWithStudentCount.isEmpty()) {
                String message = String.format("Found %d class(es) with student counts for teacher ID: %d",
                        classesWithStudentCount.size(), teacherId);
                return createSuccessResponse(message, classesWithStudentCount);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                        "No classes found for teacher with ID: " + teacherId, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching classes with student count for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching classes with student count: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get classes with student count for a specific teacher (Alternative implementation)
     * This method uses entity approach and calculates student count separately
     *
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing list of classes with student counts
     */
    @GetMapping("/getClassesWithStudentCountByTeacherAlternative/{teacherId}")
    public ResponseEntity<ResponseDTO> getClassesWithStudentCountByTeacherAlternative(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching classes with student count (alternative method) for teacher ID: {}", teacherId);

            // Validate teacher ID
            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Invalid teacher ID provided", HttpStatus.BAD_REQUEST);
            }

            List<ClassWithStudentCountDTO> classesWithStudentCount =
                    classManagementService.getClassesWithStudentCountByTeacherAlternative(teacherId);

            if (!classesWithStudentCount.isEmpty()) {
                String message = String.format("Found %d class(es) with student counts for teacher ID: %d (Alternative method)",
                        classesWithStudentCount.size(), teacherId);
                return createSuccessResponse(message, classesWithStudentCount);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                        "No classes found for teacher with ID: " + teacherId, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching classes with student count (alternative) for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching classes with student count (alternative): " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get student count for a specific class
     *
     * @param classId The ID of the class
     * @return ResponseEntity containing the student count for the class
     */
    @GetMapping("/getStudentCountForClass/{classId}")
    public ResponseEntity<ResponseDTO> getStudentCountForClass(@PathVariable Integer classId) {
        try {
            log.info("Fetching student count for class ID: {}", classId);

            // Validate class ID
            if (classId == null || classId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Invalid class ID provided", HttpStatus.BAD_REQUEST);
            }

            Long studentCount = classManagementService.getStudentCountForClass(classId);

            if (studentCount != null) {
                String message = String.format("Class ID %d has %d student(s)", classId, studentCount);
                return createSuccessResponse(message, studentCount);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND,
                        "Class not found with ID: " + classId, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching student count for class ID {}: ", classId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching student count for class: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get class count for a specific teacher (Optimized version)
     *
     * @param teacherId The ID of the teacher
     * @return ResponseEntity containing the count of classes for the teacher
     */
    @GetMapping("/getClassCountByTeacher/{teacherId}")
    public ResponseEntity<ResponseDTO> getClassCountByTeacher(@PathVariable Integer teacherId) {
        try {
            log.info("Fetching class count for teacher ID: {}", teacherId);

            // Validate teacher ID
            if (teacherId == null || teacherId <= 0) {
                return createErrorResponse(VarList.RES_ERROR,
                        "Invalid teacher ID provided", HttpStatus.BAD_REQUEST);
            }

            Long classCount = classManagementService.getClassCountByTeacher(teacherId);

//            List<ClassDTO> classList = classManagementService.getClassesByTeacher(teacherId);
//            int classCount = classList.size();

            String message = String.format("Teacher has %d class(es)", classCount);
            return createSuccessResponse(message, classCount);

        } catch (Exception ex) {
            log.error("Error fetching class count for teacher ID {}: ", teacherId, ex);
            return createErrorResponse(VarList.RES_ERROR,
                    "Error fetching class count for teacher: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}

