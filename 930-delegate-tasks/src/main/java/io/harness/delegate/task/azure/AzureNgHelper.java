/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.azure;

import static java.lang.String.format;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.artifact.ArtifactMetadataKeys;
import io.harness.artifact.ArtifactUtilities;
import io.harness.artifacts.beans.BuildDetailsInternal;
import io.harness.artifacts.comparator.BuildDetailsInternalComparatorDescending;
import io.harness.azure.client.AzureAuthorizationClient;
import io.harness.azure.client.AzureComputeClient;
import io.harness.azure.client.AzureContainerRegistryClient;
import io.harness.azure.client.AzureKubernetesClient;
import io.harness.azure.client.AzureManagementClient;
import io.harness.azure.model.AzureAuthenticationType;
import io.harness.azure.model.AzureConfig;
import io.harness.azure.response.AcrRegistriesDTO;
import io.harness.azure.response.AcrRegistryDTO;
import io.harness.azure.response.AcrRepositoriesDTO;
import io.harness.azure.response.AcrRepositoryDTO;
import io.harness.azure.response.AzureClusterDTO;
import io.harness.azure.response.AzureClustersDTO;
import io.harness.azure.response.AzureResourceGroupDTO;
import io.harness.azure.response.AzureResourceGroupsDTO;
import io.harness.azure.response.AzureSubscriptionDTO;
import io.harness.azure.response.AzureSubscriptionsDTO;
import io.harness.connector.ConnectivityStatus;
import io.harness.connector.ConnectorValidationResult;
import io.harness.delegate.beans.azure.response.AzureClustersResponse;
import io.harness.delegate.beans.azure.response.AzureRegistriesResponse;
import io.harness.delegate.beans.azure.response.AzureRepositoriesResponse;
import io.harness.delegate.beans.azure.response.AzureResourceGroupsResponse;
import io.harness.delegate.beans.azure.response.AzureSubscriptionsResponse;
import io.harness.delegate.beans.connector.azureconnector.AzureConnectorDTO;
import io.harness.delegate.task.artifacts.mappers.AcrRequestResponseMapper;
import io.harness.errorhandling.NGErrorHelper;
import io.harness.exception.AzureAKSException;
import io.harness.exception.AzureContainerRegistryException;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.NestedExceptionUtils;
import io.harness.expression.RegexFunctor;
import io.harness.k8s.model.KubernetesConfig;
import io.harness.logging.CommandExecutionStatus;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.security.encryption.SecretDecryptionService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerservice.KubernetesCluster;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@OwnedBy(HarnessTeam.CDP)
@Singleton
public class AzureNgHelper {
  @Inject private SecretDecryptionService secretDecryptionService;
  @Inject private AzureAuthorizationClient azureAuthorizationClient;
  @Inject private AzureComputeClient azureComputeClient;
  @Inject private AzureManagementClient azureManagementClient;
  @Inject private AzureContainerRegistryClient azureContainerRegistryClient;
  @Inject private AzureKubernetesClient azureKubernetesClient;
  @Inject private NGErrorHelper ngErrorHelper;

  public ConnectorValidationResult getConnectorValidationResult(
      List<EncryptedDataDetail> encryptedDataDetails, AzureConnectorDTO connectorDTO) {
    ConnectorValidationResult connectorValidationResult;
    try {
      AzureConfig azureConfig = AcrRequestResponseMapper.toAzureInternalConfig(connectorDTO.getCredential(),
          encryptedDataDetails, connectorDTO.getCredential().getAzureCredentialType(),
          connectorDTO.getAzureEnvironmentType(), secretDecryptionService);

      azureAuthorizationClient.validateAzureConnection(azureConfig);
      connectorValidationResult = ConnectorValidationResult.builder()
                                      .status(ConnectivityStatus.SUCCESS)
                                      .testedAt(System.currentTimeMillis())
                                      .build();
    } catch (Exception e) {
      String errorMessage = e.getMessage();
      connectorValidationResult = ConnectorValidationResult.builder()
                                      .status(ConnectivityStatus.FAILURE)
                                      .errors(Collections.singletonList(ngErrorHelper.createErrorDetail(errorMessage)))
                                      .errorSummary(ngErrorHelper.getErrorSummary(errorMessage))
                                      .testedAt(System.currentTimeMillis())
                                      .build();
    }
    return connectorValidationResult;
  }

  public AzureSubscriptionsResponse listSubscriptions(
      List<EncryptedDataDetail> encryptionDetails, AzureConnectorDTO azureConnector) {
    AzureConfig azureConfig = AcrRequestResponseMapper.toAzureInternalConfig(azureConnector.getCredential(),
        encryptionDetails, azureConnector.getCredential().getAzureCredentialType(),
        azureConnector.getAzureEnvironmentType(), secretDecryptionService);

    AzureSubscriptionsResponse response;
    try {
      AzureSubscriptionsDTO subscriptions =
          AzureSubscriptionsDTO.builder()
              .subscriptions(azureComputeClient.listSubscriptions(azureConfig)
                                 .stream()
                                 .map(subscription
                                     -> AzureSubscriptionDTO.builder()
                                            .subscriptionId(subscription.subscriptionId())
                                            .subscriptionName(subscription.displayName())
                                            .build())
                                 .collect(Collectors.toList()))
              .build();
      response = AzureSubscriptionsResponse.builder()
                     .subscriptions(subscriptions)
                     .commandExecutionStatus(CommandExecutionStatus.SUCCESS)
                     .build();

    } catch (Exception e) {
      String errorMessage = e.getMessage();
      response = AzureSubscriptionsResponse.builder()
                     .commandExecutionStatus(CommandExecutionStatus.FAILURE)
                     .errorSummary(ngErrorHelper.getErrorSummary(errorMessage))
                     .build();
    }
    return response;
  }

  public AzureResourceGroupsResponse listResourceGroups(
      List<EncryptedDataDetail> encryptionDetails, AzureConnectorDTO azureConnector, String subscriptionId) {
    AzureConfig azureConfig = AcrRequestResponseMapper.toAzureInternalConfig(azureConnector.getCredential(),
        encryptionDetails, azureConnector.getCredential().getAzureCredentialType(),
        azureConnector.getAzureEnvironmentType(), secretDecryptionService);
    AzureResourceGroupsResponse response;
    try {
      AzureResourceGroupsDTO resourceGroups =
          AzureResourceGroupsDTO.builder()
              .resourceGroups(azureManagementClient.listResourceGroups(azureConfig, subscriptionId)
                                  .stream()
                                  .map(HasName::name)
                                  .map(name -> AzureResourceGroupDTO.builder().resourceGroup(name).build())
                                  .collect(Collectors.toList()))
              .build();
      response = AzureResourceGroupsResponse.builder()
                     .resourceGroups(resourceGroups)
                     .commandExecutionStatus(CommandExecutionStatus.SUCCESS)
                     .build();

    } catch (Exception e) {
      String errorMessage = e.getMessage();
      response = AzureResourceGroupsResponse.builder()
                     .commandExecutionStatus(CommandExecutionStatus.FAILURE)
                     .errorSummary(ngErrorHelper.getErrorSummary(errorMessage))
                     .build();
    }
    return response;
  }

  public AzureRegistriesResponse listContainerRegistries(
      List<EncryptedDataDetail> encryptionDetails, AzureConnectorDTO azureConnector, String subscriptionId) {
    AzureConfig azureConfig = AcrRequestResponseMapper.toAzureInternalConfig(azureConnector.getCredential(),
        encryptionDetails, azureConnector.getCredential().getAzureCredentialType(),
        azureConnector.getAzureEnvironmentType(), secretDecryptionService);

    AzureRegistriesResponse response;
    try {
      AcrRegistriesDTO containerRegistries =
          AcrRegistriesDTO.builder()
              .registries(azureContainerRegistryClient.listContainerRegistries(azureConfig, subscriptionId)
                              .stream()
                              .map(registry -> AcrRegistryDTO.builder().registry(registry.name()).build())
                              .collect(Collectors.toList()))
              .build();
      response = AzureRegistriesResponse.builder()
                     .containerRegistries(containerRegistries)
                     .commandExecutionStatus(CommandExecutionStatus.SUCCESS)
                     .build();

    } catch (Exception e) {
      String errorMessage = e.getMessage();
      response = AzureRegistriesResponse.builder()
                     .commandExecutionStatus(CommandExecutionStatus.FAILURE)
                     .errorSummary(ngErrorHelper.getErrorSummary(errorMessage))
                     .build();
    }
    return response;
  }

  public AzureClustersResponse listClusters(List<EncryptedDataDetail> encryptionDetails,
      AzureConnectorDTO azureConnector, String subscriptionId, String resourceGroup) {
    AzureConfig azureConfig = AcrRequestResponseMapper.toAzureInternalConfig(azureConnector.getCredential(),
        encryptionDetails, azureConnector.getCredential().getAzureCredentialType(),
        azureConnector.getAzureEnvironmentType(), secretDecryptionService);

    AzureClustersResponse response;
    try {
      AzureClustersDTO clusters =
          AzureClustersDTO.builder()
              .clusters(
                  azureKubernetesClient.listKubernetesClusters(azureConfig, subscriptionId)
                      .stream()
                      .filter(
                          kubernetesCluster -> kubernetesCluster.resourceGroupName().equalsIgnoreCase(resourceGroup))
                      .map(HasName::name)
                      .map(cluster -> AzureClusterDTO.builder().cluster(cluster).build())
                      .collect(Collectors.toList()))
              .build();
      response = AzureClustersResponse.builder()
                     .clusters(clusters)
                     .commandExecutionStatus(CommandExecutionStatus.SUCCESS)
                     .build();

    } catch (Exception e) {
      String errorMessage = e.getMessage();
      response = AzureClustersResponse.builder()
                     .commandExecutionStatus(CommandExecutionStatus.FAILURE)
                     .errorSummary(ngErrorHelper.getErrorSummary(errorMessage))
                     .build();
    }
    return response;
  }

  public KubernetesConfig getClusterConfig(AzureConnectorDTO azureConnector, String subscriptionId,
      String resourceGroup, String cluster, String namespace, List<EncryptedDataDetail> encryptedDataDetails) {
    AzureConfig azureConfig = AcrRequestResponseMapper.toAzureInternalConfig(azureConnector.getCredential(),
        encryptedDataDetails, azureConnector.getCredential().getAzureCredentialType(),
        azureConnector.getAzureEnvironmentType(), secretDecryptionService);

    KubernetesCluster k8sCluster =
        azureKubernetesClient.listKubernetesClusters(azureConfig, subscriptionId)
            .stream()
            .filter(kubernetesCluster
                -> kubernetesCluster.resourceGroupName().equalsIgnoreCase(resourceGroup)
                    && kubernetesCluster.name().equalsIgnoreCase(cluster))
            .findFirst()
            .orElseThrow(()
                             -> new AzureAKSException(String.format(
                                 "AKS Cluster %s has not been found for subscription %s and resourceGroup %s ", cluster,
                                 subscriptionId, resourceGroup)));

    // TODO add additional credentials username, pass, cert...
    if (azureConfig.getAzureAuthenticationType() == AzureAuthenticationType.SERVICE_PRINCIPAL_CERT
        || azureConfig.getAzureAuthenticationType() == AzureAuthenticationType.SERVICE_PRINCIPAL_SECRET) {
      // add logic for SPs
      return KubernetesConfig.builder()
          .masterUrl(k8sCluster.fqdn())
          .namespace(namespace)
          .username(k8sCluster.servicePrincipalClientId().toCharArray())
          .password(k8sCluster.servicePrincipalSecret().toCharArray())
          .build();
    } else if (azureConfig.getAzureAuthenticationType() == AzureAuthenticationType.MANAGED_IDENTITY_SYSTEM_ASSIGNED
        || azureConfig.getAzureAuthenticationType() == AzureAuthenticationType.MANAGED_IDENTITY_USER_ASSIGNED) {
      // add logic for MSI
      return null;
    }

    return null;
  }

  public AzureRepositoriesResponse listRepositories(List<EncryptedDataDetail> encryptionDetails,
      AzureConnectorDTO azureConnector, String subscriptionId, String containerRegistry) {
    AzureConfig azureConfig = AcrRequestResponseMapper.toAzureInternalConfig(azureConnector.getCredential(),
        encryptionDetails, azureConnector.getCredential().getAzureCredentialType(),
        azureConnector.getAzureEnvironmentType(), secretDecryptionService);

    // temp implementation until jackson library is upgraded works only for secret pass
    if (azureConfig.getAzureAuthenticationType() != AzureAuthenticationType.SERVICE_PRINCIPAL_SECRET) {
      throw new InvalidRequestException(format(
          "Unable to list repositories, registryHost: %s, subscriptionId: %s . Not supported authentication type!",
          containerRegistry, subscriptionId));
    }

    AzureRepositoriesResponse response;
    try {
      Registry registry =
          azureContainerRegistryClient
              .findFirstContainerRegistryByNameOnSubscription(azureConfig, subscriptionId, containerRegistry)
              .orElseThrow(()
                               -> new InvalidRequestException(
                                   format("Not found Azure container registry by name: %s, subscription id: %s",
                                       containerRegistry, subscriptionId)));

      AcrRepositoriesDTO repositories =
          AcrRepositoriesDTO.builder()
              .repositories(
                  azureContainerRegistryClient.listRepositories(azureConfig, subscriptionId, registry.loginServerUrl())
                      .stream()
                      .distinct()
                      .map(repository -> AcrRepositoryDTO.builder().repository(repository).build())
                      .collect(Collectors.toList()))
              .build();
      response = AzureRepositoriesResponse.builder()
                     .repositories(repositories)
                     .commandExecutionStatus(CommandExecutionStatus.SUCCESS)
                     .build();

    } catch (Exception e) {
      String errorMessage = e.getMessage();
      response = AzureRepositoriesResponse.builder()
                     .commandExecutionStatus(CommandExecutionStatus.FAILURE)
                     .errorSummary(ngErrorHelper.getErrorSummary(errorMessage))
                     .build();
    }
    return response;
  }

  public List<BuildDetailsInternal> getBuilds(
      AzureConfig azureConfig, String subscriptionId, String containerRegistry, String repository) {
    // temp implementation until jackson library is upgraded works only for secret pass
    if (azureConfig.getAzureAuthenticationType() != AzureAuthenticationType.SERVICE_PRINCIPAL_SECRET) {
      throw new InvalidRequestException(format(
          "Unable to list repositories, registryHost: %s, subscriptionId: %s . Not supported connector authentication type!",
          containerRegistry, subscriptionId));
    }

    Registry registry =
        azureContainerRegistryClient
            .findFirstContainerRegistryByNameOnSubscription(azureConfig, subscriptionId, containerRegistry)
            .orElseThrow(()
                             -> new InvalidRequestException(
                                 format("Not found Azure container registry by name: %s, subscription id: %s",
                                     containerRegistry, subscriptionId)));

    String registryUrl = registry.loginServerUrl().toLowerCase();
    String imageUrl = registryUrl + "/" + ArtifactUtilities.trimSlashforwardChars(repository);
    return azureContainerRegistryClient.listRepositoryTags(azureConfig, registryUrl, repository)
        .stream()
        .map(tag -> {
          Map<String, String> metadata = new HashMap<>();
          metadata.put(ArtifactMetadataKeys.IMAGE, imageUrl + ":" + tag);
          metadata.put(ArtifactMetadataKeys.TAG, tag);
          metadata.put(ArtifactMetadataKeys.REGISTRY_HOSTNAME, registryUrl);
          return BuildDetailsInternal.builder()
              .number(tag)
              .buildUrl(imageUrl + ":" + tag)
              .metadata(metadata)
              .uiDisplayName("Tag# " + tag)
              .build();
        })
        .collect(Collectors.toList());
  }

  public BuildDetailsInternal getLastSuccessfulBuildFromRegex(
      AzureConfig azureConfig, String subscription, String registry, String repository, String tagRegex) {
    try {
      Pattern.compile(tagRegex);
    } catch (PatternSyntaxException e) {
      throw NestedExceptionUtils.hintWithExplanationException(
          "Please check tagRegex field in ACR artifact configuration.",
          String.format("TagRegex field contains an invalid regex value '%s'.", tagRegex),
          new AzureContainerRegistryException(e.getMessage()));
    }
    // Temporary check as currently azure client is supported only for secret text type authentication
    if (azureConfig.getAzureAuthenticationType() != AzureAuthenticationType.SERVICE_PRINCIPAL_SECRET
        && azureConfig.getKey() != null) {
      throw NestedExceptionUtils.hintWithExplanationException("Currently not supported",
          "Currently regex field could not be used with connector that uses Certificate or Managed Identity for authentication",
          new AzureContainerRegistryException(
              String.format("Could not find an artifact tag that matches tagRegex '%s'", tagRegex)));
    }

    List<BuildDetailsInternal> builds = getBuilds(azureConfig, subscription, registry, repository);
    builds = builds.stream()
                 .filter(build -> new RegexFunctor().match(tagRegex, build.getNumber()))
                 .sorted(new BuildDetailsInternalComparatorDescending())
                 .collect(Collectors.toList());

    if (builds.isEmpty()) {
      throw NestedExceptionUtils.hintWithExplanationException(
          "Please check tagRegex field in ACR artifact configuration.",
          String.format(
              "Could not find any tags that match regex [%s] for ACR repository [%s] for subscription [%s] in registry [%s].",
              tagRegex, repository, subscription, registry),
          new AzureContainerRegistryException(
              String.format("Could not find an artifact tag that matches tagRegex '%s'", tagRegex)));
    }
    return builds.get(0);
  }

  public BuildDetailsInternal verifyBuildNumber(
      AzureConfig azureConfig, String subscription, String registry, String repository, String tag) {
    // Temporary check as currently azure client is supported only for secret text type authentication
    if (azureConfig.getAzureAuthenticationType() != AzureAuthenticationType.SERVICE_PRINCIPAL_SECRET
        && azureConfig.getKey() != null) {
      return getBuildDetailsInternalWhenCantCallAzure(registry, repository, tag);
    }

    List<BuildDetailsInternal> builds = getBuilds(azureConfig, subscription, registry, repository);
    builds = builds.stream().filter(build -> build.getNumber().equals(tag)).collect(Collectors.toList());

    if (builds.isEmpty()) {
      throw NestedExceptionUtils.hintWithExplanationException(
          "Please check your ACR repository for artifact tag existence.",
          String.format(
              "Did not find any artifacts for tag [%s] in ACR repository [%s] for subscription [%s] in registry [%s].",
              tag, repository, subscription, registry),
          new AzureContainerRegistryException(String.format("Artifact tag '%s' not found.", tag)));
    } else if (builds.size() == 1) {
      return builds.get(0);
    }

    throw NestedExceptionUtils.hintWithExplanationException(
        "Please check your ACR repository for artifacts with same tag.",
        String.format(
            "Found multiple artifacts for tag [%s] in Artifactory repository [%s] for subscription [%s] in registry [%s].",
            tag, repository, subscription, registry),
        new AzureContainerRegistryException(
            String.format("Found multiple artifact tags '%s', but expected only one.", tag)));
  }

  private BuildDetailsInternal getBuildDetailsInternalWhenCantCallAzure(
      String registry, String repository, String tag) {
    String repositoryNameNormalized = ArtifactUtilities.trimSlashforwardChars(repository);
    String tagUrl = registry + ".azurecr.io/" + repositoryNameNormalized + "/";
    Map<String, String> metadata = new HashMap<>();
    metadata.put(ArtifactMetadataKeys.IMAGE, tagUrl + ":" + tag);
    metadata.put(ArtifactMetadataKeys.TAG, tag);
    return BuildDetailsInternal.builder()
        .number(tag)
        .buildUrl(tagUrl + ":" + tag)
        .metadata(metadata)
        .uiDisplayName("Tag# " + tag)
        .build();
  }
}
