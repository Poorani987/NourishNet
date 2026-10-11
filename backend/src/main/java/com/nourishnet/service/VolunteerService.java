package com.nourishnet.service;

import com.nourishnet.dto.VolunteerRegisterRequest;
import com.nourishnet.exception.ResourceNotFoundException;
import com.nourishnet.exception.UnauthorizedException;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.model.Vehicle;
import com.nourishnet.model.VolunteerDriver;
import com.nourishnet.repository.VolunteerDriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VolunteerService {
    private final VolunteerDriverRepository volunteerDriverRepository;

    public VolunteerService(VolunteerDriverRepository volunteerDriverRepository) {
        this.volunteerDriverRepository = volunteerDriverRepository;
    }

    public VolunteerDriver registerOrUpdateVehicle(VolunteerRegisterRequest request, User user) {
        if (user.getRole() != UserRole.VOLUNTEER && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only volunteers can register delivery vehicles");
        }

        Vehicle vehicle = new Vehicle(
                request.getVehicleType(),
                request.getRegistrationNumber().trim().toUpperCase(),
                request.getCapacityKg(),
                request.getNotes()
        );

        Optional<VolunteerDriver> existing = volunteerDriverRepository.findByUserId(user.getId());
        VolunteerDriver driver;

        if (existing.isPresent()) {
            driver = existing.get();
            driver.setVehicle(vehicle);
            driver.setCurrentOperatingLocation(request.getCurrentOperatingLocation());
            if (request.getLatitude() != null) driver.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) driver.setLongitude(request.getLongitude());
            driver.setOperatingRadiusKm(request.getOperatingRadiusKm());
            driver.setPreferredAreas(request.getPreferredAreas());
        } else {
            driver = new VolunteerDriver(
                    UUID.randomUUID().toString(),
                    user.getId(),
                    user.getName(),
                    user.getPhone(),
                    vehicle,
                    true,
                    request.getCurrentOperatingLocation(),
                    request.getLatitude() != null ? request.getLatitude() : user.getLatitude(),
                    request.getLongitude() != null ? request.getLongitude() : user.getLongitude(),
                    request.getOperatingRadiusKm(),
                    request.getPreferredAreas()
            );
        }

        return volunteerDriverRepository.save(driver);
    }

    public VolunteerDriver setAvailability(boolean available, User user) {
        VolunteerDriver driver = volunteerDriverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer driver profile not found for user: " + user.getName()));

        driver.setAvailable(available);
        return volunteerDriverRepository.save(driver);
    }

    public VolunteerDriver getDriverByUserId(String userId) {
        return volunteerDriverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found for user id: " + userId));
    }

    public List<VolunteerDriver> getAllDrivers() {
        return volunteerDriverRepository.findAll();
    }

    public List<VolunteerDriver> getAvailableDrivers() {
        return volunteerDriverRepository.findAvailableDrivers();
    }
}
