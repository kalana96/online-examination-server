package com.examination.online_examination_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamStatsDTO {
        private Integer teacherId;
        private Long totalExams;
        private Long upcomingExams;
        private Long pastExams;
        private Long todayExams;
        private Long publishedExams;
        private Long draftExams;
        private Long proctoredExams;
        private Double averageDuration;
        private Map<String, Long> examsByType;
        private Map<String, Long> examsByClass;
}
