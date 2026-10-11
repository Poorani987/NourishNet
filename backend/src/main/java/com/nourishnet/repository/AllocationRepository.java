package com.nourishnet.repository;

import com.nourishnet.model.Allocation;
import java.util.List;
import java.util.Optional;

public interface AllocationRepository {
    Allocation save(Allocation allocation);
    Optional<Allocation> findById(String id);
    List<Allocation> findAll();
    List<Allocation> findByRecipientId(String recipientId);
    List<Allocation> findByDonorId(String donorId);
    List<Allocation> findByBatchId(String batchId);
    long count();
    void deleteById(String id);
}
