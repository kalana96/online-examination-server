package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.dto.AdminDTO;
import com.examination.online_examination_server.dto.UserRegistrationDTO;
import com.examination.online_examination_server.entity.*;
import com.examination.online_examination_server.enums.UserRole;
import com.examination.online_examination_server.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class AdminService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AdminRepository adminRepository;


    // Method to validate Admin input
    private String validateAdminInput(AdminDTO adminDTO) {
        if (adminDTO == null) {
            log.warn("Admin data is null");
            return VarList.RES_ERROR;
        }
        if (adminDTO.getAdminCode() == null || adminDTO.getAdminCode().trim().isEmpty()) {
            log.warn("Admin code is required");
            return VarList.RES_ERROR;
        }
        if (adminDTO.getFirstName() == null || adminDTO.getFirstName().trim().isEmpty()) {
            log.warn("First name is required");
            return VarList.RES_ERROR;
        }
        if (adminDTO.getLastName() == null || adminDTO.getLastName().trim().isEmpty()) {
            log.warn("Last name is required");
            return VarList.RES_ERROR;
        }
        if (adminDTO.getEmail() == null || adminDTO.getEmail().trim().isEmpty()) {
            log.warn("Email is required");
            return VarList.RES_ERROR;
        }
        return VarList.RES_SUCCESS;
    }

    // Method to validate User Registration input
    private String validateUserInput(UserRegistrationDTO userDetails) {
        if (userDetails == null) {
            log.warn("User details are null");
            return VarList.RES_ERROR;
        }
        if (userDetails.getUsername() == null || userDetails.getUsername().trim().isEmpty()) {
            log.warn("Username is required");
            return VarList.RES_ERROR;
        }
//        if (userDetails.getPassword() == null || userDetails.getPassword().trim().isEmpty()) {
//            log.warn("Password is required");
//            return VarList.RES_ERROR;
//        }
//        if (userDetails.getPassword().length() < 6) {
//            log.warn("Password must be at least 6 characters long");
//            return VarList.RES_ERROR;
//        }
        return VarList.RES_SUCCESS;
    }

    // Method to check for duplicates when saving new admin
    private String checkForDuplicates(AdminDTO adminDTO, UserRegistrationDTO userDetails) {
        if (adminRepository.existsByAdminCode(adminDTO.getAdminCode())) {
            log.warn("Admin with admin code {} already exists", adminDTO.getAdminCode());
            return VarList.RES_DUPLICATE;
        }
        if (adminRepository.existsByEmail(adminDTO.getEmail())) {
            log.warn("Admin with email {} already exists", adminDTO.getEmail());
            return VarList.RES_DUPLICATE_EMAIL;
        }
        if (userRepository.existsByUsername(userDetails.getUsername())) {
            log.warn("Username {} already exists", userDetails.getUsername());
            return VarList.RES_DUPLICATE_USERNAME;
        }
        // Check if password already exists in the system
        if (isPasswordAlreadyExists(userDetails.getPassword())) {
            log.warn("Password already exists in the system");
            return VarList.RES_PASSWORD_ALREADY_EXISTS;
        }
        return VarList.RES_SUCCESS;
    }

    // Method to check for duplicates when updating admin (excluding current admin)
    private String checkForUpdateDuplicates(AdminDTO adminDTO, UserRegistrationDTO userDetails, Admin existingAdmin) {
        // Check admin code
        if (adminDTO.getAdminCode() != null &&
                !adminDTO.getAdminCode().equals(existingAdmin.getAdminCode())) {
            if (adminRepository.existsByAdminCode(adminDTO.getAdminCode())) {
                log.warn("Admin code {} already exists for another admin", adminDTO.getAdminCode());
                return VarList.RES_DUPLICATE;
            }
        }

        // Check email
        if (adminDTO.getEmail() != null &&
                !adminDTO.getEmail().equals(existingAdmin.getEmail())) {
            if (adminRepository.existsByEmailAndIdNot(adminDTO.getEmail(), adminDTO.getId())) {
                // Assuming adminRepository has a method to check email against all admins except the current one
                log.warn("Email {} already exists for another admin", adminDTO.getEmail());
                return VarList.RES_DUPLICATE_EMAIL;
            }
        }

        // Check username if user details are provided
        if (userDetails != null && userDetails.getUsername() != null &&
                existingAdmin.getUser() != null &&
                !userDetails.getUsername().equals(existingAdmin.getUser().getUsername())) {
            if (userRepository.existsByUsername(userDetails.getUsername())) {
                log.warn("Username {} already exists", userDetails.getUsername());
                return VarList.RES_DUPLICATE_USERNAME;
            }
        }

        return VarList.RES_SUCCESS;
    }

    // Method to check if password already exists in the system
    private boolean isPasswordAlreadyExists(String plainPassword) {
        try {
            if (plainPassword == null || plainPassword.trim().isEmpty()) {
                log.warn("Cannot check password existence: password is null or empty");
                return false;
            }

            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                if (passwordEncoder.matches(plainPassword, user.getPassword())) {
                    return true;
                }
            }
            return false;
        } catch (IllegalArgumentException ex) {
            log.error("Invalid password format for existence check: ", ex);
            return false;
        } catch (Exception ex) {
            log.error("Error checking password existence: ", ex);
            return false;
        }
    }

    // Method to save new Admin
    public String saveAdmin(AdminDTO adminDTO, UserRegistrationDTO userDetails) {
        try {
            // Validate admin input
            String adminValidationResult = validateAdminInput(adminDTO);
            if (!VarList.RES_SUCCESS.equals(adminValidationResult)) {
                return adminValidationResult;
            }

            // Validate user input
            String userValidationResult = validateUserInput(userDetails);
            if (!VarList.RES_SUCCESS.equals(userValidationResult)) {
                return userValidationResult;
            }

            // Check for duplicates
            String duplicateCheck = checkForDuplicates(adminDTO, userDetails);
            if (!VarList.RES_SUCCESS.equals(duplicateCheck)) {
                return duplicateCheck;
            }

            // Create and save admin
            Admin admin = createAdminEntity(adminDTO, userDetails);
            adminRepository.save(admin);

            log.info("Admin {} saved successfully with ID {}", admin.getAdminCode(), admin.getId());
            return VarList.RES_SUCCESS;

        } catch (DataAccessException ex) {
            log.error("Database error while saving admin: ", ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while saving admin: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Helper method to create Admin entity
    private Admin createAdminEntity(AdminDTO adminDTO, UserRegistrationDTO userDetails) {
        Admin admin = modelMapper.map(adminDTO, Admin.class);

        // Handle profile photo
        if (adminDTO.getProfilePhoto() != null) {
            admin.setProfilePhoto(adminDTO.getProfilePhoto());
        }

        // Create User entity
        User user = createUser(userDetails, UserRole.ADMIN);
        user.setAdmin(admin);
        admin.setUser(user);

        return admin;
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

    // Method to update existing Admin
    public String updateAdmin(AdminDTO adminDTO, UserRegistrationDTO userDetails) {
        try {
            // Validate input
            if (adminDTO == null || adminDTO.getId() == null) {
                log.warn("Invalid admin data for update");
                return VarList.RES_ERROR;
            }

            String adminValidationResult = validateAdminInput(adminDTO);
            if (!VarList.RES_SUCCESS.equals(adminValidationResult)) {
                return adminValidationResult;
            }

            // Find existing admin
            Optional<Admin> existingAdminOpt = adminRepository.findActiveById(adminDTO.getId());
            if (!existingAdminOpt.isPresent()) {
                log.warn("Admin with ID {} not found or is deleted", adminDTO.getId());
                return VarList.RES_NO_DATE_FOUND;
            }
            Admin existingAdmin = existingAdminOpt.get();

            // Check for duplicates (excluding current admin)
            String duplicateCheck = checkForUpdateDuplicates(adminDTO, userDetails, existingAdmin);
            if (!VarList.RES_SUCCESS.equals(duplicateCheck)) {
                return duplicateCheck;
            }

            // Update admin entity
            updateAdminEntity(existingAdmin, adminDTO);

            // Update user information if provided
            if (userDetails != null) {
                String userUpdateResult = updateUserInformation(existingAdmin, userDetails);
                if (!VarList.RES_SUCCESS.equals(userUpdateResult)) {
                    return userUpdateResult;
                }
            }

            // Save updated admin
            adminRepository.save(existingAdmin);
            log.info("Admin with ID {} updated successfully", adminDTO.getId());
            return VarList.RES_SUCCESS;

        } catch (DataAccessException ex) {
            log.error("Database error while updating admin: ", ex);
            return VarList.RES_ERROR;
        } catch (Exception ex) {
            log.error("Unexpected error while updating admin: ", ex);
            return VarList.RES_ERROR;
        }
    }

    // Helper method to update Admin entity
    private void updateAdminEntity(Admin existingAdmin, AdminDTO adminDTO) {
        existingAdmin.setAdminCode(adminDTO.getAdminCode());
        existingAdmin.setFirstName(adminDTO.getFirstName());
        existingAdmin.setLastName(adminDTO.getLastName());
        existingAdmin.setEmail(adminDTO.getEmail());
        existingAdmin.setAddress(adminDTO.getAddress());
        existingAdmin.setContactNo(adminDTO.getContactNo());

        // Handle profile photo update
        if (adminDTO.getProfilePhoto() != null) {
            existingAdmin.setProfilePhoto(adminDTO.getProfilePhoto());
        }
    }

    // Method to update user information if provided
    private String updateUserInformation(Admin existingAdmin, UserRegistrationDTO userDetails) {
        if (userDetails == null || existingAdmin.getUser() == null) {
            return VarList.RES_SUCCESS;
        }

        String userValidationResult = validateUserInput(userDetails);
        if (!VarList.RES_SUCCESS.equals(userValidationResult)) {
            return userValidationResult;
        }

        User existingUser = existingAdmin.getUser();

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

    // Method to get all admins
    public List<AdminDTO> getAllAdmins() {
        try {
            List<Admin> admins = adminRepository.findAll();
            List<AdminDTO> adminDTOs = new ArrayList<>();

            for (Admin admin : admins) {
                AdminDTO adminDTO = mapAdminToDTO(admin);
                adminDTOs.add(adminDTO);
            }
            return adminDTOs;
        } catch (Exception ex) {
            log.error("Error fetching all admins: ", ex);
            return new ArrayList<>();
        }
    }

    // Helper method to map Admin entity to DTO
    private AdminDTO mapAdminToDTO(Admin admin) {
        AdminDTO adminDTO = modelMapper.map(admin, AdminDTO.class);

        // Handle profile photo
        if (admin.getProfilePhoto() != null) {
            adminDTO.setProfilePhotoBase64(Base64.getEncoder().encodeToString(admin.getProfilePhoto()));
        }

        // Handle user details if needed (you might want to add userDetails to AdminDTO)
        // if (admin.getUser() != null) {
        //     UserRegistrationDTO userDTO = new UserRegistrationDTO();
        //     userDTO.setUsername(admin.getUser().getUsername());
        //     userDTO.setRole(admin.getUser().getRole());
        //     adminDTO.setUserDetails(userDTO);
        // }

        return adminDTO;
    }

    // Method to get admin by ID
    public AdminDTO getAdminById(int adminId) {
        try {
            Optional<Admin> adminOpt = adminRepository.findActiveById(adminId);
            if (adminOpt.isPresent()) {
                return mapAdminToDTO(adminOpt.get());
            }
            log.warn("Admin with ID {} not found", adminId);
            return null;
        } catch (Exception ex) {
            log.error("Error fetching admin by ID {}: ", adminId, ex);
            return null;
        }
    }

    // Method to get admin by ID with user data
    public AdminDTO getAdminWithUserById(int adminId) {
        try {
            Optional<Admin> adminOpt = adminRepository.findActiveById(adminId);
            if (adminOpt.isPresent()) {
                return mapAdminWithUserToDTO(adminOpt.get());
            }
            log.warn("Admin with ID {} not found", adminId);
            return null;
        } catch (Exception ex) {
            log.error("Error fetching admin with user data by ID {}: ", adminId, ex);
            return null;
        }
    }

    // Helper method to map Admin entity to DTO with user data
    private AdminDTO mapAdminWithUserToDTO(Admin admin) {
        AdminDTO adminDTO = modelMapper.map(admin, AdminDTO.class);

        // Handle profile photo
        if (admin.getProfilePhoto() != null) {
            adminDTO.setProfilePhotoBase64(Base64.getEncoder().encodeToString(admin.getProfilePhoto()));
        }
        // Map user data if available
        if (admin.getUser() != null) {
            User user = admin.getUser();
            UserRegistrationDTO userDTO = new UserRegistrationDTO();
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setRole(user.getRole());
            userDTO.setActive(user.isActive());
            userDTO.setCreatedAt(user.getCreatedAt());
            userDTO.setUpdatedAt(user.getUpdatedAt());

            adminDTO.setUserDetails(userDTO);
        }
        return adminDTO;
    }

    // Method to search for a specific admin by id
    public AdminDTO searchAdmin(int id) {
        try {
            Optional<Admin> adminOpt = adminRepository.findActiveById(id);
            if (adminOpt.isPresent()) {
                return mapAdminToDTO(adminOpt.get());
            }
            return null;
        } catch (Exception ex) {
            log.error("Error searching admin by ID {}: ", id, ex);
            return null;
        }
    }

    // Method to soft delete an admin
    public String deleteAdmin(int id) {
        try {
            Optional<Admin> adminOptional = adminRepository.findActiveById(id);
            if (adminOptional.isPresent()) {
                Admin admin = adminOptional.get();
                adminRepository.deleteById(id); // Trigger the soft delete SQL
                log.info("Admin with ID {} deleted successfully", id);
                return VarList.RES_SUCCESS;
            } else {
                log.warn("Admin with ID {} not found or already deleted", id);
                return VarList.RES_NO_DATE_FOUND;
            }
        } catch (Exception ex) {
            log.error("Error deleting admin with ID {}: ", id, ex);
            return VarList.RES_ERROR;
        }
    }

    // Method to get admin by admin code
    public AdminDTO getAdminByAdminCode(String adminCode) {
        try {
            List<Admin> admins = adminRepository.findAll();
            Optional<Admin> adminOpt = admins.stream()
                    .filter(admin -> admin.getAdminCode().equals(adminCode))
                    .findFirst();

            if (adminOpt.isPresent()) {
                return mapAdminToDTO(adminOpt.get());
            }
            log.warn("Admin with admin code {} not found", adminCode);
            return null;
        } catch (Exception ex) {
            log.error("Error fetching admin by admin code {}: ", adminCode, ex);
            return null;
        }
    }

    // Method to get admin by email
    public AdminDTO getAdminByEmail(String email) {
        try {
            List<Admin> admins = adminRepository.findAll();
            Optional<Admin> adminOpt = admins.stream()
                    .filter(admin -> admin.getEmail().equals(email))
                    .findFirst();

            if (adminOpt.isPresent()) {
                return mapAdminToDTO(adminOpt.get());
            }
            log.warn("Admin with email {} not found", email);
            return null;
        } catch (Exception ex) {
            log.error("Error fetching admin by email {}: ", email, ex);
            return null;
        }
    }

    // Method to activate/deactivate admin
    public String toggleAdminStatus(int adminId, boolean isActive) {
        try {
            Optional<Admin> adminOpt = adminRepository.findActiveById(adminId);
            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                if (admin.getUser() != null) {
                    admin.getUser().setActive(isActive);
                    adminRepository.save(admin);
                    log.info("Admin with ID {} status changed to {}", adminId, isActive ? "active" : "inactive");
                    return VarList.RES_SUCCESS;
                } else {
                    log.warn("Admin with ID {} has no associated user", adminId);
                    return VarList.RES_ERROR;
                }
            } else {
                log.warn("Admin with ID {} not found", adminId);
                return VarList.RES_NO_DATE_FOUND;
            }
        } catch (Exception ex) {
            log.error("Error toggling admin status for ID {}: ", adminId, ex);
            return VarList.RES_ERROR;
        }
    }


}
