package com.nourishnet.repository;

import com.nourishnet.model.VolunteerDriver;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe implementation of VolunteerDriverRepository.
 */
@Repository
public class InMemoryVolunteerDriverRepository implements VolunteerDriverRepository {
    private final Map<String, VolunteerDriver> storage = new ConcurrentHashMap<>();

    @Override
    public VolunteerDriver save(VolunteerDriver driver) {
        if (driver.getId() == null || driver.getId().trim().isEmpty()) {
            driver.setId(UUID.randomUUID().toString());
        }
        storage.put(driver.getId(), driver);
        return driver;
    }

    @Override
    public Optional<VolunteerDriver> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<VolunteerDriver> findByUserId(String userId) {
        if (userId == null) return Optional.empty();
        return storage.values().stream()
                .filter(d -> userId.equals(d.getUserId()))
                .findFirst();
    }

    @Override
    public List<VolunteerDriver> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<VolunteerDriver> findAvailableDrivers() {
        return storage.values().stream()
                .filter(VolunteerDriver::isAvailable)
                .filter(d -> d.getActiveDeliveryId() == null || d.getActiveDeliveryId().isEmpty())
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
