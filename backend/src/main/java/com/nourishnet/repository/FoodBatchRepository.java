package com.nourishnet.repository;

import com.nourishnet.model.BatchStatus;
import com.nourishnet.model.FoodBatch;
import java.util.List;
import java.util.Optional;

public interface FoodBatchRepository {
    FoodBatch save(FoodBatch batch);
    Optional<FoodBatch> findById(String id);
    List<FoodBatch> findAll();
    List<FoodBatch> findByDonorId(String donorId);
    List<FoodBatch> findByStatus(BatchStatus status);
    List<FoodBatch> findAvailableBatches();
    long count();
    void deleteById(String id);
}
