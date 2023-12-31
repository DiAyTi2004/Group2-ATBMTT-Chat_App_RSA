package com.chatapp.chat.controller;

import com.chatapp.chat.model.MessageDTO;
import com.chatapp.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.chatapp.chat.model.Message;

import java.util.List;

@Controller
public class ChatController {
    @Autowired
    private MessageService messageService;

    @MessageMapping("/public-message")
    @SendTo("/chatroom/public")
    public ResponseEntity<MessageDTO> receivePublicMessage(@Payload MessageDTO message) {
        if (message == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<MessageDTO>(message, HttpStatus.OK);
    }

    @MessageMapping("/notification")
    public ResponseEntity<MessageDTO> receiveNotification(@Payload MessageDTO message) {
        if (message == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        message = messageService.handlerForNotification(message);
        messageService.sendMessageTo("/notification", message);
        return new ResponseEntity<MessageDTO>(message, HttpStatus.OK);
    }

    @MessageMapping("/privateMessage")
    public ResponseEntity<MessageDTO> spreadMessageToRoomId(@Payload MessageDTO message) {
        MessageDTO res = messageService.sendPrivateMessage(message);
        if (res == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<MessageDTO>(res, HttpStatus.OK);
    }

}
