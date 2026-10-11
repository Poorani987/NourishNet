package com.nourishnet.repository;

import com.nourishnet.model.Delivery;
import com.nourishnet.model.DeliveryStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe implementation of DeliveryRepository.
 */
@Repository
public class InMemoryDeliveryRepository implements DeliveryRepository {
    private final Map<String, Delivery> storage = new ConcurrentHashMap<>();

    @Override
    public Delivery save(Delivery delivery) {
        if (delivery.getId() == null || delivery.getId().trim().isEmpty()) {
            delivery.setId(UUID.randomUUID().toString());
        }
        storage.put(delivery.getId(), delivery);
        return delivery;
    }

    @Override
    public Optional<Delivery> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Delivery> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Delivery> findByVolunteerId(String volunteerId) {
        if (volunteerId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(d -> volunteerId.equals(d.getVolunteerId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Delivery> findByRecipientId(String recipientId) {
        if (recipientId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(d -> recipientId.equals(d.getRecipientId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Delivery> findByDonorId(String donorId) {
        if (donorId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(d -> donorId.equals(d.getDonorId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Delivery> findByStatus(DeliveryStatus status) {
        return storage.values().stream()
                .filter(d -> d.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Delivery> findPendingDeliveries() {
        return storage.values().stream()
                .filter(d -> d.getStatus() == DeliveryStatus.PENDING_ACCEPTANCE)
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
