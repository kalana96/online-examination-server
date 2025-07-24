package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClassDTO {
    private int id;
    private String className;
    private String description;

    // Class schedule fields
    private LocalDate classDate;
    private LocalTime startTime;
    private Integer gradeId;
    private Integer subjectId;
    private Long studentCount;

    private String gradeName; // For display purposes
    private String subjectName; // For display purposes

    private GradeDTO grade; // Nested subject information
    private SubjectDTO subject; // Nested subject information
    private List<Integer> teacherIds; // List of teacher IDs to assign to this class

    // NEW: Teacher information fields
    private List<String> teacherNames; // List of teacher names
    private List<TeacherDTO> teachers; // Full teacher information (optional)
    private String teacherNamesString; // Comma-separated teacher names for display


}
