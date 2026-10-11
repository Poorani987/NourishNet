package com.nourishnet.service;

import com.nourishnet.dto.FoodWasteCreateRequest;
import com.nourishnet.model.FoodWasteRecord;
import com.nourishnet.model.User;
import com.nourishnet.repository.FoodWasteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FoodWasteService {
    private final FoodWasteRepository foodWasteRepository;

    public FoodWasteService(FoodWasteRepository foodWasteRepository) {
        this.foodWasteRepository = foodWasteRepository;
    }

    public FoodWasteRecord recordWaste(FoodWasteCreateRequest request, User user) {
        FoodWasteRecord record = new FoodWasteRecord(
                UUID.randomUUID().toString(),
                request.getBatchId() != null ? request.getBatchId() : "DIRECT_MANUAL_LOG",
                request.getFoodName().trim(),
                request.getQuantity(),
                request.getUnit().trim(),
                request.getReason().trim(),
                request.getDisposalCategory()
        );
        return foodWasteRepository.save(record);
    }

    public List<FoodWasteRecord> getAllWasteRecords() {
        return foodWasteRepository.findAll();
    }

    public double getTotalWasteQuantity() {
        return foodWasteRepository.getTotalWasteQuantity();
    }
}
