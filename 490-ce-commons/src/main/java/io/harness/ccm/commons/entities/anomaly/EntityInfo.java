/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.ccm.commons.entities.anomaly;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Value
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntityInfo {
  // Note: Do not rename any field
  String field;
  String clusterName;
  String clusterId;
  String namespace;
  String workloadName;
  String workloadType;
  String gcpProjectId;
  String gcpProduct;
  String gcpSKUId;
  String gcpSKUDescription;
  String awsUsageAccountId;
  String awsServiceCode;
  String awsInstancetype;
  String awsUsageType;
  String azureSubscriptionGuid;
  String azureServiceName;
  String azureInstanceId;
}
