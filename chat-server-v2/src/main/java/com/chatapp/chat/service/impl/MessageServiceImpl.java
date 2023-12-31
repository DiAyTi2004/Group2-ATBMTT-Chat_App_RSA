package com.chatapp.chat.service.impl;

import com.chatapp.chat.entity.*;
import com.chatapp.chat.model.MessageDTO;
import com.chatapp.chat.model.MessageTypeDTO;
import com.chatapp.chat.model.RSAKeyDTO;
import com.chatapp.chat.model.UserDTO;
import com.chatapp.chat.repository.MessageRepository;
import com.chatapp.chat.repository.RoomRepository;
import com.chatapp.chat.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageTypeService messageTypeService;
    @Autowired
    private SetupDataService setupDataService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private RoomService roomService;

    @Override
    public MessageDTO createMessage(MessageDTO dto) {
        if (dto == null) return null;
        Room roomEntity = roomRepository.findById(dto.getRoom().getId()).orElse(null);
        if (roomEntity == null) return null;
        User currentUser = userService.getCurrentLoginUserEntity();
        if (currentUser == null) return null;

        MessageType messageType = null;
        if (dto.getMessageType() != null)
            messageType = messageTypeService.getMessageTypeEntityByName(dto.getMessageType().getName());
        else {
            messageType = messageTypeService.getMessageTypeEntityByName("chat");
            if (messageType == null) setupDataService.setupData();
            messageType = messageTypeService.getMessageTypeEntityByName("chat");
            if (messageType == null) return null;
        }

        Message messageEntity = new Message();
        messageEntity.setMessageType(messageType);
        messageEntity.setContent(dto.getContent());
        messageEntity.setRoom(roomEntity);
        messageEntity.setUser(currentUser);
        messageEntity.setSendDate(new Date());

        return new MessageDTO(messageRepository.saveAndFlush(messageEntity));
    }

    @Override
    public MessageDTO createMessageAttachedUser(MessageDTO dto) {
        if (dto == null) return null;
        if (dto.getRoom() == null) return null;
        Room roomEntity = roomRepository.findById(dto.getRoom().getId()).orElse(null);
        if (roomEntity == null) return null;
        if (dto.getUser() == null) return null;
        User currentUser = userService.getUserEntityById(dto.getUser().getId());
        if (currentUser == null) return null;

        MessageType messageType = null;
        if (dto.getMessageType() != null)
            messageType = messageTypeService.getMessageTypeEntityByName(dto.getMessageType().getName());
        else {
            messageType = messageTypeService.getMessageTypeEntityByName("chat");
            if (messageType == null) setupDataService.setupData();
            messageType = messageTypeService.getMessageTypeEntityByName("chat");
            if (messageType == null) return null;
        }

        Message messageEntity = new Message();
        messageEntity.setMessageType(messageType);
        messageEntity.setContent(dto.getContent());
        messageEntity.setRoom(roomEntity);
        messageEntity.setUser(currentUser);
        messageEntity.setSendDate(new Date());

        return new MessageDTO(messageRepository.saveAndFlush(messageEntity));
    }

    @Override
    public List<MessageDTO> findTop10PreviousByMileStone(MessageDTO mileStone) {
        List<MessageDTO> data = messageRepository.findTop10ByRoomAndSendDateBeforeOrderBySendDateDesc(mileStone.getRoom().getId(), mileStone.getSendDate(), PageRequest.of(0, 10));
        List<MessageDTO> res = new ArrayList<>();
        for (int i = data.size() - 1; i >= 0; i--) {
            res.add(data.get(i));
        }
        return res;
    }

    @Override
    public Set<MessageDTO> getAllMessagesByRoomId(UUID roomId) {
        return messageRepository.getAllMessagesByRoomId(roomId);
    }

    @Override
    public List<MessageDTO> get20LatestMessagesByRoomId(UUID roomId) {
        List<MessageDTO> data = messageRepository.get20LatestMessagesByRoomId(roomId, PageRequest.of(0, 20));
        List<MessageDTO> res = new ArrayList<>();
        for (int i = data.size() - 1; i >= 0; i--) {
            res.add(data.get(i));
        }
        return res;
    }

    @Override
    public MessageDTO handlerForNotification(MessageDTO dto) {
        MessageType typeEntity = messageTypeService.getMessageTypeEntityByName("notification");
        if (typeEntity == null) setupDataService.setupData();
        typeEntity = messageTypeService.getMessageTypeEntityByName("notification");
        if (typeEntity == null) return null;
        User userEntity = userService.getUserEntityById(dto.getUser().getId());
        if (userEntity == null) return null;

        Message message = new Message();
        message.setSendDate(new Date());
        message.setUser(userEntity);
        message.setMessageType(typeEntity);
        message.setContent(dto.getContent());

        if (dto.getRoom() != null) {
            Room roomEntity = roomRepository.findById(dto.getRoom().getId()).orElse(null);
            if (roomEntity != null) {
                message.setRoom(roomEntity);
            }
        }

        Message res = messageRepository.saveAndFlush(message);

        return new MessageDTO(res);
    }

    @Override
    public void sendMessageTo(String destination, MessageDTO dto) {
        simpMessagingTemplate.convertAndSendToUser(dto.getUser().getId().toString(), destination, dto);
    }

    @Override
    public boolean isInRoomChat(MessageDTO messageDTO){
        if (messageDTO.getUser() == null) return false;
        User currentUser = userService.getUserEntityById(messageDTO.getUser().getId());

        Room roomEntity = roomRepository.findById(messageDTO.getRoom().getId()).orElse(null);
        if (roomEntity == null) return false;

        for(UserRoom userRoom: roomEntity.getUserRooms()){
            if(userRoom.getUser().getId().equals(currentUser.getId())) return true;
        }

        return false;
    }

    @Override
    public MessageDTO sendPrivateMessage(MessageDTO messageDTO) {
        if(!isInRoomChat(messageDTO)) return null;

        if (messageDTO == null) return null;
        if (messageDTO.getUser() == null) return null;
        User currentUser = userService.getUserEntityById(messageDTO.getUser().getId());
        if (currentUser == null) return null;
        Room roomEntity = roomRepository.findById(messageDTO.getRoom().getId()).orElse(null);
        if (roomEntity == null) return null;

        String decryptedContent = messageDTO.getContent();
        // content is decrypted by room public key if this message is private for chat
        if (messageDTO.getMessageType() == null) return null;
        if (messageDTO.getMessageType().getName().equals("chat")) {
            RSAKey privateKey = roomEntity.getPrivateKey();
            decryptedContent = handleDecryptMessage(messageDTO.getContent(), new RSAKeyDTO(privateKey));
        }

        Message messageEntity = new Message();
        messageEntity.setRoom(roomEntity);
        messageEntity.setContent(decryptedContent);
        MessageType messageTypeEntity = messageTypeService.getMessageTypeEntityByName(messageDTO.getMessageType().getName());
        if (messageTypeEntity == null) {
            setupDataService.setupData();
        }
        messageTypeEntity = messageTypeService.getMessageTypeEntityByName(messageDTO.getMessageType().getName());
        if (messageTypeEntity == null) return null;
        messageEntity.setMessageType(messageTypeEntity);
        messageEntity.setSendDate(new Date());
        messageEntity.setUser(currentUser);
        Message res = messageRepository.saveAndFlush(messageEntity);

        List<MessageDTO> latestMessages = get20LatestMessagesByRoomId(res.getRoom().getId());

        MessageDTO resDto = new MessageDTO(res);
        if (resDto == null) return null;
        resDto.getRoom().setMessages(latestMessages);
        resDto.getRoom().setParticipants(roomService.getAllJoinedUsersByRoomId(resDto.getRoom().getId()));

        Set<UserRoom> userRooms = roomEntity.getUserRooms();
        List<User> users = new ArrayList<>();
        Set<UUID> userIdSet = new HashSet<>();
        for (UserRoom ur : userRooms) {
            User loopingUser = ur.getUser();
            if (userIdSet.contains(loopingUser.getId())) continue;
            users.add(loopingUser);
            userIdSet.add(loopingUser.getId());
        }

        String intactContent = resDto.getContent();
        for (User user : users) {
            if (currentUser.getId() != user.getId()) {
                RSAKey publicKey = user.getPublicKey();

                String encryptedMessage = intactContent;
                if (resDto.getMessageType().getName().equals("chat"))
                    encryptedMessage = handleEncryptMessage(intactContent, new RSAKeyDTO(publicKey));

                resDto.setContent(encryptedMessage);
                for (MessageDTO messageToUser : resDto.getRoom().getMessages()) {
                    if (messageToUser.getMessageType().getName().equals("chat"))
                        messageToUser.setContent(handleEncryptMessage(messageToUser.getContent(), new RSAKeyDTO(publicKey)));
                }
                simpMessagingTemplate.convertAndSendToUser(user.getId().toString(), "/privateMessage", resDto);
            }
        }

        return resDto;
    }

    @Override
    public String handleEncryptMessage(String message, RSAKeyDTO publicKeyDto) {
        try {
            int messageLength = message.length();
            String[] encryptedChars = new String[messageLength];

            for (int i = 0; i < messageLength; i++) {
                char currentChar = message.charAt(i);
                encryptedChars[i] = encryptMessage(String.valueOf(currentChar), publicKeyDto);
            }

            String encryptedString = String.join(",", encryptedChars);
            return encryptedString;
        } catch (Exception error) {
            System.out.println("Error: " + error.getMessage());
            return null;
        }
    }

    @Override
    public String encryptMessage(String character, RSAKeyDTO publicKeyDto) {
        BigInteger e = publicKeyDto.getE();
        BigInteger n = publicKeyDto.getN();
        return (new BigInteger(character.getBytes())).modPow(e, n).toString();
    }

    @Override
    public String decryptMessage(String character, RSAKeyDTO privateKeyDto) {
        BigInteger n = privateKeyDto.getN();
        BigInteger d = privateKeyDto.getD();
        return new String((new BigInteger(character)).modPow(d, n).toByteArray());
    }

    @Override
    public String handleDecryptMessage(String message, RSAKeyDTO privateKeyDto) {
        try {
            String encryptedNums[] = message.split(",");

            String messageDecrypted = "";
            for (String numString : encryptedNums) {
                messageDecrypted += decryptMessage(numString, privateKeyDto);
            }
            return messageDecrypted;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }


}
