package com.chatapp.chat.service;

import com.chatapp.chat.entity.User;
import com.chatapp.chat.model.FriendDTO;
import com.chatapp.chat.model.MessageDTO;
import com.chatapp.chat.model.RoomDTO;
import com.chatapp.chat.model.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public interface UserService {
    public UserDTO getCurrentLoginUser();

    public UserDTO getUserById(UUID userId);

    public UserDTO getUserByName(String userName);

    public Set<UserDTO> getAllFiends();

    public Set<FriendDTO> getAddFriendRequests();

    public Set<FriendDTO> getPendingFriendRequests();

    public List<RoomDTO> getAllJoinedRooms();

    public List<RoomDTO> getAllPrivateRooms();

    public List<RoomDTO> getAllPublicRooms();

    public User getCurrentLoginUserEntity();

    public User getUserEntityById(UUID userId);

    public Set<UserDTO> searchUsers(String searchString);

    public Set<UserDTO> getAllUsers();

    public Set<UserDTO> searchUsersExcludeSelf(String searchString);

    public UserDTO updateUserInfo(UserDTO dto);

    public String uploadAvatar(MultipartFile fileUpload);

    public List<MessageDTO> getTop20LatestNotifications();

    public List<MessageDTO> getAllNotifications();
}
