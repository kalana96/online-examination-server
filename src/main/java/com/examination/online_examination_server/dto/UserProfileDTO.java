package com.examination.online_examination_server.dto;

import com.examination.online_examination_server.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserProfileDTO {
    private int id;
    private String username;
    private String email;
    private UserRole role;
    private boolean isActive;
    private String firstName;
    private String lastName;
    private String contactNo;
    private Map<String, Object> additionalInfo;
}
