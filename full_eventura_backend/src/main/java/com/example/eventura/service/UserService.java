package com.example.eventura.service;

import com.example.eventura.dto.request.LoginRequest;
import com.example.eventura.dto.request.RegisterRequest;
import com.example.eventura.dto.request.UpdateUserRequest;
import com.example.eventura.dto.response.UserResponse;
import com.example.eventura.entity.User;
import com.example.eventura.exception.ResourceConflictException;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.exception.UnauthorizedException;
import com.example.eventura.repository.UserRepository;
import com.example.eventura.security.JwtTokenProvider;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new ResourceConflictException("Email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setAccountStatus(User.AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        // Send welcome email using Thymeleaf template
        String subject = "Welcome to Eventura!";
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), subject, savedUser.getFirstName(), savedUser.getLastName());
        } catch (MessagingException e) {
            logger.error("Failed to send welcome email to {}: {}", savedUser.getEmail(), e.getMessage());
            // Continue registration despite email failure
        }

        return convertToResponse(savedUser);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return jwtTokenProvider.generateToken(user);
    }

    public UserResponse updateAccountStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate and normalize status input
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        // Strip JSON quotes if present (e.g., "\"SUSPENDED\"" -> "SUSPENDED")
        String cleanedStatus = status.trim();
        if (cleanedStatus.startsWith("\"") && cleanedStatus.endsWith("\"")) {
            cleanedStatus = cleanedStatus.substring(1, cleanedStatus.length() - 1).trim();
        }

        // Normalize to uppercase
        String normalizedStatus = cleanedStatus.toUpperCase();

        // Validate status
        try {
            User.AccountStatus newStatus = User.AccountStatus.valueOf(normalizedStatus);
            // Optional: Enforce state transition rules (uncomment if desired)

            if (user.getAccountStatus() == User.AccountStatus.DELETED) {
                throw new IllegalStateException("Cannot change status of a DELETED account");
            }
            user.setAccountStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Must be one of: ACTIVE, SUSPENDED, DELETED");
        }

        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    public void deleteOwnAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setAccountStatus(User.AccountStatus.DELETED);
        userRepository.save(user);
    }
    //only change the status
//    public void deleteUserAccountByAdmin(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        user.setAccountStatus(User.AccountStatus.DELETED);
//        userRepository.save(user);
//    }
    public void deleteUserAccountByAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Delete the user completely from the database
        userRepository.delete(user);
    }

    public UserResponse getUserById(Long userId, Long requestingUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Authorization check: Only the user themselves or an admin can access
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Requesting user not found"));

//        if (!userId.equals(requestingUserId) && !requestingUser.getRole().equals(User.Role.ADMIN)) {
//            throw new UnauthorizedException("Not authorized to access this user's information");
//        }

        return convertToResponse(user);
    }

    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()) != null) {
                throw new ResourceConflictException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        // Validate mobile number uniqueness if changed
        if (request.getMobileNumber() != null && !request.getMobileNumber().equals(user.getMobileNumber())) {
            if (userRepository.findByMobileNumber(request.getMobileNumber()) != null) {
                throw new ResourceConflictException("Mobile number already exists");
            }
            user.setMobileNumber(request.getMobileNumber());
        }

        // Update other fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);

        // Optionally send email notification for profile update
        try {
            emailService.sendProfileUpdateEmail(updatedUser.getEmail(), "Profile Updated",
                    updatedUser.getFirstName(), updatedUser.getLastName());
        } catch (MessagingException e) {
            logger.error("Failed to send profile update email to {}: {}", updatedUser.getEmail(), e.getMessage());
        }

        return convertToResponse(updatedUser);
    }


    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }



    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setMobileNumber(user.getMobileNumber());
        response.setRole(user.getRole().name());
        response.setAccountStatus(user.getAccountStatus().name());
        return response;
    }
}