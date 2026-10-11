package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.dto.FoodWasteCreateRequest;
import com.nourishnet.model.FoodWasteRecord;
import com.nourishnet.model.User;
import com.nourishnet.service.AuthService;
import com.nourishnet.service.FoodWasteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/waste")
public class FoodWasteController {
    private final FoodWasteService foodWasteService;
    private final AuthService authService;

    public FoodWasteController(FoodWasteService foodWasteService, AuthService authService) {
        this.foodWasteService = foodWasteService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FoodWasteRecord>> recordWaste(
            @Valid @RequestBody FoodWasteCreateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireUser(authHeader);
        FoodWasteRecord record = foodWasteService.recordWaste(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Food waste logged for safe diversion and recovery", record));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodWasteRecord>>> getAllWasteRecords() {
        List<FoodWasteRecord> list = foodWasteService.getAllWasteRecords();
        return ResponseEntity.ok(ApiResponse.ok("Food waste logs retrieved", list));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWasteStats() {
        List<FoodWasteRecord> list = foodWasteService.getAllWasteRecords();
        double totalKg = foodWasteService.getTotalWasteQuantity();

        Map<String, Double> byCategory = new HashMap<>();
        for (FoodWasteRecord r : list) {
            String cat = r.getDisposalCategory() != null ? r.getDisposalCategory().name() : "OTHER";
            byCategory.put(cat, byCategory.getOrDefault(cat, 0.0) + r.getQuantity());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalKg", Math.round(totalKg * 100.0) / 100.0);
        stats.put("count", list.size());
        stats.put("categoryBreakdown", byCategory);

        return ResponseEntity.ok(ApiResponse.ok("Waste statistics retrieved", stats));
    }
}
