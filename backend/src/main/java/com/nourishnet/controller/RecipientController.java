package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipients")
public class RecipientController {
    private final UserRepository userRepository;

    public RecipientController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getRecipients() {
        List<User> recipients = userRepository.findByRole(UserRole.RECIPIENT);
        return ResponseEntity.ok(ApiResponse.ok("Recipients retrieved", recipients));
    }
}
