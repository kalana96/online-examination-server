package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassWithStudentCountDTO {
    private Integer id;
    private String className;
    private String description;
    private LocalDate classDate;
    private LocalTime startTime;
    private String gradeName;
    private String subjectName;
    private Long studentCount;
}
