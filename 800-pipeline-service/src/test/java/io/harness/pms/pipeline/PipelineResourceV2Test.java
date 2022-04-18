package io.harness.pms.pipeline;

import static io.harness.rule.OwnerRule.NAMAN;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import io.harness.CategoryTest;
import io.harness.category.element.UnitTests;
import io.harness.ng.core.dto.ResponseDTO;
import io.harness.pms.contracts.governance.GovernanceMetadata;
import io.harness.pms.governance.PipelineSaveResponse;
import io.harness.pms.pipeline.service.PMSPipelineService;
import io.harness.rule.Owner;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PipelineResourceV2Test extends CategoryTest {
  PipelineResourceV2 pipelineResource;
  @Mock PMSPipelineService pmsPipelineService;

  private final String accountId = "accountId";
  private final String orgId = "orgId";
  private final String projectId = "projId";
  private final String pipelineId = "pipelineId";
  private final String pipelineYaml = "pipeline:\n"
      + "  identifier: pipelineId\n"
      + "  name: pipeline name";

  PipelineEntity entity;
  PipelineEntity entityWithVersion;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    pipelineResource = new PipelineResourceV2(pmsPipelineService);
    String pipelineName = "pipeline name";

    entity = PipelineEntity.builder()
                 .accountId(accountId)
                 .orgIdentifier(orgId)
                 .projectIdentifier(projectId)
                 .identifier(pipelineId)
                 .name(pipelineName)
                 .yaml(pipelineYaml)
                 .allowStageExecutions(false)
                 .build();
    entityWithVersion = entity.withVersion(0L);
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testCreatePipeline() {
    doReturn(GovernanceMetadata.newBuilder().setDeny(false).build())
        .when(pmsPipelineService)
        .validatePipelineYamlAndSetTemplateRefIfAny(entity);
    doReturn(entityWithVersion).when(pmsPipelineService).create(entity);
    ResponseDTO<PipelineSaveResponse> responseDTO =
        pipelineResource.createPipeline(accountId, orgId, projectId, null, null, null, null, pipelineYaml);
    assertThat(responseDTO.getData().getGovernanceMetadata().getDeny()).isFalse();
    assertThat(responseDTO.getData().getIdentifier()).isEqualTo(pipelineId);
  }

  @Test
  @Owner(developers = NAMAN)
  @Category(UnitTests.class)
  public void testCreatePipelineWithFailedGovernanceChecks() {
    doReturn(GovernanceMetadata.newBuilder().setDeny(true).build())
        .when(pmsPipelineService)
        .validatePipelineYamlAndSetTemplateRefIfAny(entity);
    ResponseDTO<PipelineSaveResponse> responseDTO =
        pipelineResource.createPipeline(accountId, orgId, projectId, null, null, null, null, pipelineYaml);
    assertThat(responseDTO.getData().getGovernanceMetadata().getDeny()).isTrue();
    assertThat(responseDTO.getData().getIdentifier()).isNull();
  }
}