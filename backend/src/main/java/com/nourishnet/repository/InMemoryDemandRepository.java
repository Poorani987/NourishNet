package com.nourishnet.repository;

import com.nourishnet.model.DemandRequest;
import com.nourishnet.model.DemandStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe implementation of DemandRepository.
 */
@Repository
public class InMemoryDemandRepository implements DemandRepository {
    private final Map<String, DemandRequest> storage = new ConcurrentHashMap<>();

    @Override
    public DemandRequest save(DemandRequest request) {
        if (request.getId() == null || request.getId().trim().isEmpty()) {
            request.setId(UUID.randomUUID().toString());
        }
        storage.put(request.getId(), request);
        return request;
    }

    @Override
    public Optional<DemandRequest> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<DemandRequest> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<DemandRequest> findByRecipientId(String recipientId) {
        if (recipientId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(d -> recipientId.equals(d.getRecipientId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandRequest> findByStatus(DemandStatus status) {
        return storage.values().stream()
                .filter(d -> d.getStatus() == status)
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
