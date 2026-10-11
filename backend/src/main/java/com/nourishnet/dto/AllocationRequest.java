package com.nourishnet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class AllocationRequest {
    @NotBlank(message = "Food batch identifier is required")
    private String batchId;

    @Positive(message = "Claimed quantity must be strictly positive")
    private double quantity;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private Double recipientLatitude;
    private Double recipientLongitude;
    private String destinationNodeId;

    public AllocationRequest() {}

    public AllocationRequest(String batchId, double quantity, String deliveryAddress, 
                             Double recipientLatitude, Double recipientLongitude, String destinationNodeId) {
        this.batchId = batchId;
        this.quantity = quantity;
        this.deliveryAddress = deliveryAddress;
        this.recipientLatitude = recipientLatitude;
        this.recipientLongitude = recipientLongitude;
        this.destinationNodeId = destinationNodeId;
    }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public Double getRecipientLatitude() { return recipientLatitude; }
    public void setRecipientLatitude(Double recipientLatitude) { this.recipientLatitude = recipientLatitude; }

    public Double getRecipientLongitude() { return recipientLongitude; }
    public void setRecipientLongitude(Double recipientLongitude) { this.recipientLongitude = recipientLongitude; }

    public String getDestinationNodeId() { return destinationNodeId; }
    public void setDestinationNodeId(String destinationNodeId) { this.destinationNodeId = destinationNodeId; }
}
