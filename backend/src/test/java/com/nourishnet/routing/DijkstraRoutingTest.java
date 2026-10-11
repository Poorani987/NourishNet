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

@DisplayName("Dijkstra Shortest-Path Routing Algorithm Tests")
class DijkstraRoutingTest {

    private WeightedGraph testGraph;
    private DijkstraRoutingService routingService;

    @BeforeEach
    void setUp() {
        testGraph = new WeightedGraph();

        // Build a known reference graph:
        // A -(2.0)- B -(3.0)- D
        // A -(4.0)- C -(1.0)- D
        // E (isolated node with no edges)
        testGraph.addNode(new RoadNode("A", "Node Alpha", 13.0, 80.0, "Start"));
        testGraph.addNode(new RoadNode("B", "Node Bravo", 13.1, 80.1, "Junction"));
        testGraph.addNode(new RoadNode("C", "Node Charlie", 13.2, 80.2, "Junction"));
        testGraph.addNode(new RoadNode("D", "Node Delta", 13.3, 80.3, "Destination"));
        testGraph.addNode(new RoadNode("E", "Node Echo (Isolated)", 13.9, 80.9, "Isolated Island"));

        testGraph.addEdge(new RoadEdge("A", "B", 2.0, true));
        testGraph.addEdge(new RoadEdge("B", "D", 3.0, true));
        testGraph.addEdge(new RoadEdge("A", "C", 4.0, true));
        testGraph.addEdge(new RoadEdge("C", "D", 1.0, true));

        routingService = new DijkstraRoutingService(testGraph);
    }

    @Test
    @DisplayName("Shortest path should find optimal route over multiple alternatives")
    void testShortestPathCalculation() {
        // Path A -> B -> D distance is 2.0 + 3.0 = 5.0
        // Path A -> C -> D distance is 4.0 + 1.0 = 5.0 (tie)
        // Now if we change A-B to 1.5, path A -> B -> D should be 4.5
        testGraph.clear();
        testGraph.addNode(new RoadNode("A", "Alpha", 13.0, 80.0, "Start"));
        testGraph.addNode(new RoadNode("B", "Bravo", 13.1, 80.1, "Junction"));
        testGraph.addNode(new RoadNode("C", "Charlie", 13.2, 80.2, "Junction"));
        testGraph.addNode(new RoadNode("D", "Delta", 13.3, 80.3, "Target"));

        testGraph.addEdge(new RoadEdge("A", "B", 1.5, true));
        testGraph.addEdge(new RoadEdge("B", "D", 2.5, true));
        testGraph.addEdge(new RoadEdge("A", "C", 3.0, true));
        testGraph.addEdge(new RoadEdge("C", "D", 2.0, true));

        RoutingResultDto result = routingService.calculateShortestPath("A", "D");

        assertNotNull(result);
        assertEquals("A", result.getSource());
        assertEquals("D", result.getDestination());
        assertEquals(4.0, result.getTotalDistanceKm());
        assertEquals(List.of("A", "B", "D"), result.getPath());
        assertEquals(3, result.getPathDetails().size());
        assertEquals("SAMPLE_ROAD_NETWORK", result.getDataType());
    }

    @Test
    @DisplayName("Source equal to destination should return 0 distance and single node")
    void testSourceEqualsDestination() {
        RoutingResultDto result = routingService.calculateShortestPath("A", "A");

        assertNotNull(result);
        assertEquals(0.0, result.getTotalDistanceKm());
        assertEquals(List.of("A"), result.getPath());
        assertEquals(1, result.getPathDetails().size());
    }

    @Test
    @DisplayName("Unreachable destination should throw RouteNotFoundException")
    void testUnreachableDestination() {
        assertThrows(RouteNotFoundException.class, () ->
                routingService.calculateShortestPath("A", "E")
        );
    }

    @Test
    @DisplayName("Invalid or non-existent node ID should throw InvalidRouteException")
    void testInvalidNodeIdentifier() {
        assertThrows(InvalidRouteException.class, () ->
                routingService.calculateShortestPath("NON_EXISTENT", "D")
        );
        assertThrows(InvalidRouteException.class, () ->
                routingService.calculateShortestPath("A", "DOES_NOT_EXIST")
        );
        assertThrows(InvalidRouteException.class, () ->
                routingService.calculateShortestPath("", "D")
        );
    }

    @Test
    @DisplayName("Negative edge weight must be rejected during graph building")
    void testNegativeEdgeWeightRejection() {
        assertThrows(IllegalArgumentException.class, () ->
                new RoadEdge("A", "B", -5.0, true)
        );
    }
}
