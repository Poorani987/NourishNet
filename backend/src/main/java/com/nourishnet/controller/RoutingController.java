package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.dto.RoutingResultDto;
import com.nourishnet.model.RoadEdge;
import com.nourishnet.model.RoadNode;
import com.nourishnet.routing.DijkstraRoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping({"/api/routing", "/api/routes"})
public class RoutingController {
    private final DijkstraRoutingService routingService;

    public RoutingController(DijkstraRoutingService routingService) {
        this.routingService = routingService;
    }

    @GetMapping("/nodes")
    public ResponseEntity<ApiResponse<Collection<RoadNode>>> getAllNodes() {
        return ResponseEntity.ok(ApiResponse.ok("Road network nodes retrieved", routingService.getGraph().getNodes().values()));
    }

    @GetMapping("/edges")
    public ResponseEntity<ApiResponse<List<RoadEdge>>> getAllEdges() {
        return ResponseEntity.ok(ApiResponse.ok("Road network edges retrieved", routingService.getGraph().getAllEdges()));
    }

    @GetMapping({"/calculate", "/shortest-path"})
    public ResponseEntity<ApiResponse<RoutingResultDto>> calculateShortestPath(
            @RequestParam String source,
            @RequestParam String destination) {
        RoutingResultDto result = routingService.calculateShortestPath(source, destination);
        return ResponseEntity.ok(ApiResponse.ok("Dijkstra shortest path calculated", result));
    }
}
