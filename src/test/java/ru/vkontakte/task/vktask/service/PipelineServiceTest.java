package ru.vkontakte.task.vktask.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.vkontakte.task.vktask.dto.ExecutionOrderResponse;
import ru.vkontakte.task.vktask.dto.NodeResponse;
import ru.vkontakte.task.vktask.entity.Pipeline;
import ru.vkontakte.task.vktask.entity.PipelineEdge;
import ru.vkontakte.task.vktask.entity.PipelineNode;
import ru.vkontakte.task.vktask.exception.CycleDetectedException;
import ru.vkontakte.task.vktask.exception.DuplicateNodeException;
import ru.vkontakte.task.vktask.exception.SelfDependencyException;
import ru.vkontakte.task.vktask.repository.PipelineEdgeRepository;
import ru.vkontakte.task.vktask.repository.PipelineNodeRepository;
import ru.vkontakte.task.vktask.repository.PipelineRepository;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @Mock
    private PipelineNodeRepository nodeRepository;

    @Mock
    private PipelineEdgeRepository edgeRepository;

    @InjectMocks
    private PipelineService pipelineService;

    @Test
    void addNodeRejectsDuplicateName() {
        UUID pipelineId = UUID.randomUUID();
        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline("main", pipelineId)));
        when(nodeRepository.existsByPipelineIdAndName(pipelineId, "input")).thenReturn(true);

        assertThatThrownBy(() -> pipelineService.addNode(pipelineId, "input"))
                .isInstanceOf(DuplicateNodeException.class);

        verify(nodeRepository, never()).save(any());
    }

    @Test
    void addEdgeRejectsSelfDependency() {
        UUID pipelineId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        assertThatThrownBy(() -> pipelineService.addEdge(pipelineId, nodeId, nodeId))
                .isInstanceOf(SelfDependencyException.class);

        verify(edgeRepository, never()).save(any());
    }

    @Test
    void addEdgeRejectsCycle() {
        UUID pipelineId = UUID.randomUUID();
        Pipeline pipeline = pipeline("main", pipelineId);
        PipelineNode input = node(pipeline, "input");
        PipelineNode filter = node(pipeline, "filter");
        PipelineNode output = node(pipeline, "output");

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(nodeRepository.findByIdAndPipelineId(output.getId(), pipelineId)).thenReturn(Optional.of(output));
        when(nodeRepository.findByIdAndPipelineId(input.getId(), pipelineId)).thenReturn(Optional.of(input));
        when(edgeRepository.existsByPipelineIdAndSourceNodeIdAndTargetNodeId(pipelineId, output.getId(), input.getId()))
                .thenReturn(false);
        when(nodeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId)).thenReturn(List.of(input, filter, output));
        when(edgeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId))
                .thenReturn(List.of(edge(pipeline, input, filter), edge(pipeline, filter, output)));

        assertThatThrownBy(() -> pipelineService.addEdge(pipelineId, output.getId(), input.getId()))
                .isInstanceOf(CycleDetectedException.class);

        verify(edgeRepository, never()).save(any());
    }

    @Test
    void getExecutionOrderReturnsTopologicalOrder() {
        UUID pipelineId = UUID.randomUUID();
        Pipeline pipeline = pipeline("main", pipelineId);
        PipelineNode input = node(pipeline, "input");
        PipelineNode filter = node(pipeline, "filter");
        PipelineNode enrich = node(pipeline, "enrich");
        PipelineNode output = node(pipeline, "output");

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(nodeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId))
                .thenReturn(List.of(input, filter, enrich, output));
        when(edgeRepository.findAllByPipelineIdOrderByCreatedAtAsc(pipelineId))
                .thenReturn(List.of(
                        edge(pipeline, input, filter),
                        edge(pipeline, input, enrich),
                        edge(pipeline, filter, output),
                        edge(pipeline, enrich, output)
                ));

        ExecutionOrderResponse response = pipelineService.getExecutionOrder(pipelineId);

        assertThat(response.nodes())
                .extracting(NodeResponse::name)
                .containsExactly("input", "filter", "enrich", "output");
    }

    private Pipeline pipeline(String name, UUID id) {
        Pipeline pipeline = new Pipeline(name);
        ReflectionTestUtils.setField(pipeline, "id", id);
        return pipeline;
    }

    private PipelineNode node(Pipeline pipeline, String name) {
        PipelineNode node = new PipelineNode(pipeline, name);
        ReflectionTestUtils.setField(node, "id", UUID.randomUUID());
        return node;
    }

    private PipelineEdge edge(Pipeline pipeline, PipelineNode sourceNode, PipelineNode targetNode) {
        PipelineEdge edge = new PipelineEdge(pipeline, sourceNode, targetNode);
        ReflectionTestUtils.setField(edge, "id", UUID.randomUUID());
        return edge;
    }
}
