package com.examination.online_examination_server.service;

import com.examination.online_examination_server.dto.StudentAnswerDTO;
import com.examination.online_examination_server.entity.StudentAnswer;
import com.examination.online_examination_server.exception.ResourceNotFoundException;
import com.examination.online_examination_server.repository.StudentAnswerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentAnswerService {

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;
    private final ModelMapper modelMapper;



    public StudentAnswerDTO saveAnswer(StudentAnswerDTO studentAnswerDTO) {
        log.info("Saving answer for student: {} and question: {}", studentAnswerDTO.getStudentId(), studentAnswerDTO.getQuestionId());

        // Check if answer already exists
        StudentAnswer existingAnswer = studentAnswerRepository.findByExamAttemptIdAndQuestionId(
                        studentAnswerDTO.getExamAttemptId(), studentAnswerDTO.getQuestionId())
                .orElse(null);

        if (existingAnswer != null) {
            // Update existing answer
            modelMapper.map(studentAnswerDTO, existingAnswer);
            StudentAnswer updatedAnswer = studentAnswerRepository.save(existingAnswer);
            return modelMapper.map(updatedAnswer, StudentAnswerDTO.class);
        } else {
            // Create new answer
            StudentAnswer studentAnswer = modelMapper.map(studentAnswerDTO, StudentAnswer.class);
            StudentAnswer savedAnswer = studentAnswerRepository.save(studentAnswer);
            return modelMapper.map(savedAnswer, StudentAnswerDTO.class);
        }
    }

//    @Transactional(readOnly = true)
    public List<StudentAnswerDTO> getAnswersByAttemptId(Long attemptId) {
        log.info("Fetching answers for attempt id: {}", attemptId);

        List<StudentAnswer> answers = studentAnswerRepository.findByExamAttemptId(attemptId);
        return answers.stream()
                .map(answer -> modelMapper.map(answer, StudentAnswerDTO.class))
                .collect(Collectors.toList());
    }

//    @Transactional(readOnly = true)
    public List<StudentAnswerDTO> getAnswersByStudentId(Integer studentId) {
        log.info("Fetching answers for student id: {}", studentId);

        List<StudentAnswer> answers = studentAnswerRepository.findByStudentId(studentId);
        return answers.stream()
                .map(answer -> modelMapper.map(answer, StudentAnswerDTO.class))
                .collect(Collectors.toList());
    }

//    @Transactional(readOnly = true)
    public StudentAnswerDTO getAnswerById(Long id) {
        log.info("Fetching answer with id: {}", id);

        StudentAnswer answer = studentAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student answer not found with id: " + id));

        return modelMapper.map(answer, StudentAnswerDTO.class);
    }

    public StudentAnswerDTO updateAnswer(Long id, StudentAnswerDTO studentAnswerDTO) {
        log.info("Updating answer with id: {}", id);

        StudentAnswer answer = studentAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student answer not found with id: " + id));

        modelMapper.map(studentAnswerDTO, answer);
        StudentAnswer updatedAnswer = studentAnswerRepository.save(answer);

        return modelMapper.map(updatedAnswer, StudentAnswerDTO.class);
    }

    public void deleteAnswer(Long id) {
        log.info("Deleting answer with id: {}", id);

        StudentAnswer answer = studentAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student answer not found with id: " + id));

        studentAnswerRepository.delete(answer);
    }

//    @Transactional(readOnly = true)
    public Long getCorrectAnswersCount(Long attemptId) {
        log.info("Getting correct answers count for attempt id: {}", attemptId);
        return studentAnswerRepository.countCorrectAnswersByAttemptId(attemptId);
    }

//    @Transactional(readOnly = true)
    public Long getWrongAnswersCount(Long attemptId) {
        log.info("Getting wrong answers count for attempt id: {}", attemptId);
        return studentAnswerRepository.countWrongAnswersByAttemptId(attemptId);
    }

//    @Transactional(readOnly = true)
    public Long getUnansweredCount(Long attemptId) {
        log.info("Getting unanswered questions count for attempt id: {}", attemptId);
        return studentAnswerRepository.countUnansweredByAttemptId(attemptId);
    }





}
