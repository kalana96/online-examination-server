package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.dto.ChatHistoryResponse;
import com.examination.online_examination_server.dto.ChatMessageDTO;
import com.examination.online_examination_server.dto.SendChatMessageRequest;
import com.examination.online_examination_server.service.ExamChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teacher/exam-chat")
public class TeacherExamChatController {

    @Autowired
    private ExamChatService examChatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @RequestBody SendChatMessageRequest request,
            Authentication authentication) {
        Integer teacherId = Integer.parseInt(authentication.getName()); // Assuming username is teacher ID
        ChatMessageDTO message = examChatService.sendMessageFromTeacher(request, teacherId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/history/{examId}/{studentId}")
    public ResponseEntity<ChatHistoryResponse> getChatHistory(
            @PathVariable Integer examId,
            @PathVariable Integer studentId,
            Authentication authentication) {
        ChatHistoryResponse history = examChatService.getChatHistory(examId, studentId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/active-chats/{examId}")
    public ResponseEntity<List<Integer>> getActiveStudentChats(
            @PathVariable Integer examId,
            Authentication authentication) {
        List<Integer> activeChats = examChatService.getActiveStudentChats(examId);
        return ResponseEntity.ok(activeChats);
    }

    @PostMapping("/mark-read/{examId}/{studentId}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Integer examId,
            @PathVariable Integer studentId,
            Authentication authentication) {
        Integer teacherId = Integer.parseInt(authentication.getName());
        examChatService.markMessagesAsRead(examId, studentId, teacherId, "TEACHER");
        return ResponseEntity.ok().build();
    }
}