package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.ClassDTO;
import com.examination.online_examination_server.dto.StudentDTO;
import com.examination.online_examination_server.dto.SubjectDTO;
import com.examination.online_examination_server.dto.UserRegistrationDTO;
import com.examination.online_examination_server.entity.*;
import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.enums.UserRole;
import com.examination.online_examination_server.repository.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;
    // Save a new student

    // Methode to Validate Student input
    private String validateStudentInput(StudentDTO studentDTO) {
        if (studentDTO == null || studentDTO.getRegistrationNumber() == null) {
            log.warn("Invalid student data provided");
            return VarList.RES_ERROR;
        }

        if (studentDTO.getClassIds() == null || studentDTO.getClassIds().isEmpty()) {
            log.warn("Student must have at least one Class");
            return VarList.RES_CLASS_NOT_FOUND;
        }

        // Validate gradeId
        if (studentDTO.getGradeId() <= 0) {
            log.warn("Valid gradeId is required");
            return VarList.RES_INVALID_INPUT;
        }
        return VarList.RES_SUCCESS;
    }

    // Methode to Check for duplicates of save student data
    private String checkForDuplicates(StudentDTO studentDTO) {
        if (studentRepository.existsByRegistrationNumber(studentDTO.getRegistrationNumber())) {
            log.warn("Student with registration number {} already exists",
                    studentDTO.getRegistrationNumber());
            return VarList.RES_DUPLICATE;
        }
        if (studentDTO.getNic() != null && studentRepository.existsByNic(studentDTO.getNic())) {
            log.warn("Student with NIC {} already exists", studentDTO.getNic());
            return VarList.RES_DUPLICATE_NIC;
        }
        if (studentDTO.getEmail() != null && studentRepository.existsByEmail(studentDTO.getEmail())) {
            log.warn("Student with email {} already exists", studentDTO.getEmail());
            return VarList.RES_DUPLICATE_EMAIL;
        }
        //check if password already exists in the system
        if (isPasswordAlreadyExists(studentDTO.getUserDetails().getPassword())) {
            log.warn("Password already exists in the system");
            return VarList.RES_PASSWORD_ALREADY_EXISTS;
        }
        return VarList.RES_SUCCESS;
    }

    // Methode to Check for duplicates (excluding current student)
    private String checkForUpdateDuplicates(StudentDTO studentDTO, Student existingStudent) {
        // Check registration number
        if (studentDTO.getRegistrationNumber() != null &&
                !studentDTO.getRegistrationNumber().equals(existingStudent.getRegistrationNumber())) {
            if (studentRepository.existsByRegistrationNumberAndIdNot(
                    studentDTO.getRegistrationNumber(), studentDTO.getId())) {
                log.warn("Registration number {} already exists for another student",
                        studentDTO.getRegistrationNumber());
                return VarList.RES_DUPLICATE;
            }
        }
        // Check NIC
        if (studentDTO.getNic() != null &&
                !studentDTO.getNic().equals(existingStudent.getNic())) {
            if (studentRepository.existsByNicAndIdNot(studentDTO.getNic(), studentDTO.getId())) {
                log.warn("NIC {} already exists for another student", studentDTO.getNic());
                return VarList.RES_DUPLICATE_NIC;
            }
        }
        // Check email
        if (studentDTO.getEmail() != null &&
                !studentDTO.getEmail().equals(existingStudent.getEmail())) {
            if (studentRepository.existsByEmailAndIdNot(studentDTO.getEmail(), studentDTO.getId())) {
                log.warn("Email {} already exists for another student", studentDTO.getEmail());
                return VarList.RES_DUPLICATE_EMAIL;
            }
        }
        return VarList.RES_SUCCESS;
    }

    // Method to check if password already exists in the system
    private boolean isPasswordAlreadyExists(String plainPassword) {
        try {
            if (plainPassword == null || plainPassword.trim().isEmpty()) {
                log.warn("Cannot check password existence: password is null or empty");
                return false; // Consider null/empty passwords as not existing
            }
            // Get all users from the database
            List<User> allUsers = userRepository.findAll();

            // Check if the plain password matches any existing encoded password
            for (User user : allUsers) {
                if (passwordEncoder.matches(plainPassword, user.getPassword())) {
                    return true; // Password already exists
                }
            }
            return false; // Password is unique

        } catch (IllegalArgumentException ex) {
            log.error("Invalid password format for existence check: ", ex);
            return false;
        } catch (Exception ex) {
            log.error("Error checking password existence: ", ex);
            // In case of error, allow the password (fail open for availability)
            return false;
        }
    }


    //Methode to Validate and add classes
    private List<Class> validateClasses(List<Integer> classIds) {
        List<Class> classes = new ArrayList<>();

        for (Integer classId : classIds) {
            Optional<Class> classOpt = classRepository.findActiveById(classId);
            if (classOpt.isPresent()) {
                classes.add(classOpt.get());
            } else {
                log.warn("Subject with ID {} not found or is deleted", classId);
                return null;
            }
        }
        return classes;
    }

    // Method to validate grade exists
    private boolean validateGrade(int gradeId) {
        Optional<Grade> gradeOpt = gradeRepository.findById(gradeId);
        if (!gradeOpt.isPresent()) {
            log.warn("Grade with ID {} not found", gradeId);
            return false;
        }
        return true;
    }


    //method to register new Student
    public String saveStudent(StudentDTO studentDTO) {
        try {
            // Validate input
            String validationResult = validateStudentInput(studentDTO);
            if (!VarList.RES_SUCCESS.equals(validationResult)) {
                return validationResult;
            }
            // Validate grade exists
            if (!validateGrade(studentDTO.getGradeId())) {
                return VarList.RES_GRADE_NOT_FOUND;
            }
            // Check for duplicates
            String duplicateCheck = checkForDuplicates(studentDTO);
            if (!VarList.RES_SUCCESS.equals(duplicateCheck)) {
                return duplicateCheck;
            }
            // Validate and add subjects
            List<Class> classes = validateClasses(studentDTO.getClassIds());
            if (classes == null) {
                return VarList.RES_INVALID_INPUT;
            }
            // Create and save student
            Student student = createStudentEntity(studentDTO, classes);
            studentRepository.save(student);
            log.info("Student {} saved successfully with ID {}", student.getRegistrationNumber(), student.getId());
            return VarList.RES_SUCCESS;
        } catch (DataAccessException ex) {
            log.error("Database error while saving student: ", ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while saving student: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Helper method to Create Student entity
    private Student createStudentEntity(StudentDTO studentDTO, List<Class> classes) {
        Student student = modelMapper.map(studentDTO, Student.class);
        student.setClasses(classes);
        if (studentDTO.getProfilePhoto() != null) {
            student.setProfilePhoto(studentDTO.getProfilePhoto());
        }
        //create User entity
        User user = createUser(studentDTO.getUserDetails(), UserRole.STUDENT);
        user.setStudent(student);
        student.setUser(user);

        return student;
    }

    // Helper method to create User entity
    private User createUser(UserRegistrationDTO userDetails, UserRole role) {
        User user = new User();
        user.setUsername(userDetails.getUsername());
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        user.setRole(role);
        user.setActive(true);
        return user;
    }


    // Update an existing student
    public String updateStudent(StudentDTO studentDTO) {
        try {
            // Validate input
            String validationResult = validateStudentInput(studentDTO);
            if (!VarList.RES_SUCCESS.equals(validationResult)) {
                return validationResult;
            }
            // Validate input
            if (studentDTO == null || studentDTO.getId() == 0) {
                log.warn("Invalid student data for update");
                return VarList.RES_ERROR;
            }
            // Find existing student
            Optional<Student> existingStudentOpt = studentRepository.findActiveById(studentDTO.getId());
            if (!existingStudentOpt.isPresent()) {
                log.warn("Student with ID {} not found or is deleted", studentDTO.getId());
                return VarList.RES_NO_DATE_FOUND;
            }
            Student existingStudent = existingStudentOpt.get();

            // Check for duplicates (excluding current student)
            String duplicateCheck = checkForUpdateDuplicates(studentDTO, existingStudent);
            if (!VarList.RES_SUCCESS.equals(duplicateCheck)) {
                return duplicateCheck;
            }

            // Validate and get classes
            List<Class> classes = validateClassForUpdate(studentDTO.getClassIds());
            if (classes == null) {
                return VarList.RES_INVALID_INPUT;
            }
            // Update student entity
            updateStudentEntity(existingStudent, studentDTO, classes);

            // Update user information if provided
            String userUpdateResult = updateUserInformation(existingStudent, studentDTO);
            if (!VarList.RES_SUCCESS.equals(userUpdateResult)) {
                return userUpdateResult;
            }
            // Save updated student
            studentRepository.save(existingStudent);
            log.info("Student with ID {} updated successfully", studentDTO.getId());
            return VarList.RES_SUCCESS;

        } catch (DataAccessException ex) {
            log.error("Database error while updating student: ", ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while updating student: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Update an existing student
    public String updateStudentProfile(StudentDTO studentDTO) {
        try {

            // Validate input
            if (studentDTO == null || studentDTO.getId() == 0) {
                log.warn("Invalid student data for update");
                return VarList.RES_ERROR;
            }
            // Find existing student
            Optional<Student> existingStudentOpt = studentRepository.findActiveById(studentDTO.getId());
            if (!existingStudentOpt.isPresent()) {
                log.warn("Student with ID {} not found or is deleted", studentDTO.getId());
                return VarList.RES_NO_DATE_FOUND;
            }
            Student existingStudent = existingStudentOpt.get();

            if (studentDTO.getRegistrationNumber() != null &&
                    !studentDTO.getRegistrationNumber().equals(existingStudent.getRegistrationNumber())) {
                if (studentRepository.existsByRegistrationNumberAndIdNot(
                        studentDTO.getRegistrationNumber(), studentDTO.getId())) {
                    log.warn("Registration number {} already exists for another student",
                            studentDTO.getRegistrationNumber());
                    return VarList.RES_DUPLICATE;
                }
            }

            // Check email
            if (studentDTO.getEmail() != null &&
                    !studentDTO.getEmail().equals(existingStudent.getEmail())) {
                if (studentRepository.existsByEmailAndIdNot(studentDTO.getEmail(), studentDTO.getId())) {
                    log.warn("Email {} already exists for another student", studentDTO.getEmail());
                    return VarList.RES_DUPLICATE_EMAIL;
                }
            }

            existingStudent.setRegistrationNumber(studentDTO.getRegistrationNumber());
            existingStudent.setFirstName(studentDTO.getFirstName());
//            existingStudent.setMiddleName(studentDTO.getMiddleName());
            existingStudent.setLastName(studentDTO.getLastName());
//            existingStudent.setNic(studentDTO.getNic());
            existingStudent.setEmail(studentDTO.getEmail());
            existingStudent.setContactNo(studentDTO.getContactNo());
            existingStudent.setAddress(studentDTO.getAddress());
//            existingStudent.setAge(studentDTO.getAge());
            existingStudent.setDob(studentDTO.getDob());
//            existingStudent.setGender(studentDTO.getGender());
//            existingStudent.setGradeId(studentDTO.getGradeId());

            // Handle profile photo update
            if (studentDTO.getProfilePhoto() != null) {
                existingStudent.setProfilePhoto(studentDTO.getProfilePhoto());
            }

            // Update user information if provided
            String userUpdateResult = updateUserInformation(existingStudent, studentDTO);
            if (!VarList.RES_SUCCESS.equals(userUpdateResult)) {
                return userUpdateResult;
            }
            // Save updated student
            studentRepository.save(existingStudent);
            log.info("Student with ID {} updated successfully", studentDTO.getId());
            return VarList.RES_SUCCESS;

        } catch (DataAccessException ex) {
            log.error("Database error while updating student: ", ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while updating student: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Methode to Update student entity
    private void updateStudentEntity(Student existingStudent, StudentDTO studentDTO, List<Class> classes) {
        existingStudent.setRegistrationNumber(studentDTO.getRegistrationNumber());
        existingStudent.setFirstName(studentDTO.getFirstName());
        existingStudent.setMiddleName(studentDTO.getMiddleName());
        existingStudent.setLastName(studentDTO.getLastName());
        existingStudent.setNic(studentDTO.getNic());
        existingStudent.setEmail(studentDTO.getEmail());
        existingStudent.setContactNo(studentDTO.getContactNo());
        existingStudent.setAddress(studentDTO.getAddress());
        existingStudent.setAge(studentDTO.getAge());
        existingStudent.setDob(studentDTO.getDob());
        existingStudent.setGender(studentDTO.getGender());
        existingStudent.setGradeId(studentDTO.getGradeId());
        existingStudent.setClasses(classes);

        // Handle profile photo update
        if (studentDTO.getProfilePhoto() != null) {
            existingStudent.setProfilePhoto(studentDTO.getProfilePhoto());
        }
    }

    // Methode to Validate and get classes
    private List<Class> validateClassForUpdate(List<Integer> classIds) {
        List<Class> classes = new ArrayList<>();

        for (Integer classId : classIds) {
            Optional<Class> classOpt = classRepository.findById(classId);
            if (classOpt.isPresent()) {
                classes.add(classOpt.get());
            } else {
                log.warn("class with ID {} not found", classId);
                return null;
            }
        }
        return classes;
    }

    // Methode Update user information if provided
    private String updateUserInformation(Student existingStudent, StudentDTO studentDTO) {
        if (studentDTO.getUserDetails() == null || existingStudent.getUser() == null) {
            return VarList.RES_SUCCESS;
        }
        User existingUser = existingStudent.getUser();
        UserRegistrationDTO userDetails = studentDTO.getUserDetails();

        // Check if username is being changed
        if (!userDetails.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.existsByUsername(userDetails.getUsername())) {
                log.warn("Username {} already exists", userDetails.getUsername());
                return VarList.RES_DUPLICATE_USERNAME;
            }
            existingUser.setUsername(userDetails.getUsername());
        }
        // Update password if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        return VarList.RES_SUCCESS;
    }


    // Get student list with proper handling of deleted classes/subjects
    public List<StudentDTO> getAllStudents() {
        try {
            // Only fetch active students
            List<Student> students = studentRepository.findAll();
            List<StudentDTO> studentDTOs = new ArrayList<>();

            for (Student student : students) {
                StudentDTO studentDTO = mapStudentToDTO(student);
                studentDTOs.add(studentDTO);
            }
            return studentDTOs;
        } catch (Exception ex) {
            log.error("Error fetching all students: ", ex);
            return new ArrayList<>();
        }
    }

    /**
     * Get total count of active students
     *
     * @return total number of active students
     */
    public long getStudentCount() {
        try {
            return studentRepository.countByIsDeletedFalse();
        } catch (Exception ex) {
            log.error("Error getting student count: ", ex);
            return 0;
        }
    }

    /**
     * Get students by class ID for email notifications
     * Returns actual Student entities (not DTOs) for email service
     *
     * @param classId - the class ID to find students for
     * @return List of Student entities
     */
    public List<Student> getStudentsByClassId(Integer classId) {
        try {
            log.info("Fetching students for class ID: {}", classId);

            // Use your existing repository method to get students by class
            List<Student> students = studentRepository.findByClassIdAndIsDeletedFalse(classId);

            log.info("Found {} students for class ID: {}", students.size(), classId);
            return students;

        } catch (Exception ex) {
            log.error("Error fetching students by class ID {} for email notification: ", classId, ex);
            return new ArrayList<>();
        }
    }

    /**
     * Check if any active students exist for a given class
     * @param classId - the class ID to check
     * @return true if students exist, false otherwise
     */
    public boolean hasStudentsInClass(Integer classId) {
        try {
            return studentRepository.existsByClassIdActive(classId);
        } catch (Exception ex) {
            log.error("Error checking if students exist for class ID {}: ", classId, ex);
            return false;
        }
    }

    // Helper method to map Student entity to DTO
    private StudentDTO mapStudentToDTO(Student student) {
        StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class);

        // Handle subjects - filter out deleted subjects
        if (student.getClasses() != null && !student.getClasses().isEmpty()) {
            List<Class> activeSubjects = student.getClasses().stream()
                    .filter(subject -> !subject.isDeleted())
                    .collect(Collectors.toList());

            if (!activeSubjects.isEmpty()) {
                List<Integer> classIds = activeSubjects.stream()
                        .map(Class::getId)
                        .collect(Collectors.toList());
                studentDTO.setClassIds(classIds);

                List<ClassDTO> subjectDTOs = activeSubjects.stream()
                        .map(subject -> modelMapper.map(subject, ClassDTO.class))
                        .collect(Collectors.toList());
                studentDTO.setClasses(subjectDTOs);
            } else {
                studentDTO.setClassIds(new ArrayList<>());
                studentDTO.setClasses(new ArrayList<>());
                log.warn("Student {} has no active subjects", student.getId());
            }
        }

        // Handle profile photo
        if (student.getProfilePhoto() != null) {
            studentDTO.setProfilePhotoBase64(Base64.getEncoder().encodeToString(student.getProfilePhoto()));
        }

        // Handle user details
        if (student.getUser() != null) {
            UserRegistrationDTO userDTO = new UserRegistrationDTO();
            userDTO.setUsername(student.getUser().getUsername());
//            userDTO.setPassword(student.getUser().getPassword());
            userDTO.setRole(student.getUser().getRole());
            studentDTO.setUserDetails(userDTO);
        }
        return studentDTO;
    }


    // Get an student by ID
    public StudentDTO getStudentById(int studentId) {
        try {
            Optional<Student> studentOpt = studentRepository.findStudentWithCompleteDetails(studentId);

            if (studentOpt.isPresent()) {
                return mapStudentToDTO(studentOpt.get());
            }
            log.warn("Student with ID {} not found", studentId);
            return null;
        } catch (Exception ex) {
            log.error("Error fetching student by ID {}: ", studentId, ex);
            return null;
        }
    }


    // Search for a specific student by id
    public StudentDTO SearchStudent(int id) {
        if (studentRepository.existsById(id)) {
            Student student = studentRepository.findById(id).orElse(null); // Fetch subject by ID
            StudentDTO studentDTO = modelMapper.map(student, StudentDTO.class); // Map to DTO

            // Convert image to Base64 string if photo exists
            if (student.getProfilePhoto() != null) {
                studentDTO.setProfilePhotoBase64(Base64.getEncoder().encodeToString(student.getProfilePhoto()));
            }
            return studentDTO;
//            return modelMapper.map(student, StudentDTO.class); // Map to DTO
        } else {
            return null; // Return null if subject doesn't exist
        }
    }


    //  method to get students by class
    public List<StudentDTO> getStudentsByClass(Integer classId) {
        try {
            if (studentRepository.existsByClassIdActive(classId)) {
                List<Student> students = studentRepository.findByClassIdAndIsDeletedFalse(classId);
                return students.stream()
                        .map(this::mapStudentToDTO)
                        .collect(Collectors.toList());

            } else {
                return null; // Return null if subject doesn't exist
            }


//            List<Student> students = studentRepository.findByClassIdAndIsDeletedFalse(classId);
//            return students.stream()
//                    .map(this::mapStudentToDTO)
//                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error fetching students by class ID {}: ", classId, ex);
            return new ArrayList<>();
        }
    }


    // Soft delete a student
    public String deleteStudent(int id) {
        try {
            Optional<Student> studentOptional = studentRepository.findActiveById(id);
            if (studentOptional.isPresent()) {
                Student student = studentOptional.get();
                studentRepository.deleteById(id); // Trigger the soft delete SQL
                return VarList.RES_SUCCESS;
            } else {
                return VarList.RES_NO_DATE_FOUND;
            }
        } catch (Exception ex) {
            log.error("Error deleting student with ID {}: ", id, ex);
            return VarList.RES_ERROR;
        }
    }

    public StudentDTO getStudentByEmail(String email) {
        try {
            Optional<Student> studentOpt = studentRepository.findByEmail(email);

            if (studentOpt.isPresent()) {
                return mapStudentToDTO(studentOpt.get());
            }
            log.warn("Student with email {} not found", email);
            return null;
        } catch (Exception ex) {
            log.error("Error fetching student by email {}: ", email, ex);
            return null;
        }
    }


}
