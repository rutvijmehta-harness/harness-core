/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClientTool {
  CF("cf", "", "./client-tools/helm/", "./cf --version", ""),
  HELM("helm", "public/shared/tools/helm/release/%s/bin/%s/amd64/helm", "./client-tools/helm/", "./helm version -c",
      "storage/harness-download/harness-helm/release/%s/bin/%s/amd64/helm"),
  KUBECTL("kubectl", "public/shared/tools/kubectl/release/%s/bin/%s/amd64/kubectl", "./client-tools/kubectl/",
      "./kubectl version --short --client\n",
      "storage/harness-download/kubernetes-release/release/%s/bin/%s/amd64/kubectl"),
  KUSTOMIZE("kustomize", "public/shared/tools/kustomize/release/%s/bin/%s/amd64/kustomize", "./client-tools/kustomize/",
      "./kustomize version --short\n", "storage/harness-download/harness-kustomize/release/%s/bin/%s/amd64/kustomize"),
  OC("oc", "public/shared/tools/oc/release/%s/bin/%s/amd64/oc", "./client-tools/oc/", "./oc version --client\n",
      "storage/harness-download/harness-oc/release/%s/bin/%s/amd64/oc"),
  SCM("scm", "public/shared/tools/scm/release/%s/bin/%s/amd64/scm", "./client-tools/scm/", "./scm --version\n",
      "storage/harness-download/harness-scm/release/%s/bin/%s/amd64/scm"),
  TERRAFORM_CONFIG_INSPECT("terraform-config-inspect",
      "public/shared/tools/terraform-config-inspect/%s/%s/amd64/terraform-config-inspect",
      "./client-tools/tf-config-inspect", "./terraform-config-inspect",
      "storage/harness-download/harness-terraform-config-inspect/%s/%s/amd64/terraform-config-inspect"),
  GO_TEMPLATE("go-template", "public/shared/tools/go-template/release/%s/bin/%s/amd64/go-template",
      "./client-tools/go-template/", "./go-template -v\n",
      "storage/harness-download/snapshot-go-template/release/%s/bin/%s/amd64/go-template"),
  HARNESS_PYWINRM("harness-pywinrm", "public/shared/tools/harness-pywinrm/release/%s/bin/%s/amd64/harness-pywinrm",
      "./client-tools/harness-pywinrm/", "./harness-pywinrm -v\n",
      "storage/harness-download/snapshot-harness-pywinrm/release/%s/bin/%s/amd64/harness-pywinrm"),
  CHARTMUSEUM("chartmuseum", "public/shared/tools/chartmuseum/release/%s/bin/%s/amd64/chartmuseum",
      "./client-tools/chartmuseum/", "./chartmuseum -v",
      "storage/harness-download/harness-chartmuseum/release/%s/bin/%s/amd64/chartmuseum");

  private final String binaryName;
  private final String cdnPath;
  private final String baseDir;
  private final String validateCommand;
  private final String onPremPath;
}
