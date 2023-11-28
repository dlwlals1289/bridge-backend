package com.Bridge.bridge.controller;

import com.Bridge.bridge.dto.request.ChatMessageRequest;
import com.Bridge.bridge.dto.response.ChatMessageResponse;
import com.Bridge.bridge.service.AlarmService;
import com.Bridge.bridge.service.ChatService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {
    private final ChatService chatService;


    /**
     * 채팅방에 메세지 보내는 경우 (입장 메세지는 따로 구현 x)
     */
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest chatMessageRequest) throws FirebaseMessagingException {
        log.info("message = {}", chatMessageRequest.getMessage());

        //메세지 저장
        ChatMessageRequest messageRequest = chatService.saveMessage(chatMessageRequest);

        //메세지 클라이언트로 전송
        chatService.sendMesssage(messageRequest);
    }
}
