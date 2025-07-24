package com.examination.online_examination_server.service;

import com.examination.online_examination_server.dto.ReqRes;
import com.examination.online_examination_server.dto.UserRegistrationDTO;
import com.examination.online_examination_server.entity.*;
import com.examination.online_examination_server.enums.UserRole;
import com.examination.online_examination_server.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
public class UsersManagementService {

    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

   // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$"); // Username validation pattern (alphanumeric and underscores)
    private static final Pattern NIC_PATTERN = Pattern.compile("^[0-9]{9}[vVxX]$|^[0-9]{12}$");  // NIC validation pattern (Sri Lankan format)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$"); // Phone number validation pattern


    //Methode to new user Registration
    public ReqRes register(ReqRes registrationRequest){
        ReqRes resp = new ReqRes();

        try {
            // Input validation
            if (validateRegistrationInput(registrationRequest, resp)) {
                return resp;
            }
            // Check if username already exists
            if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
                resp.setStatusCode(400);
                resp.setMessage("User with this username already exists");
                return resp;
            }
            // Create User entity
            User user = new User();
            user.setUsername(registrationRequest.getUsername().trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            user.setRole(UserRole.valueOf(registrationRequest.getRole().toUpperCase()));
            user.setActive(true);

            // Create role-specific entity based on user role
            UserRole role = UserRole.valueOf(registrationRequest.getRole().toUpperCase());

            switch (role) {
                case STUDENT:
                    Student student = createStudentFromRequest(registrationRequest);
                    if (student == null) {
                        resp.setStatusCode(400);
                        resp.setMessage("Failed to create student profile");
                        return resp;
                    }
                    user.setStudent(student);
                    break;
                case TEACHER:
                    Teacher teacher = createTeacherFromRequest(registrationRequest);
                    if (teacher == null) {
                        resp.setStatusCode(400);
                        resp.setMessage("Failed to create teacher profile");
                        return resp;
                    }
                    user.setTeacher(teacher);
                    break;
                case ADMIN:
                    Admin admin = createAdminFromRequest(registrationRequest);
                    if (admin == null) {
                        resp.setStatusCode(400);
                        resp.setMessage("Failed to create admin profile");
                        return resp;
                    }
                    user.setAdmin(admin);
                    break;
                default:
                    resp.setStatusCode(400);
                    resp.setMessage("Invalid role specified");
                    return resp;
            }
            User savedUser = userRepository.save(user);

            if (savedUser.getId() > 0) {
                resp.setUser(savedUser);
                resp.setMessage("User registered successfully");
                resp.setStatusCode(201);
                log.info("User registered successfully with username: {}", savedUser.getUsername());
            }
        }catch (Exception e){
            log.error("Registration failed for username: {}", registrationRequest.getUsername(), e);
            resp.setStatusCode(500);
            resp.setMessage("Registration failed: " + e.getMessage());
        }
        return resp;
    }

    // Complete registration validation method
    private boolean validateRegistrationInput(ReqRes request, ReqRes response) {
        // Validate username
        if (!StringUtils.hasText(request.getUsername())) {
            response.setStatusCode(400);
            response.setMessage("Username is required");
            return true;
        }
        // Validate password
        if (!StringUtils.hasText(request.getPassword())) {
            response.setStatusCode(400);
            response.setMessage("Password is required");
            return true;
        }
        if (request.getPassword().length() < 6) {
            response.setStatusCode(400);
            response.setMessage("Password must be at least 6 characters long");
            return true;
        }
        // Validate role
        if (!StringUtils.hasText(request.getRole())) {
            response.setStatusCode(400);
            response.setMessage("Role is required");
            return true;
        }
        String role = request.getRole().trim().toUpperCase();
        try {
            UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            response.setStatusCode(400);
            response.setMessage("Role must be ADMIN, STUDENT, or TEACHER");
            return true;
        }
        // Role-specific validation
        switch (role) {
            case "STUDENT":
                return validateStudentInput(request, response);
            case "TEACHER":
                return validateTeacherInput(request, response);
            case "ADMIN":
                return validateAdminInput(request, response);
            default:
                response.setStatusCode(400);
                response.setMessage("Invalid role specified");
                return true;
        }
    }

    private boolean validateStudentInput(ReqRes request, ReqRes response) {
        // Validate required fields for students
        if (!StringUtils.hasText(request.getFirstName())) {
            response.setStatusCode(400);
            response.setMessage("First name is required for students");
            return true;
        }
        if (!StringUtils.hasText(request.getLastName())) {
            response.setStatusCode(400);
            response.setMessage("Last name is required for students");
            return true;
        }
        if (!StringUtils.hasText(request.getEmail())) {
            response.setStatusCode(400);
            response.setMessage("Email is required for students");
            return true;
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            response.setStatusCode(400);
            response.setMessage("Please enter a valid email address");
            return true;
        }
        if (!StringUtils.hasText(request.getNic())) {
            response.setStatusCode(400);
            response.setMessage("NIC is required for students");
            return true;
        }
//        if (!NIC_PATTERN.matcher(request.getNic().trim()).matches()) {
//            response.setStatusCode(400);
//            response.setMessage("Please enter a valid NIC number");
//            return true;
//        }
        if (!StringUtils.hasText(request.getContactNo())) {
            response.setStatusCode(400);
            response.setMessage("Contact number is required for students");
            return true;
        }
//        if (!PHONE_PATTERN.matcher(request.getContactNo().trim()).matches()) {
//            response.setStatusCode(400);
//            response.setMessage("Please enter a valid contact number");
//            return true;
//        }
        if (request.getGradeId() == null || request.getGradeId() <= 0) {
            response.setStatusCode(400);
            response.setMessage("Valid grade ID is required for students");
            return true;
        }
        // Check if email already exists for students
        if (studentRepository.existsByEmail(request.getEmail().trim())) {
            response.setStatusCode(400);
            response.setMessage("Student with this email already exists");
            return true;
        }
        // Check if NIC already exists for students
        if (studentRepository.existsByNic(request.getNic().trim())) {
            response.setStatusCode(400);
            response.setMessage("Student with this NIC already exists");
            return true;
        }
        return false;
    }

    private boolean validateTeacherInput(ReqRes request, ReqRes response) {
        // Validate required fields for teachers
        if (!StringUtils.hasText(request.getFirstName())) {
            response.setStatusCode(400);
            response.setMessage("First name is required for teachers");
            return true;
        }
        if (!StringUtils.hasText(request.getLastName())) {
            response.setStatusCode(400);
            response.setMessage("Last name is required for teachers");
            return true;
        }
        if (!StringUtils.hasText(request.getEmail())) {
            response.setStatusCode(400);
            response.setMessage("Email is required for teachers");
            return true;
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            response.setStatusCode(400);
            response.setMessage("Please enter a valid email address");
            return true;
        }
        if (!StringUtils.hasText(request.getNic())) {
            response.setStatusCode(400);
            response.setMessage("NIC is required for teachers");
            return true;
        }
//        if (!NIC_PATTERN.matcher(request.getNic().trim()).matches()) {
//            response.setStatusCode(400);
//            response.setMessage("Please enter a valid NIC number");
//            return true;
//        }
        if (!StringUtils.hasText(request.getContactNo())) {
            response.setStatusCode(400);
            response.setMessage("Contact number is required for teachers");
            return true;
        }
//        if (!PHONE_PATTERN.matcher(request.getContactNo().trim()).matches()) {
//            response.setStatusCode(400);
//            response.setMessage("Please enter a valid contact number");
//            return true;
//        }
        // Check if email already exists for teachers
//        if (teacherRepository.existsByEmail(request.getEmail().trim())) {
//            response.setStatusCode(400);
//            response.setMessage("Teacher with this email already exists");
//            return true;
//        }
        // Check if NIC already exists for teachers
//        if (teacherRepository.existsByNic(request.getNic().trim())) {
//            response.setStatusCode(400);
//            response.setMessage("Teacher with this NIC already exists");
//            return true;
//        }
        return false;
    }

    private boolean validateAdminInput(ReqRes request, ReqRes response) {
        if (!StringUtils.hasText(request.getFirstName())) {
            response.setStatusCode(400);
            response.setMessage("First name is required for admins");
            return true;
        }
        if (!StringUtils.hasText(request.getLastName())) {
            response.setStatusCode(400);
            response.setMessage("Last name is required for admins");
            return true;
        }
        if (!StringUtils.hasText(request.getEmail())) {
            response.setStatusCode(400);
            response.setMessage("Email is required for admins");
            return true;
        }
//        if (!EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
//            response.setStatusCode(400);
//            response.setMessage("Please enter a valid email address");
//            return true;
//        }
        // Check if email already exists for admins
        if (adminRepository.existsByEmail(request.getEmail().trim())) {
            response.setStatusCode(400);
            response.setMessage("Admin with this email already exists");
            return true;
        }
        // Validate contact number format (optional for admins)
//        if (StringUtils.hasText(request.getContactNo())) {
//            if (!PHONE_PATTERN.matcher(request.getContactNo().trim()).matches()) {
//                response.setStatusCode(400);
//                response.setMessage("Please enter a valid contact number");
//                return true;
//            }
//        }
        return false;
    }

    private Student createStudentFromRequest(ReqRes request) {
        try {
            Student student = new Student();
//            student.setRegistrationNumber(generateStudentRegistrationNumber());
//            student.setFirstName(request.getFirstName().trim());
//            student.setMiddleName(StringUtils.hasText(request.getMiddleName()) ? request.getMiddleName().trim() : null);
//            student.setLastName(request.getLastName().trim());
//            student.setNic(request.getNic().trim().toLowerCase());
//            student.setEmail(request.getEmail().trim().toLowerCase());
//            student.setContactNo(request.getContactNo().trim());
//            student.setAddress(StringUtils.hasText(request.getAddress()) ? request.getAddress().trim() : null);
//            student.setAge(request.getAge());
//            student.setDob(request.getDob());
//            student.setGender(StringUtils.hasText(request.getGender()) ? request.getGender().trim() : null);
//            student.setGradeId(request.getGradeId());
            // Note: studentClass and subjects will be set separately if needed
            return student;
        } catch (Exception e) {
            log.error("Error creating student from request", e);
            return null;
        }
    }

    private Teacher createTeacherFromRequest(ReqRes request) {
        try {
            Teacher teacher = new Teacher();
//            teacher.setTeacherCode(generateTeacherCode());
//            teacher.setFirstName(request.getFirstName().trim());
//            teacher.setMiddleName(StringUtils.hasText(request.getMiddleName()) ? request.getMiddleName().trim() : null);
//            teacher.setLastName(request.getLastName().trim());
//            teacher.setNic(request.getNic().trim().toLowerCase());
//            teacher.setEmail(request.getEmail().trim().toLowerCase());
//            teacher.setContactNo(request.getContactNo().trim());
//            teacher.setAddress(StringUtils.hasText(request.getAddress()) ? request.getAddress().trim() : null);
//            teacher.setAge(request.getAge());
//            teacher.setDob(request.getDob());
//            teacher.setGender(StringUtils.hasText(request.getGender()) ? request.getGender().trim() : null);
//            teacher.setQualification(StringUtils.hasText(request.getQualification()) ? request.getQualification().trim() : null);
//            teacher.setSubject(StringUtils.hasText(request.getSubject()) ? request.getSubject().trim() : null);
//            teacher.setExperience(request.getExperience());
            // Note: classes and subjects will be set separately if needed
            return teacher;
        } catch (Exception e) {
            log.error("Error creating teacher from request", e);
            return null;
        }
    }

    private Admin createAdminFromRequest(ReqRes request) {
        try {
            Admin admin = new Admin();
            admin.setAdminCode(generateAdminCode());
            admin.setFirstName(request.getFirstName().trim());
            admin.setLastName(request.getLastName().trim());
            admin.setEmail(request.getEmail().trim().toLowerCase());
            admin.setContactNo(StringUtils.hasText(request.getContactNo()) ? request.getContactNo().trim() : null);
            return admin;
        } catch (Exception e) {
            log.error("Error creating admin from request", e);
            return null;
        }
    }

    // Generate unique student registration number
    private String generateStudentRegistrationNumber() {
        String prefix = "STU";
        String year = String.valueOf(LocalDate.now().getYear());
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8); // Last 5 digits
        String randomSuffix = String.format("%03d", new Random().nextInt(1000));
        return prefix + year + timestamp + randomSuffix;
    }

    // Generate unique teacher code
    private String generateTeacherCode() {
        String prefix = "TCH";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8); // Last 5 digits
        String randomSuffix = String.format("%03d", new Random().nextInt(1000));
        return prefix + timestamp + randomSuffix;
    }

    // Generate unique admin code
    private String generateAdminCode() {
        String prefix = "ADM";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8); // Last 5 digits
        String randomSuffix = String.format("%03d", new Random().nextInt(1000));
        return prefix + timestamp + randomSuffix;
    }


    // Methode to update user
    public ReqRes updateUser(Integer userId, ReqRes updatedUserRequest) {
        ReqRes reqRes = new ReqRes();
        try {
            if (userId == null || userId <= 0) {
                reqRes.setStatusCode(400);
                reqRes.setMessage("Invalid user ID");
                return reqRes;
            }
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
                return reqRes;
            }
            User existingUser = userOptional.get();

            // Update basic user fields
            if (updateBasicUserFields(existingUser, updatedUserRequest, reqRes)) {
                return reqRes; // Return if validation failed
            }
            // Update role-specific information
            if (updateRoleSpecificInfo(existingUser, updatedUserRequest, reqRes)) {
                return reqRes; // Return if validation failed
            }
            User savedUser = userRepository.save(existingUser);
            reqRes.setUser(savedUser);
            reqRes.setStatusCode(200);
            reqRes.setMessage("User updated successfully");
            log.info("User updated successfully with ID: {}", userId);

        } catch (Exception e) {
            log.error("Error occurred while updating user with ID: {}", userId, e);
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while updating user: " + e.getMessage());
        }
        return reqRes;
    }

    private boolean updateBasicUserFields(User existingUser, ReqRes updateRequest, ReqRes response) {
        // Update username if provided
        if (StringUtils.hasText(updateRequest.getUsername())) {
            String username = updateRequest.getUsername().trim().toLowerCase();

            // Validate email format for username
//            if (!EMAIL_PATTERN.matcher(username).matches()) {
//                response.setStatusCode(400);
//                response.setMessage("Username must be a valid email address");
//                return true;
//            }

            // Check if username is already taken by another user
            Optional<User> existingUsernameUser = userRepository.findByUsername(username);
            if (existingUsernameUser.isPresent() &&
                    existingUsernameUser.get().getId() != existingUser.getId()) {

                response.setStatusCode(400);
                response.setMessage("Username is already taken by another user");
                return true;
            }
            existingUser.setUsername(username);
        }

        // Update password if provided
        if (StringUtils.hasText(updateRequest.getPassword())) {
            if (updateRequest.getPassword().length() < 6) {
                response.setStatusCode(400);
                response.setMessage("Password must be at least 6 characters long");
                return true;
            }
            existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        // Update role if provided (Note: Role changes should be handled carefully)
        if (StringUtils.hasText(updateRequest.getRole())) {
            try {
                UserRole newRole = UserRole.valueOf(updateRequest.getRole().toUpperCase());
                if (newRole != existingUser.getRole()) {
                    response.setStatusCode(400);
                    response.setMessage("Role changes are not allowed through update. Please create a new user.");
                    return true;
                }
            } catch (IllegalArgumentException e) {
                response.setStatusCode(400);
                response.setMessage("Role must be ADMIN, STUDENT, or TEACHER");
                return true;
            }
        }
        // Update active status if provided
        if (updateRequest.getActive() != null) {
            existingUser.setActive(updateRequest.getActive());
        }
        return false; // No validation errors
    }

    private boolean updateRoleSpecificInfo(User user, ReqRes updateRequest, ReqRes response) {
        switch (user.getRole()) {
            case STUDENT:
                return updateStudentInfo(user.getStudent(), updateRequest, response);
            case TEACHER:
                return updateTeacherInfo(user.getTeacher(), updateRequest, response);
            case ADMIN:
                return updateAdminInfo(user.getAdmin(), updateRequest, response);
            default:
                response.setStatusCode(400);
                response.setMessage("Invalid user role");
                return true;
        }
    }

    private boolean updateStudentInfo(Student student, ReqRes updateRequest, ReqRes response) {
        if (student == null) {
            response.setStatusCode(400);
            response.setMessage("Student profile not found");
            return true;
        }
        try {
            // Update first name
            if (StringUtils.hasText(updateRequest.getFirstName())) {
                student.setFirstName(updateRequest.getFirstName().trim());
            }
            // Update middle name
            if (updateRequest.getMiddleName() != null) {
                student.setMiddleName(StringUtils.hasText(updateRequest.getMiddleName()) ?
                        updateRequest.getMiddleName().trim() : null);
            }
            // Update last name
            if (StringUtils.hasText(updateRequest.getLastName())) {
                student.setLastName(updateRequest.getLastName().trim());
            }
            // Update email with validation
            if (StringUtils.hasText(updateRequest.getEmail())) {
                String email = updateRequest.getEmail().trim().toLowerCase();
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    response.setStatusCode(400);
                    response.setMessage("Please enter a valid email address");
                    return true;
                }
                // Check if email is already taken by another student
                if (studentRepository.existsByEmailAndIdNot(email, student.getId())) {
                    response.setStatusCode(400);
                    response.setMessage("Email is already taken by another student");
                    return true;
                }
                student.setEmail(email);
            }
            // Update NIC with validation
            if (StringUtils.hasText(updateRequest.getNic())) {
                String nic = updateRequest.getNic().trim().toLowerCase();
                if (!NIC_PATTERN.matcher(nic).matches()) {
                    response.setStatusCode(400);
                    response.setMessage("Please enter a valid NIC number");
                    return true;
                }
                // Check if NIC is already taken by another student
                if (studentRepository.existsByNicAndIdNot(nic, student.getId())) {
                    response.setStatusCode(400);
                    response.setMessage("NIC is already taken by another student");
                    return true;
                }
                student.setNic(nic);
            }
            // Update contact number with validation
            if (StringUtils.hasText(updateRequest.getContactNo())) {
                String contactNo = updateRequest.getContactNo().trim();
                if (!PHONE_PATTERN.matcher(contactNo).matches()) {
                    response.setStatusCode(400);
                    response.setMessage("Please enter a valid contact number");
                    return true;
                }
                student.setContactNo(contactNo);
            }
            // Update address
            if (updateRequest.getAddress() != null) {
                student.setAddress(StringUtils.hasText(updateRequest.getAddress()) ?
                        updateRequest.getAddress().trim() : null);
            }
            // Update age
            if (updateRequest.getAge() != null) {
                if (updateRequest.getAge() < 5 || updateRequest.getAge() > 100) {
                    response.setStatusCode(400);
                    response.setMessage("Age must be between 5 and 100");
                    return true;
                }
                student.setAge(updateRequest.getAge());
            }
            // Update date of birth
            if (updateRequest.getDob() != null) {
                student.setDob(updateRequest.getDob());
            }
            // Update gender
            if (updateRequest.getGender() != null) {
                student.setGender(StringUtils.hasText(updateRequest.getGender()) ?
                        updateRequest.getGender().trim() : null);
            }
            // Update grade ID
            if (updateRequest.getGradeId() != null) {
                if (updateRequest.getGradeId() <= 0) {
                    response.setStatusCode(400);
                    response.setMessage("Invalid grade ID");
                    return true;
                }
                student.setGradeId(updateRequest.getGradeId());
            }
        } catch (Exception e) {
            log.error("Error updating student info", e);
            response.setStatusCode(500);
            response.setMessage("Error updating student information");
            return true;
        }
        return false; // No validation errors
    }

    private boolean updateTeacherInfo(Teacher teacher, ReqRes updateRequest, ReqRes response) {
        if (teacher == null) {
            response.setStatusCode(400);
            response.setMessage("Teacher profile not found");
            return true;
        }
        try {
            // Update first name
            if (StringUtils.hasText(updateRequest.getFirstName())) {
                teacher.setFirstName(updateRequest.getFirstName().trim());
            }
            // Update last name
            if (StringUtils.hasText(updateRequest.getLastName())) {
                teacher.setLastName(updateRequest.getLastName().trim());
            }
            // Update email with validation
            if (StringUtils.hasText(updateRequest.getEmail())) {
                String email = updateRequest.getEmail().trim().toLowerCase();
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    response.setStatusCode(400);
                    response.setMessage("Please enter a valid email address");
                    return true;
                }
                // Check if email is already taken by another teacher
                if (teacherRepository.existsByEmailAndIdNot(email, teacher.getId())) {
                    response.setStatusCode(400);
                    response.setMessage("Email is already taken by another teacher");
                    return true;
                }
                teacher.setEmail(email);
            }
            // Update NIC with validation
            if (StringUtils.hasText(updateRequest.getNic())) {
                String nic = updateRequest.getNic().trim().toLowerCase();
                if (!NIC_PATTERN.matcher(nic).matches()) {
                    response.setStatusCode(400);
                    response.setMessage("Please enter a valid NIC number");
                    return true;
                }
                // Check if NIC is already taken by another teacher
                if (teacherRepository.existsByNicAndIdNot(nic, teacher.getId())) {
                    response.setStatusCode(400);
                    response.setMessage("NIC is already taken by another teacher");
                    return true;
                }
                teacher.setNic(nic);
            }
            // Update contact number with validation
            if (StringUtils.hasText(updateRequest.getContactNo())) {
                String contactNo = updateRequest.getContactNo().trim();
                if (!PHONE_PATTERN.matcher(contactNo).matches()) {
                    response.setStatusCode(400);
                    response.setMessage("Please enter a valid contact number");
                    return true;
                }
                teacher.setContactNo(contactNo);
            }
            // Update address
            if (updateRequest.getAddress() != null) {
                teacher.setAddress(StringUtils.hasText(updateRequest.getAddress()) ?
                        updateRequest.getAddress().trim() : null);
            }
            // Update gender
            if (updateRequest.getGender() != null) {
                teacher.setGender(StringUtils.hasText(updateRequest.getGender()) ?
                        updateRequest.getGender().trim() : null);
            }
            // Update qualification
//            if (updateRequest.getQualification() != null) {
//                teacher.setQualification(StringUtils.hasText(updateRequest.getQualification()) ?
//                        updateRequest.getQualification().trim() : null);
//            }
            // Update subject
//            if (updateRequest.getSubject() != null) {
//                teacher.setSubject(StringUtils.hasText(updateRequest.getSubject()) ?
//                        updateRequest.getSubject().trim() : null);
//            }
            // Update experience
//            if (updateRequest.getExperience() != null) {
//                if (updateRequest.getExperience() < 0 || updateRequest.getExperience() > 50) {
//                    response.setStatusCode(400);
//                    response.setMessage("Experience must be between 0 and 50 years");
//                    return true;
//                }
//                teacher.setExperience(updateRequest.getExperience());
//            }
        } catch (Exception e) {
            log.error("Error updating teacher info", e);
            response.setStatusCode(500);
            response.setMessage("Error updating teacher information");
            return true;
        }
        return false; // No validation errors
    }

    private boolean updateAdminInfo(Admin admin, ReqRes updateRequest, ReqRes response) {
        if (admin == null) {
            response.setStatusCode(400);
            response.setMessage("Admin profile not found");
            return true;
        }
        try {
            // Update first name
            if (StringUtils.hasText(updateRequest.getFirstName())) {
                admin.setFirstName(updateRequest.getFirstName().trim());
            }
            // Update last name
            if (StringUtils.hasText(updateRequest.getLastName())) {
                admin.setLastName(updateRequest.getLastName().trim());
            }
            // Update email with validation
            if (StringUtils.hasText(updateRequest.getEmail())) {
                String email = updateRequest.getEmail().trim().toLowerCase();
                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    response.setStatusCode(400);
                    response.setMessage("Please enter a valid email address");
                    return true;
                }
                // Check if email is already taken by another admin
                if (adminRepository.existsByEmailAndIdNot(email, admin.getId())) {
                    response.setStatusCode(400);
                    response.setMessage("Email is already taken by another admin");
                    return true;
                }
                admin.setEmail(email);
            }

            // Update contact number with validation
//            if (updateRequest.getContactNo() != null) {
//                if (StringUtils.hasText(updateRequest.getContactNo())) {
//                    String contactNo = updateRequest.getContactNo().trim();
//                    if (!PHONE_PATTERN.matcher(contactNo).matches()) {
//                        response.setStatusCode(400);
//                        response.setMessage("Please enter a valid contact number");
//                        return true;
//                    }
//                    admin.setContactNo(contactNo);
//                } else {
//                    admin.setContactNo(null); // Allow clearing contact number
//                }
//            }
        } catch (Exception e) {
            log.error("Error updating admin info", e);
            response.setStatusCode(500);
            response.setMessage("Error updating admin information");
            return true;
        }
        return false; // No validation errors
    }


    // Bulk update method for updating multiple users
    @Transactional
    public ReqRes bulkUpdateUsers(List<ReqRes> updateRequests) {
        ReqRes response = new ReqRes();
        List<String> successMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        try {
            for (ReqRes updateRequest : updateRequests) {
                if (updateRequest.getId() > 0 ) {
                    ReqRes individualResult = updateUser(updateRequest.getId(), updateRequest);
                    if (individualResult.getStatusCode() == 200) {
                        successMessages.add("User ID " + updateRequest.getId() + " updated successfully");
                    } else {
                        errorMessages.add("User ID " + updateRequest.getId() + ": " + individualResult.getMessage());
                    }
                } else {
                    errorMessages.add("User ID is required for bulk update");
                }
            }
            if (errorMessages.isEmpty()) {
                response.setStatusCode(200);
                response.setMessage("All users updated successfully. Updated: " + successMessages.size());
            } else if (successMessages.isEmpty()) {
                response.setStatusCode(400);
                response.setMessage("All updates failed: " + String.join(", ", errorMessages));
            } else {
                response.setStatusCode(207); // Multi-Status
                response.setMessage("Partial success. Succeeded: " + successMessages.size() +
                        ", Failed: " + errorMessages.size() + ". Errors: " + String.join(", ", errorMessages));
            }
        } catch (Exception e) {
            log.error("Error in bulk update", e);
            response.setStatusCode(500);
            response.setMessage("Bulk update failed: " + e.getMessage());
        }
        return response;
    }


    // Method to update user status (active/inactive)
    public ReqRes updateUserStatus(Integer userId, boolean isActive) {
        ReqRes response = new ReqRes();
        try {
            if (userId == null || userId <= 0) {
                response.setStatusCode(400);
                response.setMessage("Invalid user ID");
                return response;
            }
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("User not found");
                return response;
            }
            User user = userOptional.get();
            user.setActive(isActive);
            userRepository.save(user);

            response.setStatusCode(200);
            response.setMessage("User status updated successfully to " + (isActive ? "active" : "inactive"));
            log.info("User status updated for ID: {} to {}", userId, isActive ? "active" : "inactive");
        } catch (Exception e) {
            log.error("Error updating user status for ID: {}", userId, e);
            response.setStatusCode(500);
            response.setMessage("Error updating user status: " + e.getMessage());
        }
        return response;
    }


    public ReqRes getUserById(Integer userId) {
        ReqRes response = new ReqRes();

        try {
            // Validate user ID
            if (userId == null || userId <= 0) {
                response.setStatusCode(400);
                response.setMessage("Invalid user ID");
                return response;
            }

            // Find user by ID
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("User not found");
                return response;
            }

            User user = userOptional.get();

            // Set user data in response
            response.setUser(user);
            response.setStatusCode(200);
            response.setMessage("User retrieved successfully");

            log.info("User retrieved successfully with ID: {}", userId);

        } catch (Exception e) {
            log.error("Error occurred while retrieving user with ID: {}", userId, e);
            response.setStatusCode(500);
            response.setMessage("Error occurred while retrieving user: " + e.getMessage());
        }

        return response;
    }


    // Methode to user logging
    public ReqRes login(ReqRes loginRequest){
        ReqRes response = new ReqRes();
        try {
            //input validation
            if (validateLoginInput(loginRequest, response)) {
                return response;
            }

            String username = loginRequest.getUsername().trim().toLowerCase();
            String password = loginRequest.getPassword();

            // Check if user exists before attempting authentication
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                log.warn("Invalid email or password");
                response.setStatusCode(401);
                response.setMessage("Invalid email or password");
                return response;
            }
            // Attempt authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            // Get authenticated user
            User user = userOptional.get();

            // Generate tokens
            String jwt = jwtUtils.generateToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

            // Build successful response
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole().name());
            response.setUsername(user.getUsername());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Successfully logged in");

            // Add role-specific information
            switch (user.getRole()) {
                case STUDENT:
                    if (user.getStudent() != null) {
                        response.setId(user.getStudent().getId());
                        response.setName(user.getStudent().getFirstName() + " " + user.getStudent().getLastName());
                    }
                    break;
                case TEACHER:
                    if (user.getTeacher() != null) {
                        response.setId(user.getTeacher().getId());
                        response.setName(user.getTeacher().getFirstName() + " " + user.getTeacher().getLastName());
                    }
                    break;
                case ADMIN:
                    if (user.getAdmin() != null) {
                        response.setId(user.getAdmin().getId());
                        response.setName(user.getAdmin().getFirstName() + " " + user.getAdmin().getLastName());
                    }
                    break;
            }

        } catch (BadCredentialsException e) {
            log.warn("Invalid email or password");
            response.setStatusCode(401);
            response.setMessage("Invalid email or password");
        } catch (LockedException e) {
            log.warn("Account is locked. Please contact administrator");
            response.setStatusCode(423);
            response.setMessage("Account is locked. Please contact administrator");
        } catch (DisabledException e) {
            log.warn("Account is disabled. Please contact administrator");
            response.setStatusCode(403);
            response.setMessage("Account is disabled. Please contact administrator");
        } catch (AuthenticationException e) {
            log.warn("Authentication failed: "  + e.getMessage());
            response.setStatusCode(401);
            response.setMessage("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            log.warn("Login failed: " + e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Login failed: " + e.getMessage());
        }
        return response;
    }

    public ReqRes logout(HttpServletRequest request) {
        ReqRes response = new ReqRes();
        try {
            final String authHeader = request.getHeader("Authorization");

            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                response.setStatusCode(400);
                response.setMessage("Authorization header missing or invalid");
                return response;
            }
            final String token = authHeader.substring(7).trim(); // Remove "Bearer " prefix

            if (!StringUtils.hasText(token)) {
                response.setStatusCode(400);
                response.setMessage("Token is required");
                return response;
            }
            // Add token to blacklist
            tokenBlacklistService.blacklistToken(token);

            // Clear security context
            SecurityContextHolder.clearContext();

            response.setStatusCode(200);
            response.setMessage("Successfully logged out");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Logout failed: " + e.getMessage());
        }
        return response;
    }

    // Methode to user validate Login Inputs
    private boolean validateLoginInput(ReqRes loginRequest, ReqRes response) {
        // Validate username presence
        if (!StringUtils.hasText(loginRequest.getUsername())) {
            log.warn("Username is required");
            response.setStatusCode(400);
            response.setMessage("Username is required");
            return true;
        }
        // Validate email format
        String email = loginRequest.getUsername().trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            log.warn("Please enter a valid email address");
            response.setStatusCode(400);
            response.setMessage("Please enter a valid email address");
            return true;
        }
        // Validate password presence
        if (!StringUtils.hasText(loginRequest.getPassword())) {
            log.warn("Password is required");
            response.setStatusCode(400);
            response.setMessage("Password is required");
            return true;
        }
        // Validate password length
        if (loginRequest.getPassword().length() < 6) {
            log.warn("PPassword must be at least 6 characters long");
            response.setStatusCode(400);
            response.setMessage("Password must be at least 6 characters long");
            return true;
        }
        return false;
    }

    public ReqRes refreshToken(ReqRes refreshTokenRequest){
        ReqRes response = new ReqRes();
        try{
            if (!StringUtils.hasText(refreshTokenRequest.getToken())) {
                response.setStatusCode(400);
                response.setMessage("Refresh token is required");
                return response;
            }

            String refreshToken = refreshTokenRequest.getToken().trim();

            // Extract username from token
            String username = jwtUtils.extractUsername(refreshToken);
            if (!StringUtils.hasText(username)) {
                response.setStatusCode(401);
                response.setMessage("Invalid refresh token");
                return response;
            }

            // Find user
            User user = userRepository.findByUsername(username).orElseThrow(
                    () -> new RuntimeException("User not found")
            );

            // Validate token
            if (jwtUtils.isTokenValid(refreshToken, user)) {
                String jwt = jwtUtils.generateToken(user);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshToken);
                response.setExpirationTime("24Hrs");
                response.setMessage("Token refreshed successfully");
            } else {
                response.setStatusCode(401);
                response.setMessage("Invalid refresh token");
            }

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Token refresh failed: " + e.getMessage());
        }
        return response;
    }


    public ReqRes getAllUsers() {
        ReqRes reqRes = new ReqRes();

        try {
            List<User> result = userRepository.findAll();
            if (!result.isEmpty()) {
                reqRes.setUsersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Users retrieved successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No users found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error retrieving users: " + e.getMessage());
        }
        return reqRes;
    }
    @Transactional
    public ReqRes deleteUser(Integer userId) {
        ReqRes reqRes = new ReqRes();
        try {
            if (userId == null || userId <= 0) {
                reqRes.setStatusCode(400);
                reqRes.setMessage("Invalid user ID");
                return reqRes;
            }
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                // Soft delete is handled by @SQLDelete annotation
                userRepository.deleteById(userId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User deleted successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while deleting user: " + e.getMessage());
        }
        return reqRes;
    }


}
