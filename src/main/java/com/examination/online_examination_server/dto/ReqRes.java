package com.examination.online_examination_server.dto;

import com.examination.online_examination_server.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.examination.online_examination_server.entity.OurUsers;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqRes {

    private int statusCode;
    private int id;
    private String error;
    private String message;
    private String token;
    private String refreshToken;
    private String expirationTime;
    private String name;
    private String city;
    private String role;
    private String email;
    private String username;
    private String password;
    private OurUsers ourUsers;
    private User user;
    private String firstName;
    private String middleName;
    private String lastName;
    private String nic;
    private String contactNo;
    private String address;
    private Integer age;
    private LocalDate dob;
    private String gender;
    private Boolean active;
    private Integer gradeId;
    private List<OurUsers> ourUsersList;
    private List<User> usersList;

}
