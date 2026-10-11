package com.nourishnet.model;

public class Vehicle {
    private VehicleType type;
    private String registrationNumber;
    private double capacityKg;
    private String notes;

    public Vehicle() {}

    public Vehicle(VehicleType type, String registrationNumber, double capacityKg, String notes) {
        this.type = type;
        this.registrationNumber = registrationNumber;
        this.capacityKg = capacityKg;
        this.notes = notes;
    }

    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public double getCapacityKg() { return capacityKg; }
    public void setCapacityKg(double capacityKg) { this.capacityKg = capacityKg; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
