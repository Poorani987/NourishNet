package com.nourishnet.repository;

import com.nourishnet.model.Allocation;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe implementation of AllocationRepository.
 */
@Repository
public class InMemoryAllocationRepository implements AllocationRepository {
    private final Map<String, Allocation> storage = new ConcurrentHashMap<>();

    @Override
    public Allocation save(Allocation allocation) {
        if (allocation.getId() == null || allocation.getId().trim().isEmpty()) {
            allocation.setId(UUID.randomUUID().toString());
        }
        storage.put(allocation.getId(), allocation);
        return allocation;
    }

    @Override
    public Optional<Allocation> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Allocation> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Allocation> findByRecipientId(String recipientId) {
        if (recipientId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(a -> recipientId.equals(a.getRecipientId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Allocation> findByDonorId(String donorId) {
        if (donorId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(a -> donorId.equals(a.getDonorId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Allocation> findByBatchId(String batchId) {
        if (batchId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(a -> batchId.equals(a.getBatchId()))
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
