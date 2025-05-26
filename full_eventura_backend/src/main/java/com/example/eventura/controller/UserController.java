package com.example.eventura.controller;

import com.example.eventura.dto.request.LoginRequest;
import com.example.eventura.dto.request.RegisterRequest;
import com.example.eventura.dto.request.UpdateUserRequest;
import com.example.eventura.dto.response.UserResponse;
import com.example.eventura.security.JwtTokenProvider;
import com.example.eventura.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    //User Register
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.CREATED);
    }

    //User Login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    //Set Status
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateAccountStatus(@PathVariable Long userId, @RequestBody String status) {
        return ResponseEntity.ok(userService.updateAccountStatus(userId, status));
    }

    // set Account Status to Delete
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<Void> deleteOwnAccount(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        userService.deleteOwnAccount(userId);
        return ResponseEntity.noContent().build();
    }

    //Update User Profile
    @PutMapping("/me")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER')")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UpdateUserRequest request,
                                                   @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    //Get Own User Profile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.getUserById(userId, userId));
    }

    //Get User By ID
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId,
                                                    @RequestHeader("Authorization") String authHeader) {
        Long requestingUserId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(userService.getUserById(userId, requestingUserId));
    }
    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromJWT(token);
    }
}