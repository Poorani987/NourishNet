package com.nourishnet.repository;

import com.nourishnet.model.FoodWasteRecord;
import java.util.List;
import java.util.Optional;

public interface FoodWasteRepository {
    FoodWasteRecord save(FoodWasteRecord record);
    Optional<FoodWasteRecord> findById(String id);
    List<FoodWasteRecord> findAll();
    long count();
    double getTotalWasteQuantity();
    void deleteById(String id);
}
