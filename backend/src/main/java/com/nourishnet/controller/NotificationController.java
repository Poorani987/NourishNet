package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.model.Notification;
import com.nourishnet.model.User;
import com.nourishnet.repository.NotificationRepository;
import com.nourishnet.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    public NotificationController(NotificationRepository notificationRepository, AuthService authService) {
        this.notificationRepository = notificationRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && !authHeader.isEmpty()) {
            User user = authService.requireUser(authHeader);
            List<Notification> userSpecific = notificationRepository.findByRecipientUserId(user.getId());
            List<Notification> roleSpecific = notificationRepository.findByRecipientUserId(user.getRole().name());
            List<Notification> global = notificationRepository.findByRecipientUserId("ALL");

            List<Notification> combined = new ArrayList<>();
            combined.addAll(userSpecific);
            combined.addAll(roleSpecific);
            combined.addAll(global);
            return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved", combined));
        }

        List<Notification> all = notificationRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved", all));
    }
}
