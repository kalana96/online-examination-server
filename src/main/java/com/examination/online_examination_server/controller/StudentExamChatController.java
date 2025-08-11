package com.examination.online_examination_server.controller;

import com.examination.online_examination_server.dto.ChatHistoryResponse;
import com.examination.online_examination_server.dto.ChatMessageDTO;
import com.examination.online_examination_server.dto.SendChatMessageRequest;
import com.examination.online_examination_server.service.ExamChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/student/exam-chat")
public class StudentExamChatController {

    @Autowired
    private ExamChatService examChatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @RequestBody SendChatMessageRequest request,
            Authentication authentication) {
        Integer studentId = Integer.parseInt(authentication.getName()); // Assuming username is student ID
        ChatMessageDTO message = examChatService.sendMessageFromStudent(request, studentId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/history/{examId}")
    public ResponseEntity<ChatHistoryResponse> getChatHistory(
            @PathVariable Integer examId,
            Authentication authentication) {
        log.warn("Retrieveddddddddddddddddddddd");
        Integer studentId = Integer.parseInt(authentication.getName());
        ChatHistoryResponse history = examChatService.getChatHistory(examId, studentId);
        log.info("Retrieved chat history for exam ID: {}", examId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/mark-read/{examId}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Integer examId,
            Authentication authentication) {
        Integer studentId = Integer.parseInt(authentication.getName());
        examChatService.markMessagesAsRead(examId, studentId, studentId, "STUDENT");
        return ResponseEntity.ok().build();
    }
}
