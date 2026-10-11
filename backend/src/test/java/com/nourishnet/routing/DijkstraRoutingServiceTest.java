package com.nourishnet.routing;

import com.nourishnet.dto.RoutingResultDto;
import com.nourishnet.exception.InvalidRouteException;
import com.nourishnet.exception.RouteNotFoundException;
import com.nourishnet.model.RoadEdge;
import com.nourishnet.model.RoadNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DijkstraRoutingServiceTest {

    private WeightedGraph graph;
    private DijkstraRoutingService routingService;

    @BeforeEach
    void setUp() {
        graph = new WeightedGraph();
        routingService = new DijkstraRoutingService(graph);

        // Add 6 nodes
        graph.addNode(new RoadNode("A", "Node A", 13.0, 80.0, "Junction A"));
        graph.addNode(new RoadNode("B", "Node B", 13.1, 80.1, "Junction B"));
        graph.addNode(new RoadNode("C", "Node C", 13.2, 80.2, "Junction C"));
        graph.addNode(new RoadNode("D", "Node D", 13.3, 80.3, "Junction D"));
        graph.addNode(new RoadNode("E", "Node E", 13.4, 80.4, "Junction E"));
        graph.addNode(new RoadNode("F", "Isolated Node", 13.5, 80.5, "Isolated"));

        // Edges:
        // A -> B: 4.0
        // A -> C: 2.0
        // B -> C: 1.0
        // B -> D: 5.0
        // C -> D: 8.0
        // C -> E: 10.0
        // D -> E: 2.0
        graph.addEdge(new RoadEdge("A", "B", 4.0, true));
        graph.addEdge(new RoadEdge("A", "C", 2.0, true));
        graph.addEdge(new RoadEdge("B", "C", 1.0, true));
        graph.addEdge(new RoadEdge("B", "D", 5.0, true));
        graph.addEdge(new RoadEdge("C", "D", 8.0, true));
        graph.addEdge(new RoadEdge("C", "E", 10.0, true));
        graph.addEdge(new RoadEdge("D", "E", 2.0, true));
    }

    @Test
    @DisplayName("Dijkstra calculates correct shortest path and distance")
    void testCalculateShortestPath() {
        // Path from A to D:
        // Option 1: A -> B -> D = 4 + 5 = 9
        // Option 2: A -> C -> B -> D = 2 + 1 + 5 = 8
        // Option 3: A -> C -> D = 2 + 8 = 10
        // Expected: A -> C -> B -> D with total 8.0 km
        RoutingResultDto result = routingService.calculateShortestPath("A", "D");

        assertNotNull(result);
        assertEquals("A", result.getSource());
        assertEquals("D", result.getDestination());
        assertEquals(8.0, result.getTotalDistanceKm(), 0.001);
        assertEquals(List.of("A", "C", "B", "D"), result.getPath());
        assertEquals(4, result.getPathDetails().size());
    }

    @Test
    @DisplayName("Dijkstra calculates shortest path to distant node via intermediate min-heap hops")
    void testCalculateShortestPathToE() {
        // Path from A to E:
        // A -> C -> B -> D -> E = 2 + 1 + 5 + 2 = 10.0 km
        // vs A -> C -> E = 2 + 10 = 12.0 km
        RoutingResultDto result = routingService.calculateShortestPath("A", "E");

        assertNotNull(result);
        assertEquals(10.0, result.getTotalDistanceKm(), 0.001);
        assertEquals(List.of("A", "C", "B", "D", "E"), result.getPath());
        assertEquals(5, result.getPathDetails().size());
    }

    @Test
    @DisplayName("Source equals destination returns zero distance and single-node path")
    void testSameSourceAndDestination() {
        RoutingResultDto result = routingService.calculateShortestPath("B", "B");

        assertNotNull(result);
        assertEquals(0.0, result.getTotalDistanceKm(), 0.001);
        assertEquals(List.of("B"), result.getPath());
        assertEquals(1, result.getPathDetails().size());
    }

    @Test
    @DisplayName("Invalid or non-existent node throws InvalidRouteException")
    void testNonExistentNodeThrows() {
        assertThrows(InvalidRouteException.class, () -> routingService.calculateShortestPath("A", "UNKNOWN_NODE"));
        assertThrows(InvalidRouteException.class, () -> routingService.calculateShortestPath("", "B"));
        assertThrows(InvalidRouteException.class, () -> routingService.calculateShortestPath(null, "B"));
    }

    @Test
    @DisplayName("Unreachable disconnected node throws RouteNotFoundException")
    void testUnreachableNodeThrows() {
        assertThrows(RouteNotFoundException.class, () -> routingService.calculateShortestPath("A", "F"));
    }
}
