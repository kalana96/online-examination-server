package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.ClassDTO;
import com.examination.online_examination_server.dto.TeacherDTO;
import com.examination.online_examination_server.dto.UserRegistrationDTO;
import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.entity.Subject;
import com.examination.online_examination_server.entity.Teacher;
import com.examination.online_examination_server.entity.User;
import com.examination.online_examination_server.enums.UserRole;
import com.examination.online_examination_server.repository.ClassRepository;
import com.examination.online_examination_server.repository.SubjectRepository;
import com.examination.online_examination_server.repository.TeacherRepository;
import com.examination.online_examination_server.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class TeacherService {
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;

    // Method to Validate Teacher input
    private String validateTeacherInput(TeacherDTO teacherDTO) {
        if (teacherDTO == null || teacherDTO.getTeacherCode() == null) {
            log.warn("Invalid teacher data provided");
            return VarList.RES_ERROR;
        }

        if (teacherDTO.getClassIds() == null || teacherDTO.getClassIds().isEmpty()) {
            log.warn("Teacher must have at least one Class");
            return VarList.RES_CLASS_NOT_FOUND;
        }

        // Validate subjectId
        if (teacherDTO.getSubjectId() == null || teacherDTO.getSubjectId() <= 0) {
            log.warn("Valid subjectId is required");
            return VarList.RES_INVALID_INPUT;
        }
        return VarList.RES_SUCCESS;
    }

    // Method to Check for duplicates of save teacher data
    private String checkForDuplicates(TeacherDTO teacherDTO) {
        if (teacherRepository.existsByTeacherCode(teacherDTO.getTeacherCode())) {
            log.warn("Teacher with teacher code {} already exists",
                    teacherDTO.getTeacherCode());
            return VarList.RES_DUPLICATE;
        }
        if (teacherDTO.getNic() != null && teacherRepository.existsByNic(teacherDTO.getNic())) {
            log.warn("Teacher with NIC {} already exists", teacherDTO.getNic());
            return VarList.RES_DUPLICATE_NIC;
        }
        if (teacherDTO.getEmail() != null && teacherRepository.existsByEmail(teacherDTO.getEmail())) {
            log.warn("Teacher with email {} already exists", teacherDTO.getEmail());
            return VarList.RES_DUPLICATE_EMAIL;
        }
        //check if password already exists in the system
        if (teacherDTO.getUserDetails() != null &&
                isPasswordAlreadyExists(teacherDTO.getUserDetails().getPassword())) {
            log.warn("Password already exists in the system");
            return VarList.RES_PASSWORD_ALREADY_EXISTS;
        }
        return VarList.RES_SUCCESS;
    }

    // Method to Check for duplicates (excluding current teacher)
    private String checkForUpdateDuplicates(TeacherDTO teacherDTO, Teacher existingTeacher) {
        // Check teacher code
        if (teacherDTO.getTeacherCode() != null &&
                !teacherDTO.getTeacherCode().equals(existingTeacher.getTeacherCode())) {
            if (teacherRepository.existsByTeacherCodeAndIdNot(
                    teacherDTO.getTeacherCode(), teacherDTO.getId())) {
                log.warn("Teacher code {} already exists for another teacher",
                        teacherDTO.getTeacherCode());
                return VarList.RES_DUPLICATE;
            }
        }
        // Check NIC
        if (teacherDTO.getNic() != null &&
                !teacherDTO.getNic().equals(existingTeacher.getNic())) {
            if (teacherRepository.existsByNicAndIdNot(teacherDTO.getNic(), teacherDTO.getId())) {
                log.warn("NIC {} already exists for another teacher", teacherDTO.getNic());
                return VarList.RES_DUPLICATE_NIC;
            }
        }
        // Check email
        if (teacherDTO.getEmail() != null &&
                !teacherDTO.getEmail().equals(existingTeacher.getEmail())) {
            if (teacherRepository.existsByEmailAndIdNot(teacherDTO.getEmail(), teacherDTO.getId())) {
                log.warn("Email {} already exists for another teacher", teacherDTO.getEmail());
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

        }  catch (IllegalArgumentException ex) {
            log.error("Invalid password format for existence check: ", ex);
            return false;
        } catch (Exception ex) {
            log.error("Error checking password existence: ", ex);
            // In case of error, allow the password (fail open for availability)
            return false;
        }
    }

    //Method to Validate and add classes
    private List<Class> validateClasses(List<Integer> classIds) {
        List<Class> classes = new ArrayList<>();

        for (Integer classId : classIds) {
            Optional<Class> classOpt = classRepository.findActiveById(classId);
            if (classOpt.isPresent()) {
                classes.add(classOpt.get());
            } else {
                log.warn("Class with ID {} not found or is deleted", classId);
                return null;
            }
        }
        return classes;
    }

    // Method to validate subject exists
    private boolean validateSubject(int subjectId) {
        Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);
        if (!subjectOpt.isPresent()) {
            log.warn("Subject with ID {} not found", subjectId);
            return false;
        }
        return true;
    }

    //method to register new Teacher
    public String saveTeacher(TeacherDTO teacherDTO) {
        try {
            // Validate input
            String validationResult = validateTeacherInput(teacherDTO);
            if (!VarList.RES_SUCCESS.equals(validationResult)) {
                return validationResult;
            }

            // Validate subject exists
            if (!validateSubject(teacherDTO.getSubjectId())) {
                return VarList.RES_SUBJECT_NOT_FOUND;
            }

            // Check for duplicates
            String duplicateCheck = checkForDuplicates(teacherDTO);
            if (!VarList.RES_SUCCESS.equals(duplicateCheck)) {
                return duplicateCheck;
            }

            // Validate and add classes
            List<Class> classes = validateClasses(teacherDTO.getClassIds());
            if (classes == null) {
                return VarList.RES_INVALID_INPUT;
            }

            // Create and save teacher
            Teacher teacher = createTeacherEntity(teacherDTO, classes);
            teacherRepository.save(teacher);

            log.info("Teacher {} saved successfully with ID {}",
                    teacher.getTeacherCode(), teacher.getId());
            return VarList.RES_SUCCESS;

        } catch (DataAccessException ex) {
            log.error("Database error while saving teacher: ", ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while saving teacher: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Helper method to Create Teacher entity
    private Teacher createTeacherEntity(TeacherDTO teacherDTO, List<Class> classes) {
        Teacher teacher = modelMapper.map(teacherDTO, Teacher.class);
        teacher.setClasses(classes);

        // Set subject
        if (teacherDTO.getSubjectId() != null) {
            Optional<Subject> subjectOpt = subjectRepository.findById(teacherDTO.getSubjectId());
            subjectOpt.ifPresent(teacher::setSubject);
        }

        if (teacherDTO.getProfilePhoto() != null) {
            teacher.setProfilePhoto(teacherDTO.getProfilePhoto());
        }

        //create User entity if user details provided
        if (teacherDTO.getUserDetails() != null) {
            User user = createUser(teacherDTO.getUserDetails(), UserRole.TEACHER);
            user.setTeacher(teacher);
            teacher.setUser(user);
        }

        return teacher;
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

    // Update an existing teacher
    public String updateTeacher(TeacherDTO teacherDTO) {
        try {
            // Validate input
            String validationResult = validateTeacherInput(teacherDTO);
            if (!VarList.RES_SUCCESS.equals(validationResult)) {
                return validationResult;
            }
            // Validate input
            if (teacherDTO == null || teacherDTO.getId() == 0) {
                log.warn("Invalid teacher data for update");
                return VarList.RES_ERROR;
            }
            // Find existing teacher
            Optional<Teacher> existingTeacherOpt = teacherRepository.findActiveById(teacherDTO.getId());
            if (!existingTeacherOpt.isPresent()) {
                log.warn("Teacher with ID {} not found or is deleted", teacherDTO.getId());
                return VarList.RES_NO_DATE_FOUND;
            }
            Teacher existingTeacher = existingTeacherOpt.get();

            // Check for duplicates (excluding current teacher)
            String duplicateCheck = checkForUpdateDuplicates(teacherDTO, existingTeacher);
            if (!VarList.RES_SUCCESS.equals(duplicateCheck)) {
                return duplicateCheck;
            }

            // Validate and get classes
            List<Class> classes = validateClassForUpdate(teacherDTO.getClassIds());
            if (classes == null) {
                return VarList.RES_INVALID_INPUT;
            }
            // Update teacher entity
            updateTeacherEntity(existingTeacher, teacherDTO, classes);

            // Update user information if provided
            String userUpdateResult = updateUserInformation(existingTeacher, teacherDTO);
            if (!VarList.RES_SUCCESS.equals(userUpdateResult)) {
                return userUpdateResult;
            }
            // Save updated teacher
            teacherRepository.save(existingTeacher);
            log.info("Teacher with ID {} updated successfully", teacherDTO.getId());
            return VarList.RES_SUCCESS;

        } catch (DataAccessException ex) {
            log.error("Database error while updating teacher: ", ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while updating teacher: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Method to Update teacher entity
    private void updateTeacherEntity(Teacher existingTeacher, TeacherDTO teacherDTO, List<Class> classes) {
        existingTeacher.setTeacherCode(teacherDTO.getTeacherCode());
        existingTeacher.setFirstName(teacherDTO.getFirstName());
        existingTeacher.setLastName(teacherDTO.getLastName());
        existingTeacher.setNic(teacherDTO.getNic());
        existingTeacher.setEmail(teacherDTO.getEmail());
        existingTeacher.setContactNo(teacherDTO.getContactNo());
        existingTeacher.setAddress(teacherDTO.getAddress());
        existingTeacher.setGender(teacherDTO.getGender());
        existingTeacher.setQualification(teacherDTO.getQualification());
        existingTeacher.setClasses(classes);

        // Update subject
        if (teacherDTO.getSubjectId() != null) {
            Optional<Subject> subjectOpt = subjectRepository.findById(teacherDTO.getSubjectId());
            subjectOpt.ifPresent(existingTeacher::setSubject);
        }

        // Handle profile photo update
        if (teacherDTO.getProfilePhoto() != null) {
            existingTeacher.setProfilePhoto(teacherDTO.getProfilePhoto());
        }
    }

    // Method to Validate and get classes
    private List<Class> validateClassForUpdate(List<Integer> classIds) {
        List<Class> classes = new ArrayList<>();

        for (Integer classId : classIds) {
            Optional<Class> classOpt = classRepository.findById(classId);
            if (classOpt.isPresent()) {
                classes.add(classOpt.get());
            } else {
                log.warn("Class with ID {} not found", classId);
                return null;
            }
        }
        return classes;
    }

    // Method Update user information if provided
    private String updateUserInformation(Teacher existingTeacher, TeacherDTO teacherDTO) {
        if (teacherDTO.getUserDetails() == null || existingTeacher.getUser() == null) {
            return VarList.RES_SUCCESS;
        }
        User existingUser = existingTeacher.getUser();
        UserRegistrationDTO userDetails = teacherDTO.getUserDetails();

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

    // Get teacher list with proper handling of deleted classes/subjects
    public List<TeacherDTO> getAllTeachers() {
        try {
            // Only fetch active teachers
            List<Teacher> teachers = teacherRepository.findAll();
            List<TeacherDTO> teacherDTOs = new ArrayList<>();

            for (Teacher teacher : teachers) {
                TeacherDTO teacherDTO = mapTeacherToDTO(teacher);
                teacherDTOs.add(teacherDTO);
            }
            return teacherDTOs;
        } catch (Exception ex) {
            log.error("Error fetching all teachers: ", ex);
            return new ArrayList<>();
        }
    }

    // Helper method to map Teacher entity to DTO
    private TeacherDTO mapTeacherToDTO(Teacher teacher) {
        TeacherDTO teacherDTO = modelMapper.map(teacher, TeacherDTO.class);

        // Handle classes - filter out deleted classes
        if (teacher.getClasses() != null && !teacher.getClasses().isEmpty()) {
            List<Class> activeClasses = teacher.getClasses().stream()
                    .filter(clazz -> !clazz.isDeleted())
                    .collect(Collectors.toList());

            if (!activeClasses.isEmpty()) {
                List<Integer> classIds = activeClasses.stream()
                        .map(Class::getId)
                        .collect(Collectors.toList());
                teacherDTO.setClassIds(classIds);
            } else {
                teacherDTO.setClassIds(new ArrayList<>());
                log.warn("Teacher {} has no active classes", teacher.getId());
            }
        }

        // Handle subject
        if (teacher.getSubject() != null) {
            teacherDTO.setSubjectId(teacher.getSubject().getId());
        }

        // Handle user details
        if (teacher.getUser() != null) {
            UserRegistrationDTO userDTO = new UserRegistrationDTO();
            userDTO.setUsername(teacher.getUser().getUsername());
            userDTO.setRole(teacher.getUser().getRole());
            teacherDTO.setUserDetails(userDTO);
        }
        return teacherDTO;
    }

    // Get a teacher by ID
    public TeacherDTO getTeacherById(int teacherId) {
        try {
            Optional<Teacher> teacherOpt = teacherRepository.findActiveById(teacherId);

            if (teacherOpt.isPresent()) {
                return mapTeacherToDTO(teacherOpt.get());
            }
            log.warn("Teacher with ID {} not found", teacherId);
            return null;
        } catch (Exception ex) {
            log.error("Error fetching teacher by ID {}: ", teacherId, ex);
            return null;
        }
    }

    // Search for a specific teacher by id
    public TeacherDTO searchTeacher(int id) {
        if (teacherRepository.existsById(id)) {
            Teacher teacher = teacherRepository.findById(id).orElse(null);
            return mapTeacherToDTO(teacher);
        } else {
            return null;
        }
    }

    // Method to get teachers by class
    public List<TeacherDTO> getTeachersByClass(Integer classId) {
        try {
            List<Teacher> teachers = teacherRepository.findByClassIdAndIsDeletedFalse(classId);
            return teachers.stream()
                    .map(this::mapTeacherToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error fetching teachers by class ID {}: ", classId, ex);
            return new ArrayList<>();
        }
    }

    // Method to get teachers by subject
    public List<TeacherDTO> getTeachersBySubject(Integer subjectId) {
        try {
            List<Teacher> teachers = teacherRepository.findBySubjectIdAndIsDeletedFalse(subjectId);
            return teachers.stream()
                    .map(this::mapTeacherToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error fetching teachers by subject ID {}: ", subjectId, ex);
            return new ArrayList<>();
        }
    }

    // Soft delete a teacher
    public String deleteTeacher(int id) {
        try {
            Optional<Teacher> teacherOptional = teacherRepository.findActiveById(id);
            if (teacherOptional.isPresent()) {
                Teacher teacher = teacherOptional.get();
                teacherRepository.deleteById(id); // Trigger the soft delete SQL
                return VarList.RES_SUCCESS;
            } else {
                return VarList.RES_NO_DATE_FOUND;
            }
        } catch (Exception ex) {
            log.error("Error deleting teacher with ID {}: ", id, ex);
            return VarList.RES_ERROR;
        }
    }

    // get teacher count
    public long getStudentCount() {
        try {
            return teacherRepository.countByIsDeletedFalse();
        } catch (Exception ex) {
            log.error("Error getting teacher count: ", ex);
            return 0;
        }
    }
}