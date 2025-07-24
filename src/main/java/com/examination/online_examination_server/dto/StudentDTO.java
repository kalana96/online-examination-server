package com.examination.online_examination_server.dto;

import com.examination.online_examination_server.entity.Subject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    private Integer id; // Unique registration number
    private String registrationNumber;  // Unique registration number
    private String firstName;           // First name of the student
    private String middleName;          // Middle name of the student (optional)
    private String lastName;            // Last name of the student
    private String nic;                 // National Identity Card number
    private String email;               // Email of the student
    private String contactNo;           // Contact number of the student
    private String address;             // Address of the student
    private Integer age;                // Age of the student
    private String profilePhotoBase64;  // Exposed Base64 version
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;              // Date of birth of the student
    private String gender;              // Gender of the student
    private int gradeId;                // ID of the Grade the student belongs to
    private String gradeName;  // Add this field for grade name
    private String className;           // Gender of the student
    private List<Integer> classIds;   // List of subject IDs associated with the student
    //    @JsonIgnore
    private List<ClassDTO> classes;  // List of subject details associated with the student
    @JsonIgnore
    private byte[] profilePhoto;        // URL or path to the student's profile photo
    private UserRegistrationDTO userDetails;
}
