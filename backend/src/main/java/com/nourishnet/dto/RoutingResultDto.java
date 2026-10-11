package com.nourishnet.dto;

import com.nourishnet.model.RoadNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoutingResultDto {
    private String source;
    private String destination;
    private List<String> path = new ArrayList<>();
    private List<RoadNode> pathDetails = new ArrayList<>();
    private double totalDistanceKm;
    private String dataType = "SAMPLE_ROAD_NETWORK";
    private LocalDateTime calculatedAt = LocalDateTime.now();

    public RoutingResultDto() {}

    public RoutingResultDto(String source, String destination, List<String> path, 
                            List<RoadNode> pathDetails, double totalDistanceKm) {
        this.source = source;
        this.destination = destination;
        this.path = path;
        this.pathDetails = pathDetails;
        this.totalDistanceKm = totalDistanceKm;
        this.dataType = "SAMPLE_ROAD_NETWORK";
        this.calculatedAt = LocalDateTime.now();
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public List<String> getPath() { return path; }
    public void setPath(List<String> path) { this.path = path; }

    public List<RoadNode> getPathDetails() { return pathDetails; }
    public void setPathDetails(List<RoadNode> pathDetails) { this.pathDetails = pathDetails; }

    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
