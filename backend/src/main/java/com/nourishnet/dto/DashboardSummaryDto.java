package com.nourishnet.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardSummaryDto {
    private long totalDonors;
    private long totalRecipients;
    private long registeredVolunteers;
    private long availableFoodBatches;
    private long allocatedDonations;
    private long completedDeliveries;
    private long expiredBatches;
    private long wasteRecordsCount;
    private double totalFoodDonatedKg;
    private double totalFoodRedistributedKg;
    private double totalFoodWasteKg;
    private List<Map<String, Object>> recentActivities = new ArrayList<>();
    private String dataSource = "IN_MEMORY_COLLECTIONS_DEMO";
    private LocalDateTime generatedAt = LocalDateTime.now();

    public DashboardSummaryDto() {}

    public long getTotalDonors() { return totalDonors; }
    public void setTotalDonors(long totalDonors) { this.totalDonors = totalDonors; }

    public long getTotalRecipients() { return totalRecipients; }
    public void setTotalRecipients(long totalRecipients) { this.totalRecipients = totalRecipients; }

    public long getRegisteredVolunteers() { return registeredVolunteers; }
    public void setRegisteredVolunteers(long registeredVolunteers) { this.registeredVolunteers = registeredVolunteers; }

    public long getAvailableFoodBatches() { return availableFoodBatches; }
    public void setAvailableFoodBatches(long availableFoodBatches) { this.availableFoodBatches = availableFoodBatches; }

    public long getAllocatedDonations() { return allocatedDonations; }
    public void setAllocatedDonations(long allocatedDonations) { this.allocatedDonations = allocatedDonations; }

    public long getCompletedDeliveries() { return completedDeliveries; }
    public void setCompletedDeliveries(long completedDeliveries) { this.completedDeliveries = completedDeliveries; }

    public long getExpiredBatches() { return expiredBatches; }
    public void setExpiredBatches(long expiredBatches) { this.expiredBatches = expiredBatches; }

    public long getWasteRecordsCount() { return wasteRecordsCount; }
    public void setWasteRecordsCount(long wasteRecordsCount) { this.wasteRecordsCount = wasteRecordsCount; }

    public double getTotalFoodDonatedKg() { return totalFoodDonatedKg; }
    public void setTotalFoodDonatedKg(double totalFoodDonatedKg) { this.totalFoodDonatedKg = totalFoodDonatedKg; }

    public double getTotalFoodRedistributedKg() { return totalFoodRedistributedKg; }
    public void setTotalFoodRedistributedKg(double totalFoodRedistributedKg) { this.totalFoodRedistributedKg = totalFoodRedistributedKg; }

    public double getTotalFoodWasteKg() { return totalFoodWasteKg; }
    public void setTotalFoodWasteKg(double totalFoodWasteKg) { this.totalFoodWasteKg = totalFoodWasteKg; }

    public List<Map<String, Object>> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<Map<String, Object>> recentActivities) { this.recentActivities = recentActivities; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
