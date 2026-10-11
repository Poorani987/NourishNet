package com.nourishnet.repository;

import com.nourishnet.model.DemandRequest;
import com.nourishnet.model.DemandStatus;
import java.util.List;
import java.util.Optional;

public interface DemandRepository {
    DemandRequest save(DemandRequest request);
    Optional<DemandRequest> findById(String id);
    List<DemandRequest> findAll();
    List<DemandRequest> findByRecipientId(String recipientId);
    List<DemandRequest> findByStatus(DemandStatus status);
    long count();
    void deleteById(String id);
}
