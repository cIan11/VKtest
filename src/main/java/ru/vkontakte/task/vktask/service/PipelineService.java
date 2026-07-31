package ru.vkontakte.task.vktask.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vkontakte.task.vktask.dto.EdgeResponse;
import ru.vkontakte.task.vktask.dto.ExecutionOrderResponse;
import ru.vkontakte.task.vktask.dto.NodeResponse;
import ru.vkontakte.task.vktask.dto.PipelineGraphResponse;
import ru.vkontakte.task.vktask.dto.PipelineResponse;
import ru.vkontakte.task.vktask.exception.CycleDetectedException;
import ru.vkontakte.task.vktask.exception.DuplicateEdgeException;
import ru.vkontakte.task.vktask.exception.DuplicateNodeException;
import ru.vkontakte.task.vktask.exception.NodeNotFoundException;
import ru.vkontakte.task.vktask.exception.PipelineNotFoundException;
import ru.vkontakte.task.vktask.exception.SelfDependencyException;
import ru.vkontakte.task.vktask.entity.Pipeline;
import ru.vkontakte.task.vktask.entity.PipelineEdge;
import ru.vkontakte.task.vktask.entity.PipelineNode;
import ru.vkontakte.task.vktask.repository.PipelineEdgeRepository;
import ru.vkontakte.task.vktask.repository.PipelineNodeRepository;
import ru.vkontakte.task.vktask.repository.PipelineRepository;

@Service
public class PipelineService {

    private final PipelineRepository pipelineRepository;
    private final PipelineNodeRepository nodeRepository;
    private final PipelineEdgeRepository edgeRepository;

    public PipelineService(
            PipelineRepository pipelineRepository,
            PipelineNodeRepository nodeRepository,
            PipelineEdgeRepository edgeRepository
    ) {
        this.pipelineRepository = pipelineRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
    }

    @Transactional
    public PipelineResponse createPipeline(String name) {
        Pipeline pipeline = pipelineRepository.save(new Pipeline(name));
        return toPipelineResponse(pipeline);
    }

    @Transactional
    public NodeResponse addNode(UUID pipelineId, String name) {
        Pipeline pipeline = getPipeline(pipelineId);
        if (nodeRepository.existsByPipelineIdAndName(pipelineId, name)) {
            throw new DuplicateNodeException(name);
        }

        PipelineNode node = nodeRepository.save(new PipelineNode(pipeline, name));
        return toNodeResponse(node);
    }

    @Transactional
    public EdgeResponse addEdge(UUID pipelineId, UUID sourceNodeId, UUID targetNodeId) {
        if (sourceNodeId.equals(targetNodeId)) {
            throw new SelfDependencyException();
        }

        Pipeline pipeline = getPipeline(pipelineId);
        PipelineNode sourceNode = getNode(pipelineId, sourceNodeId);
        PipelineNode targetNode = getNode(pipelineId, targetNodeId);

        if (edgeRepository.existsByPipelineIdAndSourceNodeIdAndTargetNodeId(pipelineId, sourceNodeId, targetNodeId)) {
            throw new DuplicateEdgeException();
        }

        List<PipelineNode> nodes = nodeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId);
        List<PipelineEdge> edges = edgeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId);
        if (createsCycle(nodes, edges, sourceNodeId, targetNodeId)) {
            throw new CycleDetectedException();
        }

        PipelineEdge edge = edgeRepository.save(new PipelineEdge(pipeline, sourceNode, targetNode));
        return toEdgeResponse(edge);
    }

    @Transactional(readOnly = true)
    public PipelineGraphResponse getPipelineGraph(UUID pipelineId) {
        Pipeline pipeline = getPipeline(pipelineId);
        List<NodeResponse> nodes = nodeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId).stream()
                .map(this::toNodeResponse)
                .toList();
        List<EdgeResponse> edges = edgeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId).stream()
                .map(this::toEdgeResponse)
                .toList();

        return new PipelineGraphResponse(pipeline.getId(), pipeline.getName(), nodes, edges);
    }

    @Transactional(readOnly = true)
    public ExecutionOrderResponse getExecutionOrder(UUID pipelineId) {
        getPipeline(pipelineId);
        List<PipelineNode> nodes = nodeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId);
        List<PipelineEdge> edges = edgeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId);
        List<NodeResponse> order = topologicalSort(nodes, edges).stream()
                .map(this::toNodeResponse)
                .toList();

        return new ExecutionOrderResponse(pipelineId, order);
    }

    private Pipeline getPipeline(UUID pipelineId) {
        return pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new PipelineNotFoundException(pipelineId));
    }

    private PipelineNode getNode(UUID pipelineId, UUID nodeId) {
        return nodeRepository.findByIdAndPipelineId(nodeId, pipelineId)
                .orElseThrow(() -> new NodeNotFoundException(nodeId));
    }

    private boolean createsCycle(
            List<PipelineNode> nodes,
            List<PipelineEdge> edges,
            UUID newSourceNodeId,
            UUID newTargetNodeId
    ) {
        Map<UUID, List<UUID>> graph = buildAdjacency(nodes, edges);
        graph.computeIfAbsent(newSourceNodeId, ignored -> new ArrayList<>()).add(newTargetNodeId);
        return hasPath(graph, newTargetNodeId, newSourceNodeId);
    }

    private boolean hasPath(Map<UUID, List<UUID>> graph, UUID startNodeId, UUID targetNodeId) {
        Set<UUID> visited = new HashSet<>();
        Queue<UUID> queue = new ArrayDeque<>();
        queue.add(startNodeId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            if (current.equals(targetNodeId)) {
                return true;
            }
            queue.addAll(graph.getOrDefault(current, List.of()));
        }

        return false;
    }

    private List<PipelineNode> topologicalSort(List<PipelineNode> nodes, List<PipelineEdge> edges) {
        Map<UUID, PipelineNode> nodesById = new LinkedHashMap<>();
        Map<UUID, Integer> inDegree = new HashMap<>();
        Map<UUID, List<UUID>> graph = buildAdjacency(nodes, edges);

        for (PipelineNode node : nodes) {
            nodesById.put(node.getId(), node);
            inDegree.put(node.getId(), 0);
        }
        for (PipelineEdge edge : edges) {
            UUID targetNodeId = edge.getTargetNode().getId();
            inDegree.compute(targetNodeId, (id, degree) -> degree == null ? 1 : degree + 1);
        }

        Queue<UUID> ready = new ArrayDeque<>();
        for (PipelineNode node : nodes) {
            if (inDegree.get(node.getId()) == 0) {
                ready.add(node.getId());
            }
        }

        List<PipelineNode> result = new ArrayList<>();
        while (!ready.isEmpty()) {
            UUID current = ready.poll();
            result.add(nodesById.get(current));

            for (UUID target : graph.getOrDefault(current, List.of())) {
                int updatedDegree = inDegree.computeIfPresent(target, (id, degree) -> degree - 1);
                if (updatedDegree == 0) {
                    ready.add(target);
                }
            }
        }

        if (result.size() != nodes.size()) {
            throw new CycleDetectedException();
        }

        return result;
    }

    private Map<UUID, List<UUID>> buildAdjacency(List<PipelineNode> nodes, List<PipelineEdge> edges) {
        Map<UUID, List<UUID>> graph = new HashMap<>();
        for (PipelineNode node : nodes) {
            graph.put(node.getId(), new ArrayList<>());
        }
        for (PipelineEdge edge : edges) {
            graph.computeIfAbsent(edge.getSourceNode().getId(), ignored -> new ArrayList<>())
                    .add(edge.getTargetNode().getId());
        }
        return graph;
    }

    private PipelineResponse toPipelineResponse(Pipeline pipeline) {
        return new PipelineResponse(
                pipeline.getId(),
                pipeline.getName()
        );
    }

    private NodeResponse toNodeResponse(PipelineNode node) {
        return new NodeResponse(node.getId(), node.getName());
    }

    private EdgeResponse toEdgeResponse(PipelineEdge edge) {
        return new EdgeResponse(edge.getId(), edge.getSourceNode().getId(), edge.getTargetNode().getId());
    }
}
