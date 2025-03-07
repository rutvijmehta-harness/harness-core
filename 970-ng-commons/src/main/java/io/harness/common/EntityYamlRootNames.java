/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.common;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

/**
 * Name of the top element in yaml.
 * For example:
 * <p>connector:
 * name: testname
 * projectIdentifier: projectId
 * </p>
 * In this the top element is <b>connector</b>
 */
@OwnedBy(PL)
public class EntityYamlRootNames {
  public static String ENVIRONMENT_GROUP = "environmentGroup";
  public static String PROJECT = "project";
  public static String PIPELINE = "pipeline";
  public static String PIPELINE_STEP = "pipelineStep";
  public static String CONNECTOR = "connector";
  public static String SECRET = "secret";
  public static String FILE = "file";
  public static String SERVICE = "service";
  public static String ENVIRONMENT = "environment";
  public static String INPUT_SET = "inputSet";
  public static String OVERLAY_INPUT_SET = "overlayInputSet";
  public static String CV_CONFIG = "cvConfig";
  public static String VERIFY = "Verify";
  public static String DELEGATE = "delegate";
  public static String DELEGATE_CONFIGURATION = "delegateConfigurations";
  public static String CV_VERIFICATION_JOB = "cvVerificationJob";
  public static String INTEGRATION_STAGE = "integrationStage";
  public static String INTEGRATION_STEP = "integrationSteps";
  public static String CV_KUBERNETES_ACTIVITY_SOURCE = "cvKubernetesActivitySource";
  public static String DEPLOYMENT_STEP = "deploymentSteps";
  public static String DEPLOYMENT_STAGE = "deploymentStage";
  public static String FEATURE_FLAG_STAGE = "featureFlagStage";
  public static String APPROVAL_STAGE = "approvalStage";
  public static String TRIGGERS = "trigger";
  public static String MONITORED_SERVICE = "monitoredService";
  public static String TEMPLATE = "template";
  public static String GIT_REPOSITORY = "gitRepository";
  public static String FEATURE_FLAGS = "featureFlags";
  public static String HTTP = "Http";
  public static String JIRA_CREATE = "JiraCreate";
  public static String JIRA_UPDATE = "JiraUpdate";
  public static String SHELL_SCRIPT = "ShellScript";
  public static String K8S_CANARY_DEPLOY = "K8sCanaryDeploy";
  public static String K8S_APPLY = "K8sApply";
  public static String K8S_BLUE_GREEN_DEPLOY = "K8sBlueGreenDeploy";
  public static String K8S_ROLLING_DEPLOY = "K8sRollingDeploy";
  public static String K8S_ROLLING_ROLLBACK = "K8sRollingRollback";
  public static String K8S_SCALE = "K8sScale";
  public static String K8S_DELETE = "K8sDelete";
  public static String K8S_SWAP_SERVICES = "K8sBGSwapServices";
  public static String K8S_CANARY_DELETE = "K8sCanaryDelete";
  public static String TERRAFORM_APPLY = "TerraformApply";
  public static String TERRAFORM_PLAN = "TerraformPlan";
  public static String TERRAFORM_DESTROY = "TerraformDestroy";
  public static String TERRAFORM_ROLLBACK = "TerraformRollback";
  public static String HELM_DEPLOY = "HelmDeploy";
  public static String HELM_ROLLBACK = "HelmRollback";
  public static String SERVICENOW_CREATE = "ServiceNowCreate";
  public static String SERVICENOW_UPDATE = "ServiceNowUpdate";
  public static String SERVICENOW_APPROVAL = "ServiceNowApproval";
  public static String JIRA_APPROVAL = "JiraApproval";
  public static String HARNESS_APPROVAL = "HarnessApproval";
  public static String BARRIER = "Barrier";
  public static String FLAG_CONFIGURATION = "FlagConfiguration";
  public static String OPAPOLICY = "governancePolicy";
  public static String POLICY_STEP = "Policy";
  public static String RUN_STEP = "Run";
  public static String RUN_TEST = "RunTests";
  public static String PLUGIN = "Plugin";
  public static String SECURITY = "Security";
  public static String SECURITY_STAGE = "securityStage";
  public static String SECURITY_STEP = "securityStep";
  public static String RESTORE_CACHE_GCS = "RestoreCacheGCS";
  public static String RESTORE_CACHE_S3 = "RestoreCacheS3";
  public static String SAVE_CACHE_GCS = "SaveCacheGCS";
  public static String SAVE_CACHE_S3 = "SaveCacheS3";
  public static String ARTIFACTORY_UPLOAD = "ArtifactoryUpload";
  public static String GCS_UPLOAD = "GCSUpload";
  public static String S3_UPLOAD = "S3Upload";

  public static String BUILD_AND_PUSH_GCR = "BuildAndPushGCR";
  public static String BUILD_AND_PUSH_ECR = "BuildAndPushECR";
  public static String BUILD_AND_PUSH_DOCKER_REGISTRY = "BuildAndPushDockerRegistry";
}
