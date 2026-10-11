package com.nourishnet.routing;

import com.nourishnet.dto.RoutingResultDto;
import com.nourishnet.exception.InvalidRouteException;
import com.nourishnet.exception.RouteNotFoundException;
import com.nourishnet.model.RoadEdge;
import com.nourishnet.model.RoadNode;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Dijkstra's Shortest-Path Algorithm on a weighted road network graph.
 * Uses a PriorityQueue min-heap for O((V + E) log V) efficiency.
 */
@Service
public class DijkstraRoutingService {
    private final WeightedGraph graph;

    public DijkstraRoutingService() {
        this.graph = new WeightedGraph();
    }

    public DijkstraRoutingService(WeightedGraph graph) {
        this.graph = graph;
    }

    public WeightedGraph getGraph() {
        return graph;
    }

    /**
     * Calculates the shortest path between source and destination nodes using Dijkstra's algorithm.
     *
     * @param sourceId      Identifier of the source road junction
     * @param destinationId Identifier of the destination road junction
     * @return RoutingResultDto with ordered nodes, node details, and total distance in km
     */
    public RoutingResultDto calculateShortestPath(String sourceId, String destinationId) {
        if (sourceId == null || sourceId.trim().isEmpty()) {
            throw new InvalidRouteException("Source road junction identifier cannot be empty");
        }
        if (destinationId == null || destinationId.trim().isEmpty()) {
            throw new InvalidRouteException("Destination road junction identifier cannot be empty");
        }

        String source = sourceId.trim();
        String destination = destinationId.trim();

        if (!graph.hasNode(source)) {
            throw new InvalidRouteException("Source road junction does not exist: " + source);
        }
        if (!graph.hasNode(destination)) {
            throw new InvalidRouteException("Destination road junction does not exist: " + destination);
        }

        // Case: Source equals destination
        if (source.equals(destination)) {
            RoadNode node = graph.getNode(source);
            return new RoutingResultDto(
                    source,
                    destination,
                    Collections.singletonList(source),
                    Collections.singletonList(node),
                    0.0
            );
        }

        // Standard Dijkstra algorithm using PriorityQueue min-heap
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::distance));
        Set<String> visited = new HashSet<>();

        for (String nodeId : graph.getNodes().keySet()) {
            distances.put(nodeId, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);
        pq.offer(new NodeDistance(source, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String currentId = current.nodeId();

            if (current.distance() > distances.get(currentId)) {
                continue; // Stale queue entry
            }

            if (currentId.equals(destination)) {
                break; // Target reached
            }

            visited.add(currentId);

            for (RoadEdge edge : graph.getEdgesFrom(currentId)) {
                String neighborId = edge.getToNodeId();
                if (visited.contains(neighborId)) {
                    continue;
                }

                double newDistance = distances.get(currentId) + edge.getDistanceKm();
                if (newDistance < distances.get(neighborId)) {
                    distances.put(neighborId, newDistance);
                    previous.put(neighborId, currentId);
                    pq.offer(new NodeDistance(neighborId, newDistance));
                }
            }
        }

        Double totalDistance = distances.get(destination);
        if (totalDistance == null || Double.isInfinite(totalDistance)) {
            throw new RouteNotFoundException(
                    "No navigable road path found from " + source + " to " + destination + " in the road graph."
            );
        }

        // Reconstruct path sequence from destination backwards to source
        LinkedList<String> pathNodeIds = new LinkedList<>();
        LinkedList<RoadNode> pathNodes = new LinkedList<>();
        String step = destination;

        while (step != null) {
            pathNodeIds.addFirst(step);
            pathNodes.addFirst(graph.getNode(step));
            step = previous.get(step);
        }

        // Double precision rounding to 2 decimals
        double roundedDistance = Math.round(totalDistance * 100.0) / 100.0;

        return new RoutingResultDto(source, destination, pathNodeIds, pathNodes, roundedDistance);
    }

    private record NodeDistance(String nodeId, double distance) {}
}
