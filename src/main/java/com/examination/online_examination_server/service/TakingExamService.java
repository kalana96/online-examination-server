package com.examination.online_examination_server.service;

import com.examination.online_examination_server.Utility.VarList;
import com.examination.online_examination_server.constant.VarListt;
import com.examination.online_examination_server.dto.*;
import com.examination.online_examination_server.entity.*;
import com.examination.online_examination_server.entity.Class;
import com.examination.online_examination_server.exception.*;
import com.examination.online_examination_server.exception.exam.ClassNotFoundException;
import com.examination.online_examination_server.exception.exam.*;
import com.examination.online_examination_server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TakingExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamAttemptRepository examAttemptRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamRegistrationRepository examRegistrationRepository;

    private final ModelMapper modelMapper;


    public ExamTakeDTO getExamForTaking(Integer examId, Integer studentId) {
        log.info("Checking exam eligibility for exam {} and student {}", examId, studentId);

        // Get exam details
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        // Check if exam is active and within time bounds
        if (!isExamAvailable(exam)) {
            throw new ExamNotAvailableException("Exam is not currently available");
        }

        // Check if student is registered for this exam
        boolean isRegistered = examRegistrationRepository.existsByExamIdAndStudentId(examId, studentId);
        if (!isRegistered) {
            throw new StudentNotRegisteredException("Student is not registered for this exam");
        }

        // Check attempt count vs max attempts
        Long attemptCount = examAttemptRepository.countByStudentIdAndExamId(studentId, examId);
        if (attemptCount >= exam.getStudentCount()) {
            throw new MaxAttemptsExceededException("Maximum attempts exceeded for this exam");
        }

        // Check if there's already a submitted attempt
        Optional<ExamAttempt> submittedAttempt = examAttemptRepository.findByStudentIdAndExamIdAndStatus(studentId, examId, ExamAttempt.AttemptStatus.SUBMITTED);

        if (submittedAttempt.isPresent()) {
            throw new ExamAlreadySubmittedException("You have already submitted this exam and cannot retake it");
        }

        // Check if student has reached maximum submitted attempts
//        Long submittedAttemptCount = examAttemptRepository
//                .countByStudentIdAndExamIdAndStatus(studentId, examId, ExamAttempt.AttemptStatus.SUBMITTED);
//
//        if (submittedAttemptCount >= exam.getStudentCount()) {
//            throw new MaxAttemptsExceededException("You have already submitted the maximum allowed attempts for this exam");
//        }

        // Check if there's an active attempt
        Optional<ExamAttempt> activeAttempt = examAttemptRepository.findByStudentIdAndExamIdAndStatus(studentId, examId, ExamAttempt.AttemptStatus.IN_PROGRESS);

        ExamTakeDTO examTakeDTO = modelMapper.map(exam, ExamTakeDTO.class);
        examTakeDTO.setAttemptCount(attemptCount.intValue());
        examTakeDTO.setMaxAttempts(exam.getStudentCount());
        examTakeDTO.setHasActiveAttempt(activeAttempt.isPresent());

        if (activeAttempt.isPresent()) {
            examTakeDTO.setActiveAttemptId(activeAttempt.get().getId());
        }

        return examTakeDTO;
    }

    public ExamSessionDTO startExamSession(Integer examId, Integer studentId, String ipAddress) {
        log.info("Starting exam session for exam {} and student {}", examId, studentId);

        // Get or create exam attempt
        ExamAttempt examAttempt = getOrCreateExamAttempt(examId, studentId, ipAddress);

        // Get questions for the exam (shuffle if needed)
        List<Question> questions = questionRepository.findByExamIdOrderByIdAsc(examId);

        if (questions.isEmpty()) {
            throw new NoQuestionsFoundException("No questions found for this exam");
        }

        // Convert to DTOs without correct answers
        List<QuestionTakeDTO> questionDTOs = questions.stream().map(this::convertToQuestionTakeDTO).collect(Collectors.toList());

        // Get existing answers if resuming
        List<StudentAnswer> existingAnswers = studentAnswerRepository.findByExamAttemptId(examAttempt.getId());

        Map<Long, StudentAnswerDTO> answerMap = existingAnswers.stream().collect(Collectors.toMap(answer -> answer.getQuestion().getId(), answer -> modelMapper.map(answer, StudentAnswerDTO.class)));

        ExamSessionDTO sessionDTO = new ExamSessionDTO();
        sessionDTO.setAttemptId(examAttempt.getId());
        sessionDTO.setExamId(examId);
        sessionDTO.setStudentId(studentId);
        sessionDTO.setExamName(examAttempt.getExam().getExamName());
        sessionDTO.setDurationMinutes(examAttempt.getExam().getDuration());
        sessionDTO.setStartTime(examAttempt.getStartTime());
        sessionDTO.setQuestions(questionDTOs);
        sessionDTO.setExistingAnswers(answerMap);
        sessionDTO.setTotalQuestions(questions.size());
        sessionDTO.setMaxMarks(examAttempt.getExam().getMaxMark());

        return sessionDTO;
    }

    public void autoSaveProgress(Long attemptId, List<StudentAnswerDTO> answers) {
        log.info("Auto-saving {} answers for attempt {}", answers.size(), attemptId);

        ExamAttempt examAttempt = examAttemptRepository.findById(attemptId).orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found"));

        if (examAttempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS) {
            throw new ExamNotInProgressException("Cannot save answers for non-active exam attempt");
        }

        for (StudentAnswerDTO answerDTO : answers) {
            saveOrUpdateAnswer(answerDTO, attemptId, examAttempt);
        }
    }

    public ExamSubmissionResultDTO submitExamFinal(Long attemptId) {
        log.info("Processing final submission for attempt {}", attemptId);

        ExamAttempt examAttempt = examAttemptRepository.findById(attemptId).orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found"));

        if (examAttempt.getStatus() != ExamAttempt.AttemptStatus.IN_PROGRESS) {
            throw new ExamNotInProgressException("Exam attempt is not in progress");
        }

        // Mark as submitted
        examAttempt.setEndTime(LocalDateTime.now());
        examAttempt.setSubmittedAt(LocalDateTime.now());
        examAttempt.setIsSubmitted(true);
        examAttempt.setStatus(ExamAttempt.AttemptStatus.SUBMITTED);

        // Calculate duration
        long durationMinutes = Duration.between(examAttempt.getStartTime(), examAttempt.getEndTime()).toMinutes();
        examAttempt.setDurationMinutes((int) durationMinutes);

        // Auto-grade multiple choice and true/false questions
        List<StudentAnswer> answers = studentAnswerRepository.findByExamAttemptId(attemptId);
        int totalScore = 0;
        int totalMarks = 0;
        int correctAnswers = 0;
        int wrongAnswers = 0;
        int unanswered = 0;

        List<Question> examQuestions = questionRepository.findByExamId(examAttempt.getExam().getId());

        for (Question question : examQuestions) {
            totalMarks += question.getMarks();

            StudentAnswer studentAnswer = answers.stream().filter(ans -> ans.getQuestion().getId().equals(question.getId())).findFirst().orElse(null);

            if (studentAnswer == null || studentAnswer.getAnswerText() == null || studentAnswer.getAnswerText().trim().isEmpty()) {
                unanswered++;
                continue;
            }

            // Auto-grade for objective questions
            if (question.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE || question.getQuestionType() == Question.QuestionType.TRUE_FALSE) {

                boolean isCorrect = question.getCorrectAnswer().trim().equalsIgnoreCase(studentAnswer.getAnswerText().trim());

                studentAnswer.setIsCorrect(isCorrect);
                studentAnswer.setIsMarked(true);

                if (isCorrect) {
                    studentAnswer.setMarksAwarded((double) question.getMarks());
                    totalScore += question.getMarks();
                    correctAnswers++;
                } else {
                    studentAnswer.setMarksAwarded(0.0);
                    wrongAnswers++;
                }

                studentAnswerRepository.save(studentAnswer);
            }
        }

        // Update exam attempt with scores
        examAttempt.setScore((double) totalScore);
        examAttempt.setTotalMarks(totalMarks);
        examAttempt.setPercentage(totalMarks > 0 ? (double) totalScore / totalMarks * 100 : 0.0);

        examAttemptRepository.save(examAttempt);

        // Create submission result
        ExamSubmissionResultDTO result = new ExamSubmissionResultDTO();
        result.setAttemptId(attemptId);
        result.setExamName(examAttempt.getExam().getExamName());
        result.setSubmittedAt(examAttempt.getSubmittedAt());
        result.setDurationMinutes(examAttempt.getDurationMinutes());
        result.setScore(totalScore);
        result.setTotalMarks(totalMarks);
        result.setPercentage(examAttempt.getPercentage());
        result.setCorrectAnswers(correctAnswers);
        result.setWrongAnswers(wrongAnswers);
        result.setUnanswered(unanswered);
        result.setTotalQuestions(examQuestions.size());
        result.setPassMark(examAttempt.getExam().getPassMark());
        result.setPassed(totalScore >= examAttempt.getExam().getPassMark());

        return result;
    }

    // Helper methods
    private boolean isExamAvailable(Exam exam) {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        if (!exam.getExamDate().equals(today)) {
            return false;
        }

        LocalTime startTime = LocalTime.parse(exam.getStartTime());
        LocalTime endTime = LocalTime.parse(exam.getEndTime());

        return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime) && exam.getStatus() == Exam.ExamStatus.ACTIVE;
    }

    private ExamAttempt getOrCreateExamAttempt(Integer examId, Integer studentId, String ipAddress) {
        // Check for existing active attempt
        Optional<ExamAttempt> activeAttempt = examAttemptRepository.findByStudentIdAndExamIdAndStatus(studentId, examId, ExamAttempt.AttemptStatus.IN_PROGRESS);

        if (activeAttempt.isPresent()) {
            return activeAttempt.get();
        }

        // Create new attempt
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        ExamAttempt newAttempt = new ExamAttempt();

        Student student = studentRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        newAttempt.setStudent(student);
        newAttempt.setExam(exam);
        newAttempt.setStartTime(LocalDateTime.now());
        newAttempt.setStatus(ExamAttempt.AttemptStatus.IN_PROGRESS);
        newAttempt.setIpAddress(ipAddress);
        newAttempt.setIsSubmitted(false);

        return examAttemptRepository.save(newAttempt);
    }

    private void saveOrUpdateAnswer(StudentAnswerDTO answerDTO, Long attemptId, ExamAttempt examAttempt) {
        Optional<StudentAnswer> existing = studentAnswerRepository.findByExamAttemptIdAndQuestionId(attemptId, answerDTO.getQuestionId());

        if (existing.isPresent()) {
            StudentAnswer answer = existing.get();
            answer.setAnswerText(answerDTO.getAnswerText());
            answer.setTimeSpentSeconds(answerDTO.getTimeSpentSeconds());
            answer.setIsFlagged(answerDTO.getIsFlagged());
            answer.setFlagReason(answerDTO.getFlagReason());
            studentAnswerRepository.save(answer);
        } else {
            // Create new answer
            StudentAnswer newAnswer = new StudentAnswer();
            newAnswer.setAnswerText(answerDTO.getAnswerText());
            newAnswer.setTimeSpentSeconds(answerDTO.getTimeSpentSeconds());
            newAnswer.setIsFlagged(answerDTO.getIsFlagged());
            newAnswer.setFlagReason(answerDTO.getFlagReason());
            newAnswer.setExamAttempt(examAttempt); // Set the ExamAttempt entity

            // Get and set the Question entity
            Question question = questionRepository.findById(answerDTO.getQuestionId()).orElseThrow(() -> new ResourceNotFoundException("Question not found"));
            newAnswer.setQuestion(question);

            // Get and set the Student entity
            Student student = examAttempt.getStudent();
            newAnswer.setStudent(student);

            studentAnswerRepository.save(newAnswer);
        }
    }

    private QuestionTakeDTO convertToQuestionTakeDTO(Question question) {
        QuestionTakeDTO dto = new QuestionTakeDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setMarks(question.getMarks());
        dto.setOptions(question.getOptions());
        dto.setImageUrl(question.getImageUrl());
        dto.setAudioUrl(question.getAudioUrl());
        dto.setVideoUrl(question.getVideoUrl());
        // Note: correctAnswer is not included for security
        return dto;
    }


}



