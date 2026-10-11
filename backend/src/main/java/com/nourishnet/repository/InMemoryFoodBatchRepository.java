package com.nourishnet.repository;

import com.nourishnet.model.BatchStatus;
import com.nourishnet.model.FoodBatch;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe implementation of FoodBatchRepository.
 */
@Repository
public class InMemoryFoodBatchRepository implements FoodBatchRepository {
    private final Map<String, FoodBatch> storage = new ConcurrentHashMap<>();

    @Override
    public FoodBatch save(FoodBatch batch) {
        if (batch.getId() == null || batch.getId().trim().isEmpty()) {
            batch.setId(UUID.randomUUID().toString());
        }
        storage.put(batch.getId(), batch);
        return batch;
    }

    @Override
    public Optional<FoodBatch> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<FoodBatch> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<FoodBatch> findByDonorId(String donorId) {
        if (donorId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(b -> donorId.equals(b.getDonorId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FoodBatch> findByStatus(BatchStatus status) {
        return storage.values().stream()
                .filter(b -> b.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<FoodBatch> findAvailableBatches() {
        LocalDateTime now = LocalDateTime.now();
        return storage.values().stream()
                .filter(b -> (b.getStatus() == BatchStatus.AVAILABLE || b.getStatus() == BatchStatus.PARTIALLY_ALLOCATED))
                .filter(b -> b.getRemainingQuantity() > 0)
                .filter(b -> b.getConsumeBefore() == null || b.getConsumeBefore().isAfter(now))
                .sorted(Comparator.comparing(FoodBatch::getConsumeBefore, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public void deleteById(String id) {
        if (id != null) {
            storage.remove(id);
        }
    }
}
