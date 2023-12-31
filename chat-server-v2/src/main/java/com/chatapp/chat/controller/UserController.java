package com.chatapp.chat.controller;

import com.chatapp.chat.entity.User;
import com.chatapp.chat.model.*;
import com.chatapp.chat.service.UserService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping(value = "/currentLoginUser")
    public ResponseEntity<UserDTO> getCurrentLoginUser() {
        UserDTO currentUser = userService.getCurrentLoginUser();
        if (currentUser != null)
            return new ResponseEntity<UserDTO>(currentUser, HttpStatus.OK);
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        UserDTO user = userService.getUserById(userId);
        if (user != null)
            return new ResponseEntity<UserDTO>(user, HttpStatus.OK);
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/friends")
    public ResponseEntity<Set<UserDTO>> getAllFriends() {
        Set<UserDTO> friends = userService.getAllFiends();
        if (friends == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Set<UserDTO>>(friends, HttpStatus.OK);
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<Set<UserDTO>> getAllFriendsOfUser(@PathVariable UUID userId){
        Set<UserDTO> friends = userService.getFriendsOfUser(userId);
        if (friends == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Set<UserDTO>>(friends, HttpStatus.OK);
    }

    @GetMapping("/joinedRoom")
    public ResponseEntity<List<RoomDTO>> getAllJoinedRooms() {
        List<RoomDTO> rooms = userService.getAllJoinedRooms();
        if (rooms == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<List<RoomDTO>>(rooms, HttpStatus.OK);
    }

    @GetMapping("/privateRooms")
    public ResponseEntity<List<RoomDTO>> getAllPrivateRooms() {
        List<RoomDTO> rooms = userService.getAllPrivateRooms();
        if (rooms == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<List<RoomDTO>>(rooms, HttpStatus.OK);
    }

    @GetMapping("/publicRooms")
    public ResponseEntity<List<RoomDTO>> getAllPublicRooms() {
        List<RoomDTO> rooms = userService.getAllPublicRooms();
        if (rooms == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<List<RoomDTO>>(rooms, HttpStatus.OK);
    }

    @PostMapping("/search")
    public ResponseEntity<Set<UserDTO>> searchUsers(@RequestBody SeachObject seachObject) {
        Set<UserDTO> users = userService.searchUsers(seachObject.getKeyword());
        if (users == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Set<UserDTO>>(users, HttpStatus.OK);
    }

    @PostMapping("/searchFriend")
    public ResponseEntity<Set<UserDTO>> searchFriend(@RequestBody SeachObject seachObject) {
        Set<UserDTO> users = userService.searchFriend(seachObject.getKeyword());
        if (users == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Set<UserDTO>>(users, HttpStatus.OK);
    }

    @PostMapping("/searchExcludeSelf")
    public ResponseEntity<Set<UserDTO>> searchUsersExcludeSelf(@RequestBody SeachObject seachObject) {
        Set<UserDTO> users = userService.searchUsersExcludeSelf(seachObject.getKeyword());
        if (users == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Set<UserDTO>>(users, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<Set<UserDTO>> getAllUsers() {
        Set<UserDTO> rooms = userService.getAllUsers();
        if (rooms == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Set<UserDTO>>(rooms, HttpStatus.OK);
    }

    @GetMapping("/addFriendRequests")
    public ResponseEntity<Set<FriendDTO>> getAddFriendRequests() {
        Set<FriendDTO> res = userService.getAddFriendRequests();
        if (res == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Set<FriendDTO>>(res, HttpStatus.OK);
    }

    @GetMapping("/pendingFriendRequests")
    public ResponseEntity<Set<FriendDTO>> getPendingFriendRequests() {
        Set<FriendDTO> res = userService.getPendingFriendRequests();
        if (res == null) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<Set<FriendDTO>>(res, HttpStatus.OK);
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("fileUpload") MultipartFile fileUpload) {
        String res = userService.uploadAvatar(fileUpload);
        if (res != null) return new ResponseEntity<String>(res, HttpStatus.OK);
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/avatar/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = userService.getAvatarByName(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PutMapping("/info")
    public ResponseEntity<UserDTO> updateUserInfo(@RequestBody UserDTO dto) {
        UserDTO res = userService.updateUserInfo(dto);
        if (res != null) return new ResponseEntity<UserDTO>(res, HttpStatus.OK);
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/notification/latest20")
    public ResponseEntity<List<MessageDTO>> getTop20LatestNotifications() {
        List<MessageDTO> res = userService.getTop20LatestNotifications();
        if (res != null) return new ResponseEntity<List<MessageDTO>>(res, HttpStatus.OK);
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/notification/all")
    public ResponseEntity<List<MessageDTO>> getAllNotifications() {
        List<MessageDTO> res = userService.getAllNotifications();
        if (res != null) return new ResponseEntity<List<MessageDTO>>(res, HttpStatus.OK);
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/publicKey")
    public ResponseEntity<RSAKeyDTO> updateUserPublicKey(@RequestBody RSAKeyDTO publicKeyDto) {
        RSAKeyDTO res = userService.updateUserPublicKey(publicKeyDto);
        if (res != null) return new ResponseEntity<RSAKeyDTO>(res, HttpStatus.OK);
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
}
