/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.steps.servicenow;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.EnvironmentType;
import io.harness.beans.IdentifierRef;
import io.harness.common.NGTaskType;
import io.harness.common.NGTimeConversionHelper;
import io.harness.connector.ConnectorDTO;
import io.harness.connector.ConnectorResourceClient;
import io.harness.data.structure.EmptyPredicate;
import io.harness.delegate.TaskSelector;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.beans.connector.ConnectorConfigDTO;
import io.harness.delegate.beans.connector.jira.JiraConnectorDTO;
import io.harness.delegate.task.jira.JiraTaskNGParameters;
import io.harness.delegate.task.jira.JiraTaskNGResponse;
import io.harness.delegate.task.servicenow.ServiceNowTaskNGParameters;
import io.harness.encryption.Scope;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.WingsException;
import io.harness.ng.core.NGAccess;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.tasks.TaskCategory;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.yaml.ParameterField;
import io.harness.remote.client.NGRestUtils;
import io.harness.secretmanagerclient.services.api.SecretManagerClientService;
import io.harness.serializer.KryoSerializer;
import io.harness.steps.StepUtils;
import io.harness.steps.jira.JiraIssueOutcome;
import io.harness.steps.servicenow.beans.ServiceNowField;
import io.harness.supplier.ThrowingSupplier;
import io.harness.utils.IdentifierRefHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@OwnedBy(HarnessTeam.CDC)
@UtilityClass
public class ServiceNowStepUtils {
  private final ConnectorResourceClient connectorResourceClient;
  private final SecretManagerClientService secretManagerClientService;
  private final KryoSerializer kryoSerializer;

  public Map<String, ParameterField<String>> processServiceNowFieldsList(List<ServiceNowField> fields) {
    if (fields == null) {
      return null;
    }

    Map<String, ParameterField<String>> fieldsMap = new HashMap<>();
    Set<String> duplicateFields = new HashSet<>();
    fields.forEach(field -> {
      if (fieldsMap.containsKey(field.getName())) {
        duplicateFields.add(field.getName());
      } else {
        fieldsMap.put(field.getName(), field.getValue());
      }
    });

    if (EmptyPredicate.isNotEmpty(duplicateFields)) {
      throw new InvalidRequestException(
          String.format("Duplicate ServiceNow fields: [%s]", String.join(", ", duplicateFields)));
    }
    return fieldsMap;
  }

  public Map<String, String> processServiceNowFieldsInSpec(Map<String, ParameterField<String>> fields) {
    if (EmptyPredicate.isEmpty(fields)) {
      return Collections.emptyMap();
    }
    Map<String, String> finalMap = new HashMap<>();
    for (Map.Entry<String, ParameterField<String>> entry : fields.entrySet()) {
      if (EmptyPredicate.isEmpty(entry.getKey()) || ParameterField.isNull(entry.getValue())) {
        continue;
      }
      if (entry.getValue().isExpression()) {
        throw new InvalidRequestException(String.format("Field [%s] has invalid ServiceNow field value", entry.getKey()));
      }
      if (entry.getValue().getValue() == null) {
        continue;
      }
      finalMap.put(entry.getKey(), entry.getValue().getValue());
    }
    return finalMap;
  }

  @Override
  public TaskRequest prepareTaskRequest(ServiceNowTaskNGParameters.ServiceNowTaskNGParametersBuilder paramsBuilder, Ambiance ambiance,
                                        String connectorRef, String timeStr, String taskName) {
    NGAccess ngAccess = AmbianceUtils.getNgAccess(ambiance);
    IdentifierRef identifierRef = IdentifierRefHelper.getIdentifierRef(
            connectorRef, ngAccess.getAccountIdentifier(), ngAccess.getOrgIdentifier(), ngAccess.getProjectIdentifier());
    Optional<ConnectorDTO> connectorDTOOptional = NGRestUtils.getResponse(
            connectorResourceClient.get(identifierRef.getIdentifier(), identifierRef.getAccountIdentifier(),
                    identifierRef.getOrgIdentifier(), identifierRef.getProjectIdentifier()));
    if (!connectorDTOOptional.isPresent()) {
      throw new InvalidRequestException(
              String.format("Connector not found for identifier: [%s]", connectorRef), WingsException.USER);
    }

    ConnectorConfigDTO configDTO = connectorDTOOptional.get().getConnectorInfo().getConnectorConfig();
    if (!(configDTO instanceof JiraConnectorDTO)) {
      throw new InvalidRequestException(
              String.format("Connector [%s] is not a jira connector", connectorRef), WingsException.USER);
    }

    JiraConnectorDTO connectorDTO = (JiraConnectorDTO) configDTO;
    paramsBuilder.jiraConnectorDTO(connectorDTO);
    paramsBuilder.encryptionDetails(secretManagerClientService.getEncryptionDetails(ngAccess, connectorDTO));
    JiraTaskNGParameters params = paramsBuilder.build();

    TaskData taskData = TaskData.builder()
            .async(true)
            .timeout(NGTimeConversionHelper.convertTimeStringToMilliseconds(timeStr))
            .taskType(NGTaskType.JIRA_TASK_NG.name())
            .parameters(new Object[] {params})
            .build();
    return StepUtils.prepareTaskRequest(ambiance, taskData, kryoSerializer, TaskCategory.DELEGATE_TASK_V2,
            Collections.emptyList(), false, taskName,
            params.getDelegateSelectors()
                    .stream()
                    .map(s -> TaskSelector.newBuilder().setSelector(s).build())
                    .collect(Collectors.toList()),
            Scope.PROJECT, EnvironmentType.ALL);
  }

  @Override
  public StepResponse prepareStepResponse(ThrowingSupplier<JiraTaskNGResponse> responseSupplier) throws Exception {
    JiraTaskNGResponse taskResponse = responseSupplier.get();
    return StepResponse.builder()
            .status(Status.SUCCEEDED)
            .stepOutcome(StepResponse.StepOutcome.builder()
                    .name("issue")
                    .outcome(new JiraIssueOutcome(taskResponse.getIssue()))
                    .build())
            .build();
  }
}
