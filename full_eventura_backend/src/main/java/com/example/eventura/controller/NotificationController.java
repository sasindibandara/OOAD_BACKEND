package com.example.eventura.controller;

import com.example.eventura.dto.response.NotificationResponse;
import com.example.eventura.security.JwtTokenProvider;
import com.example.eventura.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    //Get Own Notifications
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestHeader("Authorization") String token,
            Pageable pageable,
            @RequestParam(required = false) Boolean isRead) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        Page<NotificationResponse> notifications = notificationService.getNotificationsByUser(email, pageable, isRead);
        return ResponseEntity.ok(notifications);
    }


    //Set Notification as Read By Notification ID
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader("Authorization") String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
        NotificationResponse response = notificationService.markAsRead(notificationId, email);
        return ResponseEntity.ok(response);
    }
}