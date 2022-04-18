package io.harness.pms.pipeline;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.NGCommonEntityConstants;
import io.harness.accesscontrol.AccountIdentifier;
import io.harness.accesscontrol.NGAccessControlCheck;
import io.harness.accesscontrol.OrgIdentifier;
import io.harness.accesscontrol.ProjectIdentifier;
import io.harness.annotations.dev.OwnedBy;
import io.harness.gitsync.interceptor.GitEntityCreateInfoDTO;
import io.harness.ng.core.dto.ErrorDTO;
import io.harness.ng.core.dto.FailureDTO;
import io.harness.ng.core.dto.ResponseDTO;
import io.harness.pms.annotations.PipelineServiceAuth;
import io.harness.pms.contracts.governance.GovernanceMetadata;
import io.harness.pms.governance.PipelineSaveResponse;
import io.harness.pms.pipeline.mappers.PMSPipelineDtoMapper;
import io.harness.pms.pipeline.service.PMSPipelineService;
import io.harness.pms.rbac.PipelineRbacPermissions;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(PIPELINE)
@Api("v2/pipelines")
@Path("v2/pipelines")
@Produces({"application/json", "application/yaml"})
@Consumes({"application/json", "application/yaml"})
@AllArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({ @Inject }))
@ApiResponses(value =
    {
      @ApiResponse(code = 400, response = FailureDTO.class, message = "Bad Request")
      , @ApiResponse(code = 500, response = ErrorDTO.class, message = "Internal server error")
    })
@Tag(name = "Pipelines", description = "This contains APIs related to pipelines")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request",
    content =
    {
      @Content(mediaType = "application/json", schema = @Schema(implementation = FailureDTO.class))
      , @Content(mediaType = "application/yaml", schema = @Schema(implementation = FailureDTO.class))
    })
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found",
    content =
    {
      @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))
      , @Content(mediaType = "application/yaml", schema = @Schema(implementation = ErrorDTO.class))
    })
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
    content =
    {
      @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))
      , @Content(mediaType = "application/yaml", schema = @Schema(implementation = ErrorDTO.class))
    })
@PipelineServiceAuth
@Slf4j
public class PipelineResourceV2 {
  private final PMSPipelineService pmsPipelineService;

  @POST
  @ApiOperation(value = "Create a Pipeline", nickname = "v2CreatePipeline")
  @Operation(operationId = "v2CreatePipeline", summary = "Create a Pipeline With Governance Checks",
      responses =
      {
        @io.swagger.v3.oas.annotations.responses.
        ApiResponse(responseCode = "default", description = "Returns created pipeline with metadata")
      })
  @NGAccessControlCheck(resourceType = "PIPELINE", permission = PipelineRbacPermissions.PIPELINE_CREATE_AND_EDIT)
  public ResponseDTO<PipelineSaveResponse>
  createPipeline(@Parameter(description = PipelineResourceConstants.ACCOUNT_PARAM_MESSAGE, required = true) @NotNull
                   @QueryParam(NGCommonEntityConstants.ACCOUNT_KEY) @AccountIdentifier String accountId,
      @Parameter(description = PipelineResourceConstants.ORG_PARAM_MESSAGE, required = true) @NotNull @QueryParam(
          NGCommonEntityConstants.ORG_KEY) @OrgIdentifier String orgId,
      @Parameter(description = PipelineResourceConstants.PROJECT_PARAM_MESSAGE, required = true) @NotNull @QueryParam(
          NGCommonEntityConstants.PROJECT_KEY) @ProjectIdentifier String projectId,
      @Parameter(description = PipelineResourceConstants.PIPELINE_ID_PARAM_MESSAGE, required = false) @QueryParam(
          NGCommonEntityConstants.IDENTIFIER_KEY) String pipelineIdentifier,
      @Parameter(description = PipelineResourceConstants.PIPELINE_NAME_PARAM_MESSAGE, required = false) @QueryParam(
          NGCommonEntityConstants.NAME_KEY) String pipelineName,
      @Parameter(description = PipelineResourceConstants.PIPELINE_DESCRIPTION_PARAM_MESSAGE,
          required = false) @QueryParam(NGCommonEntityConstants.DESCRIPTION_KEY) String pipelineDescription,
      @BeanParam GitEntityCreateInfoDTO gitEntityCreateInfo,
      @RequestBody(required = true, description = "Pipeline YAML") @NotNull String yaml) {
    PipelineEntity pipelineEntity = PMSPipelineDtoMapper.toPipelineEntity(accountId, orgId, projectId, yaml);
    log.info(String.format("Creating pipeline with identifier %s in project %s, org %s, account %s",
        pipelineEntity.getIdentifier(), projectId, orgId, accountId));

    GovernanceMetadata governanceMetadata =
        pmsPipelineService.validatePipelineYamlAndSetTemplateRefIfAny(pipelineEntity);
    if (governanceMetadata.getDeny()) {
      return ResponseDTO.newResponse(PipelineSaveResponse.builder().governanceMetadata(governanceMetadata).build());
    }

    PipelineEntity createdEntity = pmsPipelineService.create(pipelineEntity);
    return ResponseDTO.newResponse(createdEntity.getVersion().toString(),
        PipelineSaveResponse.builder()
            .governanceMetadata(governanceMetadata)
            .identifier(createdEntity.getIdentifier())
            .build());
  }
}
