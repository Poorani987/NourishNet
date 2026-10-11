package com.nourishnet.repository;

import com.nourishnet.model.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(String id);
    List<Notification> findAll();
    List<Notification> findByRecipientUserId(String userId);
    long count();
    void deleteById(String id);
}
