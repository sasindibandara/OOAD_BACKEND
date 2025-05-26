package com.example.eventura.service;

import com.example.eventura.dto.response.NotificationResponse;
import com.example.eventura.entity.Notification;
import com.example.eventura.entity.User;
import com.example.eventura.exception.ResourceNotFoundException;
import com.example.eventura.exception.UnauthorizedException;
import com.example.eventura.repository.NotificationRepository;
import com.example.eventura.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationResponse createNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setIsRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        return convertToResponse(savedNotification);
    }

    public Page<NotificationResponse> getNotificationsByUser(String email, Pageable pageable, Boolean isRead) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Page<Notification> notifications;
        if (isRead != null) {
            notifications = notificationRepository.findByUserAndIsRead(user, isRead, pageable);
        } else {
            notifications = notificationRepository.findByUser(user, pageable);
        }

        return notifications.map(this::convertToResponse);
    }

    public NotificationResponse markAsRead(Long notificationId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not authorized to modify this notification");
        }

        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);

        return convertToResponse(updatedNotification);
    }

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        User user;
        if (userId == 0L) {
            user = null;
        } else {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        Page<Notification> notifications = notificationRepository.findAll(pageable);
        return notifications.map(this::convertToResponse);
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUser().getId());
        response.setMessage(notification.getMessage());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}