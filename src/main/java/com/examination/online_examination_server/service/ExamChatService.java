package com.examination.online_examination_server.service;

import com.examination.online_examination_server.dto.ChatHistoryResponse;
import com.examination.online_examination_server.dto.ChatMessageDTO;
import com.examination.online_examination_server.dto.SendChatMessageRequest;
import com.examination.online_examination_server.entity.*;
import com.examination.online_examination_server.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamChatService {

    @Autowired
    private ExamChatMessageRepository chatMessageRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageDTO sendMessageFromStudent(SendChatMessageRequest request, Integer studentId) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ExamChatMessage message = new ExamChatMessage();
        message.setExam(exam);
        message.setStudent(student);
        message.setTeacher(exam.getTeacher());
        message.setMessage(request.getMessage());
        message.setSenderType(ExamChatMessage.SenderType.STUDENT);
        message.setMessageType(ExamChatMessage.MessageType.valueOf(request.getMessageType()));

        ExamChatMessage savedMessage = chatMessageRepository.save(message);

        ChatMessageDTO dto = convertToDTO(savedMessage);

        // Send to teacher via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/exam/" + request.getExamId() + "/teacher/" + exam.getTeacher().getId(),
                dto
        );

        return dto;
    }

    @Transactional
    public ChatMessageDTO sendMessageFromTeacher(SendChatMessageRequest request, Integer teacherId) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ExamChatMessage message = new ExamChatMessage();
        message.setExam(exam);
        message.setStudent(student);
        message.setTeacher(teacher);
        message.setMessage(request.getMessage());
        message.setSenderType(ExamChatMessage.SenderType.TEACHER);
        message.setMessageType(ExamChatMessage.MessageType.valueOf(request.getMessageType()));

        ExamChatMessage savedMessage = chatMessageRepository.save(message);

        ChatMessageDTO dto = convertToDTO(savedMessage);

        // Send to student via WebSocket
        messagingTemplate.convertAndSend(
                "/topic/exam/" + request.getExamId() + "/student/" + request.getStudentId(),
                dto
        );

        return dto;
    }

    public ChatHistoryResponse getChatHistory(Integer examId, Integer studentId) {
        List<ExamChatMessage> messages = chatMessageRepository.findChatHistory(examId, studentId);
        List<ChatMessageDTO> messageDTOs = messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.info("Retrieved chat history for exam ID: {}, student ID: {}", examId, studentId);
        Long unreadCount = chatMessageRepository.countUnreadTeacherMessages(examId, studentId);
        log.info("Unread messages count for exam ID: {}, student ID: {} is {}", examId, studentId, unreadCount);

        ChatHistoryResponse response = new ChatHistoryResponse();
        response.setMessages(messageDTOs);
        response.setUnreadCount(unreadCount);
        response.setChatEnabled(true);

        return response;
    }

    @Transactional
    public void markMessagesAsRead(Integer examId, Integer studentId, Integer userId, String userType) {
        List<ExamChatMessage> unreadMessages;

        if ("STUDENT".equals(userType)) {
            unreadMessages = chatMessageRepository.findUnreadMessages(examId, studentId)
                    .stream()
                    .filter(msg -> msg.getSenderType() == ExamChatMessage.SenderType.TEACHER)
                    .collect(Collectors.toList());
        } else {
            unreadMessages = chatMessageRepository.findUnreadMessages(examId, studentId)
                    .stream()
                    .filter(msg -> msg.getSenderType() == ExamChatMessage.SenderType.STUDENT)
                    .collect(Collectors.toList());
        }

        unreadMessages.forEach(msg -> msg.setRead(true));
        chatMessageRepository.saveAll(unreadMessages);
    }

    public List<Integer> getActiveStudentChats(Integer examId) {
        return chatMessageRepository.findActiveStudentChats(examId);
    }

    private ChatMessageDTO convertToDTO(ExamChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setExamId(message.getExam().getId());
        dto.setStudentId(message.getStudent().getId());
        dto.setTeacherId(message.getTeacher().getId());
        dto.setStudentName(message.getStudent().getFullName());
        dto.setTeacherName(message.getTeacher().getFirstName() + " " + message.getTeacher().getLastName());
        dto.setMessage(message.getMessage());
        dto.setSenderType(message.getSenderType().toString());
        dto.setMessageType(message.getMessageType().toString());
        dto.setRead(message.isRead());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}