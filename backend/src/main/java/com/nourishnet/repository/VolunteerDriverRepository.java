package com.nourishnet.repository;

import com.nourishnet.model.VolunteerDriver;
import java.util.List;
import java.util.Optional;

public interface VolunteerDriverRepository {
    VolunteerDriver save(VolunteerDriver driver);
    Optional<VolunteerDriver> findById(String id);
    Optional<VolunteerDriver> findByUserId(String userId);
    List<VolunteerDriver> findAll();
    List<VolunteerDriver> findAvailableDrivers();
    long count();
    void deleteById(String id);
}
