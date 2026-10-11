package com.nourishnet.repository;

import com.nourishnet.model.FoodWasteRecord;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe implementation of FoodWasteRepository.
 */
@Repository
public class InMemoryFoodWasteRepository implements FoodWasteRepository {
    private final Map<String, FoodWasteRecord> storage = new ConcurrentHashMap<>();

    @Override
    public FoodWasteRecord save(FoodWasteRecord record) {
        if (record.getId() == null || record.getId().trim().isEmpty()) {
            record.setId(UUID.randomUUID().toString());
        }
        storage.put(record.getId(), record);
        return record;
    }

    @Override
    public Optional<FoodWasteRecord> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<FoodWasteRecord> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public double getTotalWasteQuantity() {
        return storage.values().stream()
                .mapToDouble(FoodWasteRecord::getQuantity)
                .sum();
    }

    @Override
    public void deleteById(String id) {
        if (id != null) {
            storage.remove(id);
        }
    }
}
