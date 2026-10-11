package com.nourishnet.model;

public class RoadEdge {
    private String fromNodeId;
    private String toNodeId;
    private double distanceKm;
    private boolean bidirectional;

    public RoadEdge() {}

    public RoadEdge(String fromNodeId, String toNodeId, double distanceKm, boolean bidirectional) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Road edge distance must be non-negative: " + distanceKm);
        }
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.distanceKm = distanceKm;
        this.bidirectional = bidirectional;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("Road edge distance must be non-negative: " + distanceKm);
        }
        this.distanceKm = distanceKm;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }
}
