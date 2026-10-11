package com.nourishnet.routing;

import com.nourishnet.model.RoadEdge;
import com.nourishnet.model.RoadNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Weighted Graph representation of a road network.
 * Nodes represent road junctions or hubs; edges represent road connections
 * with non-negative distance weights in kilometers.
 */
public class WeightedGraph {
    private final Map<String, RoadNode> nodes = new ConcurrentHashMap<>();
    private final Map<String, List<RoadEdge>> adjacencyList = new ConcurrentHashMap<>();

    public void addNode(RoadNode node) {
        if (node == null || node.getId() == null) {
            throw new IllegalArgumentException("Road node and its ID cannot be null");
        }
        nodes.put(node.getId(), node);
        adjacencyList.putIfAbsent(node.getId(), new CopyOnWriteArrayList<>());
    }

    public void addEdge(RoadEdge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Road edge cannot be null");
        }
        if (edge.getDistanceKm() < 0) {
            throw new IllegalArgumentException("Edge weight must be non-negative: " + edge.getDistanceKm());
        }
        if (!nodes.containsKey(edge.getFromNodeId())) {
            throw new IllegalArgumentException("Source node does not exist in graph: " + edge.getFromNodeId());
        }
        if (!nodes.containsKey(edge.getToNodeId())) {
            throw new IllegalArgumentException("Destination node does not exist in graph: " + edge.getToNodeId());
        }

        adjacencyList.get(edge.getFromNodeId()).add(edge);

        if (edge.isBidirectional()) {
            RoadEdge reverseEdge = new RoadEdge(
                    edge.getToNodeId(),
                    edge.getFromNodeId(),
                    edge.getDistanceKm(),
                    true
            );
            adjacencyList.get(edge.getToNodeId()).add(reverseEdge);
        }
    }

    public boolean hasNode(String nodeId) {
        return nodes.containsKey(nodeId);
    }

    public RoadNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public Map<String, RoadNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public List<RoadEdge> getEdgesFrom(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    public List<RoadEdge> getAllEdges() {
        List<RoadEdge> all = new ArrayList<>();
        for (List<RoadEdge> edges : adjacencyList.values()) {
            all.addAll(edges);
        }
        return all;
    }

    public void clear() {
        nodes.clear();
        adjacencyList.clear();
    }
}
