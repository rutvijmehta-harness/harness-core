/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.cdng.service.beans;

public interface ServiceSpecType {
  String KUBERNETES = "Kubernetes";
  String SSH = "Ssh";
  String ECS = "Ecs";
  String NATIVE_HELM = "NativeHelm";
  String PCF = "Pcf";
  String WINRM = "WinRm";
  String DEPLOYMENT_TEMPLATE = "DeploymentTemplate";
}
