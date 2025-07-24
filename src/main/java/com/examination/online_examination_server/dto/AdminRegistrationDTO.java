package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminRegistrationDTO {
    private UserRegistrationDTO userDetails;

//    @NotBlank(message = "Admin code is required")
    private String adminCode;

//    @NotBlank(message = "First name is required")
    private String firstName;

//    @NotBlank(message = "Last name is required")
    private String lastName;
    private String contactNo;
    private String department;
}
