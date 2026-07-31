package ru.vkontakte.task.vktask.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vkontakte.task.vktask.dto.CreateEdgeRequest;
import ru.vkontakte.task.vktask.dto.CreateNodeRequest;
import ru.vkontakte.task.vktask.dto.CreatePipelineRequest;
import ru.vkontakte.task.vktask.dto.EdgeResponse;
import ru.vkontakte.task.vktask.dto.ExecutionOrderResponse;
import ru.vkontakte.task.vktask.dto.NodeResponse;
import ru.vkontakte.task.vktask.dto.PipelineGraphResponse;
import ru.vkontakte.task.vktask.dto.PipelineResponse;
import ru.vkontakte.task.vktask.service.PipelineService;

@RestController
@RequestMapping("/pipelines")
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping
    public ResponseEntity<PipelineResponse> createPipeline(@Valid @RequestBody CreatePipelineRequest request) {
        PipelineResponse response = pipelineService.createPipeline(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{pipelineId}/nodes")
    public ResponseEntity<NodeResponse> addNode(
            @PathVariable UUID pipelineId,
            @Valid @RequestBody CreateNodeRequest request
    ) {
        NodeResponse response = pipelineService.addNode(pipelineId, request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{pipelineId}/edges")
    public ResponseEntity<EdgeResponse> addEdge(
            @PathVariable UUID pipelineId,
            @Valid @RequestBody CreateEdgeRequest request
    ) {
        EdgeResponse response = pipelineService.addEdge(pipelineId, request.sourceNodeId(), request.targetNodeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{pipelineId}")
    public PipelineGraphResponse getPipeline(@PathVariable UUID pipelineId) {
        return pipelineService.getPipelineGraph(pipelineId);
    }

    @GetMapping("/{pipelineId}/execution-order")
    public ExecutionOrderResponse getExecutionOrder(@PathVariable UUID pipelineId) {
        return pipelineService.getExecutionOrder(pipelineId);
    }
}
