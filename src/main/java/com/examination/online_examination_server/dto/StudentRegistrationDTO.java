package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentRegistrationDTO {
    private UserRegistrationDTO userDetails;

//    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

//    @NotBlank(message = "First name is required")
    private String firstName;

    private String middleName;

//    @NotBlank(message = "Last name is required")
    private String lastName;

//    @NotBlank(message = "NIC is required")
    private String nic;

    private String contactNo;
    private String address;
    private Integer age;
    private LocalDate dob;
    private String gender;

//    @NotNull(message = "Grade ID is required")
    private int gradeId;

//    @NotNull(message = "Class ID is required")
    private int classId;

//    @NotNull(message = "Subject IDs are required")
    private List<Integer> subjectIds;
}
