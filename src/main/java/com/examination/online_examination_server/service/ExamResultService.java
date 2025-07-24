package com.examination.online_examination_server.service;

import com.examination.online_examination_server.dto.ExamResultDTO;
import com.examination.online_examination_server.entity.Exam;
import com.examination.online_examination_server.entity.ExamAttempt;
import com.examination.online_examination_server.entity.ExamResult;
import com.examination.online_examination_server.entity.Student;
import com.examination.online_examination_server.exception.DuplicateResourceException;
import com.examination.online_examination_server.exception.ResourceNotFoundException;
import com.examination.online_examination_server.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExamResultService {
    @Autowired
    private ExamResultRepository examResultRepository;
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ExamAttemptRepository examAttemptRepository;
    @Autowired
    private StudentAnswerRepository studentAnswerRepository;
    @Autowired
    private ModelMapper modelMapper;


        /**
         * Create a new exam result
         */
        public ExamResultDTO createExamResult(ExamResultDTO examResultDTO) {
            log.info("Creating exam result for student ID: {} and exam ID: {}",
                    examResultDTO.getStudentId(), examResultDTO.getExamId());

            // Check if result already exists
            Optional<ExamResult> existingResult = examResultRepository
                    .findByStudentIdAndExamId(examResultDTO.getStudentId(), examResultDTO.getExamId());

            if (existingResult.isPresent()) {
                throw new DuplicateResourceException("Exam result already exists for this student and exam");
            }

            // Validate exam exists
            Exam exam = examRepository.findActiveById(examResultDTO.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examResultDTO.getExamId()));

            // Validate student exists
            Student student = studentRepository.findById(examResultDTO.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + examResultDTO.getStudentId()));

            // Validate exam attempt exists if provided
            ExamAttempt examAttempt = null;
            if (examResultDTO.getExamAttemptId() != null) {
                examAttempt = examAttemptRepository.findById(examResultDTO.getExamAttemptId())
                        .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found with ID: " + examResultDTO.getExamAttemptId()));
            }

            // Create exam result entity
            ExamResult examResult = new ExamResult();
            BeanUtils.copyProperties(examResultDTO, examResult, "id", "examId", "studentId", "examAttemptId");

            examResult.setExam(exam);
            examResult.setStudent(student);
            examResult.setExamAttempt(examAttempt);

            // Calculate rank if not provided
            if (examResult.getRankInClass() == null) {
                examResult.setRankInClass(calculateRankInClass(examResultDTO.getExamId(), examResultDTO.getPercentage()));
            }

            // Save the result
            ExamResult savedResult = examResultRepository.save(examResult);
            log.info("Created exam result with ID: {}", savedResult.getId());

            return convertToDTO(savedResult);
        }

        /**
         * Get exam result by ID
         */
//        @Transactional(readOnly = true)
        public ExamResultDTO getExamResultById(Long id) {
            log.info("Fetching exam result with ID: {}", id);

            ExamResult examResult = examResultRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam result not found with ID: " + id));

            return convertToDTO(examResult);
        }

        /**
         * Get all exam results with pagination
         */
//        @Transactional(readOnly = true)
        public Page<ExamResultDTO> getAllExamResults(int page, int size, String sortBy, String sortDirection) {
            log.info("Fetching exam results - page: {}, size: {}, sortBy: {}, sortDirection: {}",
                    page, size, sortBy, sortDirection);

            Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ExamResult> results = examResultRepository.findAll(pageable);

            return results.map(this::convertToDTO);
        }

        /**
         * Get exam results by student ID
         */
//        @Transactional(readOnly = true)
        public List<ExamResultDTO> getExamResultsByStudentId(Integer studentId) {
            log.info("Fetching exam results for student ID: {}", studentId);

            List<ExamResult> results = examResultRepository.findByStudentId(studentId);
            return results.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        /**
         * Get exam results by exam ID
         */
//        @Transactional(readOnly = true)
        public List<ExamResultDTO> getExamResultsByExamId(Integer examId) {
            log.info("Fetching exam results for exam ID: {}", examId);

            List<ExamResult> results = examResultRepository.findByExamId(examId);
            return results.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        /**
         * Get exam results by exam ID with pagination
         */
//        @Transactional(readOnly = true)
        public Page<ExamResultDTO> getExamResultsByExamId(Integer examId, int page, int size) {
            log.info("Fetching exam results for exam ID: {} with pagination", examId);

            Pageable pageable = PageRequest.of(page, size, Sort.by("percentage").descending());
            Page<ExamResult> results = examResultRepository.findByExamId(examId, pageable);

            return results.map(this::convertToDTO);
        }

        /**
         * Get exam results by class ID
         */
//        @Transactional(readOnly = true)
        public List<ExamResultDTO> getExamResultsByClassId(Integer classId) {
            log.info("Fetching exam results for class ID: {}", classId);

            List<ExamResult> results = examResultRepository.findByClassId(classId);
            return results.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        /**
         * Get exam results by student and exam
         */
//        @Transactional(readOnly = true)
        public Optional<ExamResultDTO> getExamResultByStudentAndExam(Integer studentId, Integer examId) {
            log.info("Fetching exam result for student ID: {} and exam ID: {}", studentId, examId);

            return examResultRepository.findByStudentIdAndExamId(studentId, examId)
                    .map(this::convertToDTO);
        }

        /**
         * Get top performers by exam ID
         */
//        @Transactional(readOnly = true)
        public List<ExamResultDTO> getTopPerformersByExamId(Integer examId, int limit) {
            log.info("Fetching top {} performers for exam ID: {}", limit, examId);

            List<ExamResult> results = examResultRepository.findByExamIdOrderByPercentageDesc(examId);
            return results.stream()
                    .limit(limit)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        /**
         * Update exam result
         */
        public ExamResultDTO updateExamResult(Long id, ExamResultDTO examResultDTO) {
            log.info("Updating exam result with ID: {}", id);

            ExamResult existingResult = examResultRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam result not found with ID: " + id));

            // Update fields
            BeanUtils.copyProperties(examResultDTO, existingResult,
                    "id", "examId", "studentId", "examAttemptId", "exam", "student", "examAttempt");

            // Update exam if changed
            if (examResultDTO.getExamId() != null && !examResultDTO.getExamId().equals(existingResult.getExam().getId())) {
                Exam exam = examRepository.findActiveById(examResultDTO.getExamId())
                        .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examResultDTO.getExamId()));
                existingResult.setExam(exam);
            }

            // Update student if changed
            if (examResultDTO.getStudentId() != null && !examResultDTO.getStudentId().equals(existingResult.getStudent().getId())) {
                Student student = studentRepository.findById(examResultDTO.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + examResultDTO.getStudentId()));
                existingResult.setStudent(student);
            }

            // Update exam attempt if changed
            if (examResultDTO.getExamAttemptId() != null) {
                ExamAttempt examAttempt = examAttemptRepository.findById(examResultDTO.getExamAttemptId())
                        .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found with ID: " + examResultDTO.getExamAttemptId()));
                existingResult.setExamAttempt(examAttempt);
            }

            ExamResult updatedResult = examResultRepository.save(existingResult);
            log.info("Updated exam result with ID: {}", updatedResult.getId());

            return convertToDTO(updatedResult);
        }

        /**
         * Delete exam result
         */
        public void deleteExamResult(Long id) {
            log.info("Deleting exam result with ID: {}", id);

            if (!examResultRepository.existsById(id)) {
                throw new ResourceNotFoundException("Exam result not found with ID: " + id);
            }

            examResultRepository.deleteById(id);
            log.info("Deleted exam result with ID: {}", id);
        }

        /**
         * Review exam result
         */
        public ExamResultDTO reviewExamResult(Long id, String reviewedBy, String teacherComments) {
            log.info("Reviewing exam result with ID: {} by: {}", id, reviewedBy);

            ExamResult examResult = examResultRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam result not found with ID: " + id));

            examResult.setIsReviewed(true);
            examResult.setReviewedBy(reviewedBy);
            examResult.setReviewedAt(LocalDateTime.now());
            examResult.setTeacherComments(teacherComments);

            ExamResult updatedResult = examResultRepository.save(examResult);
            log.info("Reviewed exam result with ID: {}", updatedResult.getId());

            return convertToDTO(updatedResult);
        }

        /**
         * Calculate and update ranks for all students in an exam
         */
        public void updateRanksForExam(Integer examId) {
            log.info("Updating ranks for exam ID: {}", examId);

            List<ExamResult> results = examResultRepository.findByExamIdOrderByPercentageDesc(examId);

            for (int i = 0; i < results.size(); i++) {
                ExamResult result = results.get(i);
                result.setRankInClass(i + 1);
                examResultRepository.save(result);
            }

            log.info("Updated ranks for {} students in exam ID: {}", results.size(), examId);
        }

        /**
         * Generate exam result from exam attempt
         */
        public ExamResultDTO generateExamResultFromAttempt(Long examAttemptId) {
            log.info("Generating exam result from exam attempt ID: {}", examAttemptId);

            ExamAttempt examAttempt = examAttemptRepository.findById(examAttemptId)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found with ID: " + examAttemptId));

            // Check if result already exists
            Optional<ExamResult> existingResult = examResultRepository
                    .findByStudentIdAndExamId(examAttempt.getStudent().getId(), examAttempt.getExam().getId());

            if (existingResult.isPresent()) {
                log.info("Exam result already exists, updating existing result");
                return updateExistingResultFromAttempt(existingResult.get(), examAttempt);
            }

            // Calculate result metrics
            Long correctAnswers = studentAnswerRepository.countCorrectAnswersByAttemptId(examAttemptId);
            Long wrongAnswers = studentAnswerRepository.countWrongAnswersByAttemptId(examAttemptId);
            Long unanswered = studentAnswerRepository.countUnansweredByAttemptId(examAttemptId);

            // Create new exam result
            ExamResult examResult = new ExamResult();
            examResult.setExam(examAttempt.getExam());
            examResult.setStudent(examAttempt.getStudent());
            examResult.setExamAttempt(examAttempt);
            examResult.setTotalScore(examAttempt.getScore());
            examResult.setTotalMarks(examAttempt.getTotalMarks());
            examResult.setPercentage(examAttempt.getPercentage());
            examResult.setGrade(calculateGrade(examAttempt.getPercentage()));
            examResult.setIsPassed(examAttempt.getPercentage() >= examAttempt.getExam().getPassMark());
            examResult.setCompletionTimeMinutes(examAttempt.getDurationMinutes());
            examResult.setCorrectAnswersCount(correctAnswers.intValue());
            examResult.setWrongAnswersCount(wrongAnswers.intValue());
            examResult.setUnansweredCount(unanswered.intValue());
            examResult.setRankInClass(calculateRankInClass(examAttempt.getExam().getId(), examAttempt.getPercentage()));

            ExamResult savedResult = examResultRepository.save(examResult);
            log.info("Generated exam result with ID: {}", savedResult.getId());

            return convertToDTO(savedResult);
        }

        /**
         * Get exam statistics
         */
//        @Transactional(readOnly = true)
        public ExamStatistics getExamStatistics(Integer examId) {
            log.info("Calculating statistics for exam ID: {}", examId);

            List<ExamResult> results = examResultRepository.findByExamId(examId);
            Long passedCount = examResultRepository.countPassedStudentsByExamId(examId);
            Double averagePercentage = examResultRepository.getAveragePercentageByExamId(examId);

            return ExamStatistics.builder()
                    .examId(examId)
                    .totalStudents(results.size())
                    .passedStudents(passedCount.intValue())
                    .failedStudents(results.size() - passedCount.intValue())
                    .averagePercentage(averagePercentage != null ? averagePercentage : 0.0)
                    .highestScore(results.stream().mapToDouble(ExamResult::getPercentage).max().orElse(0.0))
                    .lowestScore(results.stream().mapToDouble(ExamResult::getPercentage).min().orElse(0.0))
                    .build();
        }

        // Helper methods
        private ExamResultDTO convertToDTO(ExamResult examResult) {
            ExamResultDTO dto = new ExamResultDTO();
            BeanUtils.copyProperties(examResult, dto);
            dto.setExamId(examResult.getExam().getId());
            dto.setStudentId(examResult.getStudent().getId());
            if (examResult.getExamAttempt() != null) {
                dto.setExamAttemptId(examResult.getExamAttempt().getId());
            }
            return dto;
        }

        private ExamResultDTO updateExistingResultFromAttempt(ExamResult existingResult, ExamAttempt examAttempt) {
            // Calculate result metrics
            Long correctAnswers = studentAnswerRepository.countCorrectAnswersByAttemptId(examAttempt.getId());
            Long wrongAnswers = studentAnswerRepository.countWrongAnswersByAttemptId(examAttempt.getId());
            Long unanswered = studentAnswerRepository.countUnansweredByAttemptId(examAttempt.getId());

            // Update existing result
            existingResult.setExamAttempt(examAttempt);
            existingResult.setTotalScore(examAttempt.getScore());
            existingResult.setTotalMarks(examAttempt.getTotalMarks());
            existingResult.setPercentage(examAttempt.getPercentage());
            existingResult.setGrade(calculateGrade(examAttempt.getPercentage()));
            existingResult.setIsPassed(examAttempt.getPercentage() >= examAttempt.getExam().getPassMark());
            existingResult.setCompletionTimeMinutes(examAttempt.getDurationMinutes());
            existingResult.setCorrectAnswersCount(correctAnswers.intValue());
            existingResult.setWrongAnswersCount(wrongAnswers.intValue());
            existingResult.setUnansweredCount(unanswered.intValue());

            ExamResult updatedResult = examResultRepository.save(existingResult);
            return convertToDTO(updatedResult);
        }

        private Integer calculateRankInClass(Integer examId, Double percentage) {
            List<ExamResult> results = examResultRepository.findByExamIdOrderByPercentageDesc(examId);

            int rank = 1;
            for (ExamResult result : results) {
                if (result.getPercentage() > percentage) {
                    rank++;
                } else {
                    break;
                }
            }

            return rank;
        }

        private String calculateGrade(Double percentage) {
            if (percentage >= 90) return "A+";
            if (percentage >= 80) return "A";
            if (percentage >= 70) return "B";
            if (percentage >= 60) return "C";
            if (percentage >= 50) return "D";
            return "F";
        }

        // Inner class for exam statistics
        @lombok.Data
        @lombok.Builder
        public static class ExamStatistics {
            private Integer examId;
            private Integer totalStudents;
            private Integer passedStudents;
            private Integer failedStudents;
            private Double averagePercentage;
            private Double highestScore;
            private Double lowestScore;
        }
}

