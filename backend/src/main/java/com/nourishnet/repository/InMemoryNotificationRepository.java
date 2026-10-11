package com.nourishnet.repository;

import com.nourishnet.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe implementation of NotificationRepository.
 */
@Repository
public class InMemoryNotificationRepository implements NotificationRepository {
    private final Map<String, Notification> storage = new ConcurrentHashMap<>();

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null || notification.getId().trim().isEmpty()) {
            notification.setId(UUID.randomUUID().toString());
        }
        storage.put(notification.getId(), notification);
        return notification;
    }

    @Override
    public Optional<Notification> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Notification> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Notification> findByRecipientUserId(String userId) {
        if (userId == null) return Collections.emptyList();
        return storage.values().stream()
                .filter(n -> "ALL".equalsIgnoreCase(n.getRecipientUserId()) || userId.equalsIgnoreCase(n.getRecipientUserId()))
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
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
