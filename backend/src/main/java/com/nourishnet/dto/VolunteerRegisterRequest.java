package com.nourishnet.dto;

import com.nourishnet.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class VolunteerRegisterRequest {
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Vehicle registration identifier is required")
    private String registrationNumber;

    @Positive(message = "Vehicle capacity must be positive")
    private double capacityKg;

    private String notes;

    @NotBlank(message = "Current or operating location is required")
    private String currentOperatingLocation;

    private Double latitude;
    private Double longitude;

    @Positive(message = "Operating radius must be positive")
    private double operatingRadiusKm = 10.0;

    private String preferredAreas;

    public VolunteerRegisterRequest() {}

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public double getCapacityKg() { return capacityKg; }
    public void setCapacityKg(double capacityKg) { this.capacityKg = capacityKg; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCurrentOperatingLocation() { return currentOperatingLocation; }
    public void setCurrentOperatingLocation(String currentOperatingLocation) { this.currentOperatingLocation = currentOperatingLocation; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public double getOperatingRadiusKm() { return operatingRadiusKm; }
    public void setOperatingRadiusKm(double operatingRadiusKm) { this.operatingRadiusKm = operatingRadiusKm; }

    public String getPreferredAreas() { return preferredAreas; }
    public void setPreferredAreas(String preferredAreas) { this.preferredAreas = preferredAreas; }
}
