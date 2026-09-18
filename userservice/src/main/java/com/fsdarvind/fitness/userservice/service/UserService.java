package com.fsdarvind.fitness.userservice.service;

import com.fsdarvind.fitness.userservice.dto.RegisterRequest;
import com.fsdarvind.fitness.userservice.dto.UserResponse;
import com.fsdarvind.fitness.userservice.model.User;
import com.fsdarvind.fitness.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findByEmail(userId).orElse(null);
        return user!=null?getUserResponse(user):null;
    }

    public UserResponse register(RegisterRequest request) {
        User checkIfUserAlreadyExist = userRepository.findByEmail(request.getEmail()).orElse(null);
        if(checkIfUserAlreadyExist!=null){
            throw new RuntimeException("Email already exist!");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        User createdUser = userRepository.save(user);
        return getUserResponse(createdUser);
    }

    public UserResponse getUserResponse(User createdUser) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(createdUser.getId());
        userResponse.setEmail(createdUser.getEmail());
        userResponse.setPassword(createdUser.getPassword());
        userResponse.setFirstName(createdUser.getFirstName());
        userResponse.setLastName(createdUser.getLastName());
        userResponse.setCreatedAt(createdUser.getCreatedAt());
        userResponse.setUpdatedAt(createdUser.getUpdatedAt());
        return userResponse;
    }

    public Boolean existsById(String userId) {
        log.info("Calling User Validation API for userId: {}", userId);
        return userRepository.existsById(userId);
    }
}
