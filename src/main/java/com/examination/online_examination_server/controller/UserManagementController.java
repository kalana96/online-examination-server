package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.dto.ReqRes;
import com.examination.online_examination_server.service.UsersManagementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/auth")
public class UserManagementController {
    @Autowired
    private UsersManagementService usersManagementService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ReqRes> register( @RequestBody ReqRes registrationRequest) {
        ReqRes response = usersManagementService.register(registrationRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<ReqRes> login(@RequestBody ReqRes loginRequest) {
//        log.warn("Login loginRequest: ");
        ReqRes response = usersManagementService.login(loginRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * User logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ReqRes> logout(HttpServletRequest request) {
        ReqRes response = usersManagementService.logout(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ReqRes> refreshToken(@RequestBody ReqRes refreshRequest) {
        ReqRes response = usersManagementService.refreshToken(refreshRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Get all users - Admin only
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReqRes> getAllUsers() {
        ReqRes response = usersManagementService.getAllUsers();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Get user by ID - Admin only
     */
    @GetMapping("/getUserById/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReqRes> getUserById(@PathVariable Integer userId) {
        ReqRes response = usersManagementService.getUserById(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    

    /**
     * Update user - Admin only
     */
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReqRes> updateUser(@PathVariable Integer userId,
                                             @RequestBody ReqRes updateRequest) {
        ReqRes response = usersManagementService.updateUser(userId, updateRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Delete user - Admin only
     */
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReqRes> deleteUser(@PathVariable Integer userId) {
        ReqRes response = usersManagementService.deleteUser(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Get current user's profile - Authenticated users
     */
//    @GetMapping("/users/profile")
//    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
//    public ResponseEntity<ReqRes> getMyProfile() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//        ReqRes response = usersManagementService.getMyInfo(username);
//        return ResponseEntity.status(response.getStatusCode()).body(response);
//    }

    /**
     * Update current user's profile - Authenticated users
     */
//    @PutMapping("/users/profile")
//    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
//    public ResponseEntity<ReqRes> updateMyProfile(@RequestBody ReqRes updateRequest) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//
//        // Get current user's ID first
//        ReqRes currentUserResponse = usersManagementService.getMyInfo(username);
//        if (currentUserResponse.getStatusCode() != 200) {
//            return ResponseEntity.status(currentUserResponse.getStatusCode()).body(currentUserResponse);
//        }
//
//        Integer userId = currentUserResponse.getUser().getId();
//        ReqRes response = usersManagementService.updateUser(userId, updateRequest);
//        return ResponseEntity.status(response.getStatusCode()).body(response);
//    }

    /**
     * Get all students - Admin and Teacher access
     */
//    @GetMapping("/admin/students")
//    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
//    public ResponseEntity<ReqRes> getAllStudents() {
//        ReqRes response = usersManagementService.getAllUsersByRole("STUDENT");
//        return ResponseEntity.status(response.getStatusCode()).body(response);
//    }

    /**
     * Get all teachers - Admin access
     */
//    @GetMapping("/admin/teachers")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ReqRes> getAllTeachers() {
//        ReqRes response = usersManagementService.getAllUsersByRole("TEACHER");
//        return ResponseEntity.status(response.getStatusCode()).body(response);
//    }

    /**
     * Get student profile by ID - Admin and Teacher access
     */
    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ReqRes> getStudentById(@PathVariable Integer studentId) {
        ReqRes response = usersManagementService.getUserById(studentId);
        if (response.getStatusCode() == 200 &&
                response.getUser() != null &&
                !response.getUser().getRole().name().equals("STUDENT")) {
            response.setStatusCode(404);
            response.setMessage("Student not found");
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Update student - Admin access
     */
    @PutMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReqRes> updateStudent(@PathVariable Integer studentId,
                                                @RequestBody ReqRes updateRequest) {
        // Ensure the role remains STUDENT
        updateRequest.setRole("STUDENT");
        ReqRes response = usersManagementService.updateUser(studentId, updateRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Get teacher profile by ID - Admin access
     */
    @GetMapping("/teachers/{teacherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReqRes> getTeacherById(@PathVariable Integer teacherId) {
        ReqRes response = usersManagementService.getUserById(teacherId);
        if (response.getStatusCode() == 200 &&
                response.getUser() != null &&
                !response.getUser().getRole().name().equals("TEACHER")) {
            response.setStatusCode(404);
            response.setMessage("Teacher not found");
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Update teacher - Admin access
     */
    @PutMapping("/teachers/{teacherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReqRes> updateTeacher(@PathVariable Integer teacherId,
                                                @RequestBody ReqRes updateRequest) {
        // Ensure the role remains TEACHER
        updateRequest.setRole("TEACHER");
        ReqRes response = usersManagementService.updateUser(teacherId, updateRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Activate/Deactivate user - Admin only
     */
//    @PatchMapping("/admin/users/{userId}/status")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ReqRes> toggleUserStatus(@PathVariable Integer userId,
//                                                   @RequestBody ReqRes statusRequest) {
//        ReqRes response = usersManagementService.toggleUserStatus(userId, statusRequest.isActive());
//        return ResponseEntity.status(response.getStatusCode()).body(response);
//    }

    /**
     * Change user password - Admin only
     */
//    @PatchMapping("/admin/users/{userId}/password")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ReqRes> changeUserPassword(@PathVariable Integer userId,
//                                                     @RequestBody ReqRes passwordRequest) {
//        ReqRes response = usersManagementService.changeUserPassword(userId, passwordRequest.getPassword());
//        return ResponseEntity.status(response.getStatusCode()).body(response);
//    }

    /**
     * Change own password - Authenticated users
     */
//    @PatchMapping("/users/change-password")
//    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
//    public ResponseEntity<ReqRes> changeMyPassword(@RequestBody ReqRes passwordRequest) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//
//        // Get current user's ID first
//        ReqRes currentUserResponse = usersManagementService.getMyInfo(username);
//        if (currentUserResponse.getStatusCode() != 200) {
//            return ResponseEntity.status(currentUserResponse.getStatusCode()).body(currentUserResponse);
//        }
//
//        Integer userId = currentUserResponse.getUser().getId();
//        ReqRes response = usersManagementService.changeUserPassword(userId, passwordRequest.getPassword());
//        return ResponseEntity.status(response.getStatusCode()).body(response);
//    }


}
