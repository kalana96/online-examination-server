package com.examination.online_examination_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminDTO {
    private Integer id;
    private String adminCode;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNo;
    private String address;
    private String profilePhotoBase64; // For sending to frontend

    @JsonIgnore
    private byte[] profilePhoto; // For storing raw photo data

    // User details
    private UserRegistrationDTO userDetails;

}
