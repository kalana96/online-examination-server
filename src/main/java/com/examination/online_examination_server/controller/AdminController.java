package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.*;
import com.examination.online_examination_server.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ClassManagementService classManagementService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ResponseDTO responseDTO;

    @Autowired
    private ExamService examService;



    // ===== ADMIN MANAGEMENT =====

    // Save a new admin
    @PostMapping(value = "/saveAdmin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> saveAdmin(
            @RequestParam("admin") String adminJson,
            @RequestParam("userDetails") String userDetailsJson,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        try {
            // Convert JSON strings to DTOs
            AdminDTO adminDTO = parseAdminJson(adminJson);
            UserRegistrationDTO userDetails = parseUserDetailsJson(userDetailsJson);

            // Validate and process profile photo
            String fileValidationResult = validateAndProcessFile(profilePhoto, adminDTO);
            if (fileValidationResult != null) {
                return createErrorResponse(VarList.RES_ERROR, fileValidationResult, HttpStatus.BAD_REQUEST);
            }

            String result = adminService.saveAdmin(adminDTO, userDetails);
            return buildAdminSaveResponse(result, adminDTO);

        } catch (JsonProcessingException ex) {
            log.error("JSON parsing error", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid JSON format: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException ex) {
            log.error("File processing error", ex);
            return createErrorResponse(VarList.RES_ERROR, "Error processing file: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid input data", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Unexpected error during admin save", ex);
            return createErrorResponse(VarList.RES_ERROR, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update an existing admin
    @PutMapping(value = "/updateAdmin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO> updateAdmin(
            @RequestParam("admin") String adminJson,
            @RequestParam(value = "userDetails", required = false) String userDetailsJson,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        try {
            // Convert JSON strings to DTOs
            AdminDTO adminDTO = parseAdminJson(adminJson);
            UserRegistrationDTO userDetails = null;
            if (userDetailsJson != null && !userDetailsJson.trim().isEmpty()) {
                userDetails = parseUserDetailsJson(userDetailsJson);
            }
            // Validate admin DTO
            if (adminDTO == null) {
                return createErrorResponse(VarList.RES_ERROR, "Admin data is required", HttpStatus.BAD_REQUEST);
            }
            // Validate and process profile photo
            String fileValidationResult = validateAndProcessFile(profilePhoto, adminDTO);
            if (fileValidationResult != null) {
                return createErrorResponse(VarList.RES_ERROR, fileValidationResult, HttpStatus.BAD_REQUEST);
            }

            String result = adminService.updateAdmin(adminDTO, userDetails);
            return buildAdminUpdateResponse(result, adminDTO);

        } catch (JsonProcessingException ex) {
            log.error("JSON parsing error", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid JSON format: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException ex) {
            log.error("File processing error", ex);
            return createErrorResponse(VarList.RES_ERROR, "Error processing file: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid input data", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Unexpected error during admin update", ex);
            return createErrorResponse(VarList.RES_ERROR, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all admins
    @GetMapping("/getAllAdmins")
    public ResponseEntity<ResponseDTO> getAllAdmins() {
        try {
            List<AdminDTO> adminList = adminService.getAllAdmins();
            return createSuccessResponse("Admins fetched successfully", adminList);
        } catch (Exception ex) {
            log.error("Error fetching all admins", ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching admins: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get admin by ID
    @GetMapping("/getAdmin/{id}")
    public ResponseEntity<ResponseDTO> getAdminById(@PathVariable int id) {
        try {
            AdminDTO adminDTO = adminService.getAdminById(id);
            if (adminDTO != null) {
                return createSuccessResponse("Admin found successfully", adminDTO);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Admin not found for ID: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching admin by ID: {}", id, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching admin: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get admin by ID with user data
    @GetMapping("/getAdminWithUser/{id}")
    public ResponseEntity<ResponseDTO> getAdminWithUserById(@PathVariable int id) {
        try {
            AdminDTO adminDTO = adminService.getAdminWithUserById(id);
            if (adminDTO != null) {
                return createSuccessResponse("Admin with user data found successfully", adminDTO);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Admin not found for ID: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching admin with user data by ID: {}", id, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching admin with user data: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search admin by ID
    @GetMapping("/searchAdmin/{id}")
    public ResponseEntity<ResponseDTO> searchAdmin(@PathVariable int id) {
        try {
            AdminDTO adminDTO = adminService.searchAdmin(id);
            if (adminDTO != null) {
                return createSuccessResponse("Admin found successfully", adminDTO);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Admin not found for ID: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error searching admin by ID: {}", id, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error searching admin: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get admin by admin code
    @GetMapping("/getAdminByCode/{adminCode}")
    public ResponseEntity<ResponseDTO> getAdminByAdminCode(@PathVariable String adminCode) {
        try {
            AdminDTO adminDTO = adminService.getAdminByAdminCode(adminCode);
            if (adminDTO != null) {
                return createSuccessResponse("Admin found successfully", adminDTO);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Admin not found for admin code: " + adminCode, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching admin by admin code: {}", adminCode, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching admin: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get admin by email
    @GetMapping("/getAdminByEmail/{email}")
    public ResponseEntity<ResponseDTO> getAdminByEmail(@PathVariable String email) {
        try {
            AdminDTO adminDTO = adminService.getAdminByEmail(email);
            if (adminDTO != null) {
                return createSuccessResponse("Admin found successfully", adminDTO);
            } else {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Admin not found for email: " + email, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Error fetching admin by email: {}", email, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching admin: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete admin by ID
    @DeleteMapping("/deleteAdmin/{id}")
    public ResponseEntity<ResponseDTO> deleteAdmin(@PathVariable int id) {
        try {
            String result = adminService.deleteAdmin(id);
            if (VarList.RES_SUCCESS.equals(result)) {
                return createSuccessResponse("Admin deleted successfully", null);
            } else if (VarList.RES_NO_DATE_FOUND.equals(result)) {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Admin not found or already deleted", HttpStatus.NOT_FOUND);
            } else {
                return createErrorResponse(VarList.RES_ERROR, "Failed to delete admin", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            log.error("Error deleting admin with ID: {}", id, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error deleting admin: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Toggle admin status (activate/deactivate)
    @PutMapping("/toggleStatus/{id}")
    public ResponseEntity<ResponseDTO> toggleAdminStatus(@PathVariable int id, @RequestParam boolean isActive) {
        try {
            String result = adminService.toggleAdminStatus(id, isActive);
            if (VarList.RES_SUCCESS.equals(result)) {
                String message = isActive ? "Admin activated successfully" : "Admin deactivated successfully";
                return createSuccessResponse(message, null);
            } else if (VarList.RES_NO_DATE_FOUND.equals(result)) {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Admin not found", HttpStatus.NOT_FOUND);
            } else {
                return createErrorResponse(VarList.RES_ERROR, "Failed to toggle admin status", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            log.error("Error toggling admin status for ID: {}", id, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error toggling admin status: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to parse Admin JSON
    private AdminDTO parseAdminJson(String adminJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.readValue(adminJson, AdminDTO.class);
    }

    // Helper method to parse UserDetails JSON
    private UserRegistrationDTO parseUserDetailsJson(String userDetailsJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.readValue(userDetailsJson, UserRegistrationDTO.class);
    }

    // Helper method to validate and process profile photo
    private String validateAndProcessFile(MultipartFile profilePhoto, AdminDTO adminDTO) throws IOException {
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            String contentType = profilePhoto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return "Only image files are allowed";
            }
            if (profilePhoto.getSize() > 5 * 1024 * 1024) {
                log.error("File size exceeds 5MB limit");
                return "File size should not exceed 5MB";
            }
            adminDTO.setProfilePhoto(profilePhoto.getBytes());
        } else {
            adminDTO.setProfilePhoto(null);
        }
        return null;
    }

    // Helper method to build save response
    private ResponseEntity<ResponseDTO> buildAdminSaveResponse(String result, AdminDTO adminDTO) {
        switch (result) {
            case VarList.RES_SUCCESS:
                return createSuccessResponse("Admin saved successfully", adminDTO);
            case VarList.RES_ERROR:
                return createErrorResponse(VarList.RES_ERROR, "Please enter valid admin data", HttpStatus.BAD_REQUEST);
            case VarList.RES_DUPLICATE:
                return createErrorResponse(VarList.RES_DUPLICATE, "Admin code already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_EMAIL:
                return createErrorResponse(VarList.RES_DUPLICATE_EMAIL, "Admin email already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_USERNAME:
                return createErrorResponse(VarList.RES_DUPLICATE_USERNAME, "Username already exists", HttpStatus.CONFLICT);
            case VarList.RES_PASSWORD_ALREADY_EXISTS:
                return createErrorResponse(VarList.RES_PASSWORD_ALREADY_EXISTS, "Password already exists in the system", HttpStatus.CONFLICT);
            default:
                return createErrorResponse(VarList.RES_FAILURE, "Failed to save admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to build update response
    private ResponseEntity<ResponseDTO> buildAdminUpdateResponse(String result, AdminDTO adminDTO) {
        switch (result) {
            case VarList.RES_SUCCESS:
                return createSuccessResponse("Admin updated successfully", adminDTO);
            case VarList.RES_ERROR:
                return createErrorResponse(VarList.RES_ERROR, "Please enter valid admin data", HttpStatus.BAD_REQUEST);
            case VarList.RES_NO_DATE_FOUND:
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "Admin not found or is deleted", HttpStatus.NOT_FOUND);
            case VarList.RES_DUPLICATE:
                return createErrorResponse(VarList.RES_DUPLICATE, "Admin code already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_EMAIL:
                return createErrorResponse(VarList.RES_DUPLICATE_EMAIL, "Admin email already exists", HttpStatus.CONFLICT);
            case VarList.RES_DUPLICATE_USERNAME:
                return createErrorResponse(VarList.RES_DUPLICATE_USERNAME, "Username already exists", HttpStatus.CONFLICT);
            default:
                log.error("Admin update failed with result code: {}", result);
                return createErrorResponse(VarList.RES_FAILURE, "Failed to update admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // ===== SUBJECT MANAGEMENT =====

    // Save a new subject
    @PostMapping("/saveSubject")
    public ResponseEntity SaveSubject(@RequestBody SubjectDTO subjectDTO) {
        try {
            String resp = subjectService.SaveSubject(subjectDTO); // Save the subject using subjectService
            if (resp.equals("00")) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(subjectDTO);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            } else if (resp.equals("06")) {
                responseDTO.setCode(VarList.RES_DUPLICATE);
                responseDTO.setMessage("Subject already exists");
                responseDTO.setContent(subjectDTO);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            } else {
                responseDTO.setCode(VarList.RES_FAILURE);
                responseDTO.setMessage("Error");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all subjects
    @GetMapping("/getAllSubjects")
    public ResponseEntity GetAllSubjects() {
        try {
            List<SubjectDTO> subjectList = subjectService.GetAllSubjects(); // Fetch all subjects
            responseDTO.setCode(VarList.RES_SUCCESS);
            responseDTO.setMessage("Success");
            responseDTO.setContent(subjectList);
            return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update an existing subject
    @PutMapping("/updateSubject")
    public ResponseEntity UpdateSubject(@RequestBody SubjectDTO subjectDTO) {
        try {
            String resp = subjectService.UpdateSubject(subjectDTO); // Update subject using subjectService
            if (resp.equals("00")) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(subjectDTO);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            } else if (resp.equals("01")) {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("Subject Not Found");
                responseDTO.setContent(subjectDTO);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            } else {
                responseDTO.setCode(VarList.RES_FAILURE);
                responseDTO.setMessage("Error");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a subject by ID
    @DeleteMapping("/deleteSubject/{id}")
    public ResponseEntity deleteSubject(@PathVariable int id) {
        try {
            String res = subjectService.deleteSubject(id); // Delete subject using subjectService
            if (res.equals("00")) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("No Subject Available for this ID");
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

    // Search for a specific subject by ID
    @GetMapping("/searchSubject/{id}")
    public ResponseEntity SearchSubject(@PathVariable int id) {
        try {
            SubjectDTO subjectDTO = subjectService.SearchSubject(id); // Fetch subject using subjectService
            if (subjectDTO != null) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(subjectDTO);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("Subject Not Found for this ID");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // ===== CLASS MANAGEMENT =====

    // Save a new class
    @PostMapping(value = "/saveClass", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO> saveClass(@RequestBody String classJson) {
        try {
            // Convert JSON string to ClassDTO with proper date handling
            ClassDTO classDTO = parseClassJson(classJson);

            String result = classManagementService.saveClass(classDTO);
            return buildClassSaveResponse(result, classDTO);

        } catch (JsonProcessingException ex) {
            log.error("JSON parsing error", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid JSON format: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid input data", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Unexpected error during class save", ex);
            return createErrorResponse(VarList.RES_ERROR, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Alternative endpoint accepting ClassDTO directly
    @PostMapping("/saveClassDirect")
    public ResponseEntity<ResponseDTO> saveClassDirect(@RequestBody ClassDTO classDTO) {
        try {
            String result = classManagementService.saveClass(classDTO);
            return buildClassSaveResponse(result, classDTO);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid input data", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Unexpected error during class save", ex);
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

    // Helper method to build save class response
    private ResponseEntity<ResponseDTO> buildClassSaveResponse(String result, ClassDTO classDTO) {
        switch (result) {
            case VarList.RES_SUCCESS:
                return createSuccessResponse("Class saved successfully", classDTO);
            case VarList.RES_ERROR:
                return createErrorResponse(VarList.RES_ERROR, "Please enter valid class data", HttpStatus.BAD_REQUEST);
            case VarList.RES_DUPLICATE:
                return createErrorResponse(VarList.RES_DUPLICATE,
                        "Class with same name, date and time already exists", HttpStatus.CONFLICT);
            case VarList.RES_GRADE_NOT_FOUND:
                return createErrorResponse(VarList.RES_GRADE_NOT_FOUND,
                        "Grade not found", HttpStatus.BAD_REQUEST);
            case VarList.RES_NO_SUBJECTS:
                return createErrorResponse(VarList.RES_NO_SUBJECTS,
                        "Subject not found", HttpStatus.BAD_REQUEST);
            default:
                return createErrorResponse(VarList.RES_FAILURE,
                        "Failed to save class", HttpStatus.BAD_REQUEST);
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

    // Alternative endpoint accepting ClassDTO directly
    @PutMapping("/updateClassDirect")
    public ResponseEntity<ResponseDTO> updateClassDirect(@RequestBody ClassDTO classDTO) {
        try {
            String result = classManagementService.updateClass(classDTO);
            return buildClassUpdateResponse(result, classDTO);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid input data", ex);
            return createErrorResponse(VarList.RES_ERROR, "Invalid input data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Unexpected error during class update", ex);
            return createErrorResponse(VarList.RES_ERROR, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    // Delete a class by ID
    @DeleteMapping("/deleteClass/{id}")
    public ResponseEntity<ResponseDTO> deleteClass(@PathVariable int id) {
        try {
            String result = classManagementService.deleteClass(id);
            if (result.equals(VarList.RES_SUCCESS)) {
                return createSuccessResponse("Class deleted successfully", null);
            } else if (result.equals(VarList.RES_NO_DATE_FOUND)) {
                return createErrorResponse(VarList.RES_NO_DATE_FOUND, "No class available for this ID", HttpStatus.NOT_FOUND);
            } else {
                return createErrorResponse(VarList.RES_ERROR, "Failed to delete class", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            log.error("Error deleting class with ID {}: ", id, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error deleting class: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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

    // Get classes by subject ID
    @GetMapping("/getClassesBySubject/{subjectId}")
    public ResponseEntity<ResponseDTO> getClassesBySubject(@PathVariable Integer subjectId) {
        try {
            List<ClassDTO> classList = classManagementService.getClassesBySubject(subjectId);
            return createSuccessResponse("Classes fetched successfully", classList);
        } catch (Exception ex) {
            log.error("Error fetching classes by subject ID {}: ", subjectId, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching classes by subject: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get classes by grade ID
    @GetMapping("/getClassesByGrade/{gradeId}")
    public ResponseEntity<ResponseDTO> getClassesByGrade(@PathVariable Integer gradeId) {
        try {
            List<ClassDTO> classList = classManagementService.getClassesByGrade(gradeId);
            return createSuccessResponse("Classes fetched successfully", classList);
        } catch (Exception ex) {
            log.error("Error fetching classes by grade ID {}: ", gradeId, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error fetching classes by grade: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search classes by name
    @GetMapping("/searchClassesByName/{className}")
    public ResponseEntity<ResponseDTO> searchClassesByName(@PathVariable String className) {
        try {
            List<ClassDTO> classList = classManagementService.searchClassesByName(className);
            return createSuccessResponse("Classes found successfully", classList);
        } catch (Exception ex) {
            log.error("Error searching classes by name '{}': ", className, ex);
            return createErrorResponse(VarList.RES_ERROR, "Error searching classes: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // ===== GRADE MANAGEMENT =====

    @PostMapping("/saveGrade")
    public ResponseEntity SaveGrade (@RequestBody GradeDTO gradeDTO){
        try {
            String resp = gradeService.saveGrade(gradeDTO);
            if (resp.equals("00")){
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(gradeDTO);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            } else if (resp.equals("06")) {
                responseDTO.setCode(VarList.RES_DUPLICATE);
                responseDTO.setMessage("Grade already exist");
                responseDTO.setContent(gradeDTO);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }else {
                responseDTO.setCode(VarList.RES_FAILURE);
                responseDTO.setMessage("Error");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception ex){
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllGrades")
    public ResponseEntity GetAllGrades(){
        try {
            List<GradeDTO> gradeList = gradeService.getAllGrades();
            responseDTO.setCode(VarList.RES_SUCCESS);
            responseDTO.setMessage("Success");
            responseDTO.setContent(gradeList);
            return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
        }catch (Exception ex){
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateGrade")
    public ResponseEntity UpdateGrade(@RequestBody GradeDTO gradeDTO){
        try {
            String resp = gradeService.updateGrade(gradeDTO);
            if (resp.equals("00")){
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(gradeDTO);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            } else if (resp.equals("01")) {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("Grade Not Found");
                responseDTO.setContent(gradeDTO);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }else {
                responseDTO.setCode(VarList.RES_FAILURE);
                responseDTO.setMessage("Error");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception ex){
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteGrede/{id}")
    public ResponseEntity deleteGrade(@PathVariable int id){
        try {
            String res = gradeService.deleteGrade(id);
            if (res.equals("00")) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("No Grade Available For this gradeId");
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

//    @GetMapping("/searchClass/{id}")
//    public ResponseEntity SearchEmployee(@RequestBody int id){
//        try {
//            GradeDTO employeeDTO = gradeService.SearchClass(id);
//            if (employeeDTO !=null){
//                responseDTO.setCode(VarList.RES_SUCCESS);
//                responseDTO.setMessage("Success");
//                responseDTO.setContent(employeeDTO);
//                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
//            }else {
//                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
//                responseDTO.setMessage("Class Not Found for this id");
//                responseDTO.setContent(null);
//                return new ResponseEntity(responseDTO, HttpStatus.BAD_REQUEST);
//            }
//        }catch (Exception ex){
//            responseDTO.setCode(VarList.RES_ERROR);
//            responseDTO.setMessage(ex.getMessage());
//            responseDTO.setContent(null);
//            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }



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

    // Update an existing student
    @PutMapping(value = "/updateStudent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    // Get all students
    @GetMapping("/getAllStudents")
    public ResponseEntity GetAllStudents() {
        try {
            List<StudentDTO> studentList = studentService.getAllStudents(); // Fetch all students
            responseDTO.setCode(VarList.RES_SUCCESS);
            responseDTO.setMessage("Students fetched successfully");
            responseDTO.setContent(studentList);
            return new ResponseEntity(responseDTO, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error fetching all students: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all students count
    @GetMapping("/getStudentCount")
    public ResponseEntity GetStudentCount() {
        try {
            Long studentCount = studentService.getStudentCount(); // Fetch all students count
            responseDTO.setCode(VarList.RES_SUCCESS);
            responseDTO.setMessage("Fetched student count successfully");
            responseDTO.setContent(studentCount);
            return new ResponseEntity(responseDTO, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error fetching student count: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a student by ID
    @DeleteMapping("/deleteStudent/{id}")
    public ResponseEntity deleteStudent(@PathVariable int id) {
        try {
            String res = studentService.deleteStudent(id); // Delete student using studentService
            if (res.equals("00")) {
                responseDTO.setCode(VarList.RES_SUCCESS);
                responseDTO.setMessage("Success");
                responseDTO.setContent(null);
                return new ResponseEntity(responseDTO, HttpStatus.ACCEPTED);
            } else {
                responseDTO.setCode(VarList.RES_NO_DATE_FOUND);
                responseDTO.setMessage("No Student Available for this ID");
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

    // Search for a specific student by ID
    @GetMapping("/searchStudent/{id}")
    public ResponseEntity SearchStudent(@PathVariable int id) {
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


    // Get student by registration number
//    @GetMapping("/getStudent/registration/{registrationNumber}")
//    public ResponseEntity<?> getStudentByRegistrationNumber(@PathVariable String registrationNumber) {
//        try {
//            StudentDTO studentDTO = studentService.getStudentByRegistrationNumber(registrationNumber);
//
//            if (studentDTO != null) {
//                responseDTO.setCode(VarList.RES_SUCCESS);
//                responseDTO.setMessage("Student found successfully");
//                responseDTO.setContent(studentDTO);
//                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            } else {
//                responseDTO.setCode(VarList.RES_ERROR);
//                responseDTO.setMessage("Student not found");
//                responseDTO.setContent(null);
//                return new ResponseEnt<>(responseDTO, HttpStatus.NOT_FOUND);
//            }
//
//        } catch (Exception ex) {
//            log.error("Error fetching student by registration number: ", ex);
//            responseDTO.setCode(VarList.RES_ERROR);
//            responseDTO.setMessage("An error occurred while fetching student");
//            responseDTO.setContent(null);
//            return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }



    // ===== TEACHER MANAGEMENT =====

    // ===== TEACHER MANAGEMENT =====

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
            return buildTeacherSaveResponse(result, teacherDTO);

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

    private ResponseEntity<ResponseDTO> buildTeacherSaveResponse(String result, TeacherDTO teacherDTO) {
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
            return buildTeacherUpdateResponse(result, teacherDTO);

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

    private ResponseEntity<ResponseDTO> buildTeacherUpdateResponse(String result, TeacherDTO teacherDTO) {
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

    // Get teachers count
    @GetMapping("/getTeacherCount")
    public ResponseEntity GetTeacherCount() {
        try {
            Long studentCount = teacherService.getStudentCount(); // Fetch all teacher count
            responseDTO.setCode(VarList.RES_SUCCESS);
            responseDTO.setMessage("Fetched teacher count successfully");
            responseDTO.setContent(studentCount);
            return new ResponseEntity(responseDTO, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error fetching teacher count: ", ex);
            responseDTO.setCode(VarList.RES_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);
            return new ResponseEntity(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // ===== EXAM MANAGEMENT =====
    /**
     * Get all upcoming exams
     * @return ResponseEntity containing list of upcoming exams
     */
    @GetMapping("/exams/getUpcomingExams")
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