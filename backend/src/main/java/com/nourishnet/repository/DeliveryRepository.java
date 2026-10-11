package com.nourishnet.repository;

import com.nourishnet.model.Delivery;
import com.nourishnet.model.DeliveryStatus;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository {
    Delivery save(Delivery delivery);
    Optional<Delivery> findById(String id);
    List<Delivery> findAll();
    List<Delivery> findByVolunteerId(String volunteerId);
    List<Delivery> findByRecipientId(String recipientId);
    List<Delivery> findByDonorId(String donorId);
    List<Delivery> findByStatus(DeliveryStatus status);
    List<Delivery> findPendingDeliveries();
    long count();
    void deleteById(String id);
}
