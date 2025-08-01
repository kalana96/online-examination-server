package com.examination.online_examination_server.service;

import com.examination.online_examination_server.dto.ExamAttemptDTO;
import com.examination.online_examination_server.dto.TimeRemainingDTO;
import com.examination.online_examination_server.entity.ExamAttempt;
import com.examination.online_examination_server.exception.ExamNotInProgressException;
import com.examination.online_examination_server.exception.ResourceNotFoundException;
import com.examination.online_examination_server.repository.ExamAttemptRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExamAttemptService {

    @Autowired
    private ExamAttemptRepository examAttemptRepository
            ;
    private final ModelMapper modelMapper;

    public ExamAttemptDTO startExam(ExamAttemptDTO examAttemptDTO) {
        log.info("Starting exam attempt for student: {} and exam: {}", examAttemptDTO.getStudentId(), examAttemptDTO.getExamId());

        // Check if student has already started the exam
        List<ExamAttempt> existingAttempts = examAttemptRepository.findByStudentIdAndExamId(
                examAttemptDTO.getStudentId(), examAttemptDTO.getExamId());

        if (!existingAttempts.isEmpty()) {
            ExamAttempt inProgressAttempt = existingAttempts.stream()
                    .filter(attempt -> attempt.getStatus() == ExamAttempt.AttemptStatus.IN_PROGRESS)
                    .findFirst()
                    .orElse(null);

            if (inProgressAttempt != null) {
                return modelMapper.map(inProgressAttempt, ExamAttemptDTO.class);
            }
        }

        ExamAttempt examAttempt = modelMapper.map(examAttemptDTO, ExamAttempt.class);
        examAttempt.setStartTime(LocalDateTime.now());
        examAttempt.setStatus(ExamAttempt.AttemptStatus.IN_PROGRESS);

        ExamAttempt savedAttempt = examAttemptRepository.save(examAttempt);
        return modelMapper.map(savedAttempt, ExamAttemptDTO.class);
    }

    public ExamAttemptDTO submitExam(Long attemptId) {
        log.info("Submitting exam attempt with id: {}", attemptId);

        ExamAttempt examAttempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found with id: " + attemptId));

        examAttempt.setEndTime(LocalDateTime.now());
        examAttempt.setSubmittedAt(LocalDateTime.now());
        examAttempt.setIsSubmitted(true);
        examAttempt.setStatus(ExamAttempt.AttemptStatus.SUBMITTED);

        // Calculate duration
        if (examAttempt.getStartTime() != null) {
            long durationMinutes = java.time.Duration.between(examAttempt.getStartTime(), examAttempt.getEndTime()).toMinutes();
            examAttempt.setDurationMinutes((int) durationMinutes);
        }

        ExamAttempt updatedAttempt = examAttemptRepository.save(examAttempt);
        return modelMapper.map(updatedAttempt, ExamAttemptDTO.class);
    }

//    @Transactional(readOnly = true)
    public ExamAttemptDTO getExamAttemptById(Long id) {
        log.info("Fetching exam attempt with id: {}", id);

        ExamAttempt examAttempt = examAttemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found with id: " + id));

        return modelMapper.map(examAttempt, ExamAttemptDTO.class);
    }

//    @Transactional(readOnly = true)
    public List<ExamAttemptDTO> getExamAttemptsByStudentId(Integer studentId) {
        log.info("Fetching exam attempts for student id: {}", studentId);

        List<ExamAttempt> attempts = examAttemptRepository.findByStudentId(studentId);
        return attempts.stream()
                .map(attempt -> modelMapper.map(attempt, ExamAttemptDTO.class))
                .collect(Collectors.toList());
    }

//    @Transactional(readOnly = true)
    public Page<ExamAttemptDTO> getExamAttemptsByExamId(Integer examId, Pageable pageable) {
        log.info("Fetching exam attempts for exam id: {}", examId);

        Page<ExamAttempt> attempts = examAttemptRepository.findByExamId(examId, pageable);
        List<ExamAttemptDTO> attemptDTOs = attempts.getContent().stream()
                .map(attempt -> modelMapper.map(attempt, ExamAttemptDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<>(attemptDTOs, pageable, attempts.getTotalElements());
    }

//    @Transactional(readOnly = true)
    public ExamAttemptDTO getActiveAttempt(Integer studentId, Integer examId) {
        log.info("Fetching active attempt for student: {} and exam: {}", studentId, examId);

        ExamAttempt activeAttempt = examAttemptRepository.findByStudentIdAndExamIdAndStatus(
                        studentId, examId, ExamAttempt.AttemptStatus.IN_PROGRESS)
                .orElse(null);

        return activeAttempt != null ? modelMapper.map(activeAttempt, ExamAttemptDTO.class) : null;
    }

//    @Transactional(readOnly = true)
    public Long getAttemptCount(Integer studentId, Integer examId) {
        log.info("Getting attempt count for student: {} and exam: {}", studentId, examId);

        return examAttemptRepository.countByStudentIdAndExamId(studentId, examId);
    }


    public TimeRemainingDTO getTimeRemaining(Long attemptId) {
        log.info("Calculating time remaining for attempt {}", attemptId);

        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found"));

        if (attempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS) {
            throw new ExamNotInProgressException("Exam attempt is not in progress");
        }

        LocalDateTime startTime = attempt.getStartTime();
        int durationMinutes = attempt.getExam().getDuration();
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        LocalDateTime now = LocalDateTime.now();

        long minutesRemaining = Duration.between(now, endTime).toMinutes();
        long secondsRemaining = Duration.between(now, endTime).getSeconds() % 60;

        TimeRemainingDTO timeRemaining = new TimeRemainingDTO();
        timeRemaining.setMinutesRemaining(Math.max(0, minutesRemaining));
        timeRemaining.setSecondsRemaining(Math.max(0, secondsRemaining));
        timeRemaining.setTotalSecondsRemaining(Math.max(0, Duration.between(now, endTime).getSeconds()));
        timeRemaining.setTimeExpired(now.isAfter(endTime));

        // Auto-submit if time expired
        if (timeRemaining.isTimeExpired() && attempt.getStatus() == ExamAttempt.AttemptStatus.IN_PROGRESS) {
            attempt.setEndTime(endTime);
            attempt.setSubmittedAt(LocalDateTime.now());
            attempt.setIsSubmitted(true);
            attempt.setStatus(ExamAttempt.AttemptStatus.AUTO_SUBMITTED);
            examAttemptRepository.save(attempt);
        }

        return timeRemaining;
    }


}
