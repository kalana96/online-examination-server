package com.examination.online_examination_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherDTO {
    private int id;
    private String teacherCode;
    private String firstName;
    private String lastName;
    private String email;
    private String nic;
    private String contactNo;
    private String address;
    private String gender;
    private Integer subjectId;
    private String qualification;
    private List<Integer> classIds;
    private UserRegistrationDTO userDetails;
    @JsonIgnore
    private byte[] profilePhoto;
}
