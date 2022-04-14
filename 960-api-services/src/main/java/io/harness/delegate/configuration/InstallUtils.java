/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.delegate.configuration;

import static io.harness.annotations.dev.HarnessTeam.DEL;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.data.structure.HarnessStringUtils.join;
import static io.harness.delegate.configuration.ClientTool.CF;
import static io.harness.delegate.configuration.ClientTool.CHARTMUSEUM;
import static io.harness.delegate.configuration.ClientTool.GO_TEMPLATE;
import static io.harness.delegate.configuration.ClientTool.HARNESS_PYWINRM;
import static io.harness.delegate.configuration.ClientTool.HELM;
import static io.harness.delegate.configuration.ClientTool.KUBECTL;
import static io.harness.delegate.configuration.ClientTool.KUSTOMIZE;
import static io.harness.delegate.configuration.ClientTool.OC;
import static io.harness.delegate.configuration.ClientTool.SCM;
import static io.harness.delegate.configuration.ClientTool.TERRAFORM_CONFIG_INSPECT;
import static io.harness.filesystem.FileIo.createDirectoryIfDoesNotExist;
import static io.harness.network.Http.getBaseUrl;

import static java.lang.String.format;

import io.harness.annotations.dev.OwnedBy;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.LogOutputStream;

@UtilityClass
@Slf4j
@OwnedBy(DEL)
public class InstallUtils {
  private static final String defaultKubectlVersion = "v1.13.2";
  private static final String newKubectlVersion = "v1.19.2";
  private static final List<String> kubectlVersions = Arrays.asList(defaultKubectlVersion, newKubectlVersion);

  private static final String goTemplateClientVersion = "v0.4";

  private static final String harnessPywinrmVersion = "v0.4-dev";

  private static final String helm3VersionNew = "v3.8.0";
  static final String helm3Version = "v3.1.2";
  static final String helm2Version = "v2.13.1";

  private static final List<String> helmVersions = Arrays.asList(helm2Version, helm3Version, helm3VersionNew);

  private static final String chartMuseumVersionOld = "v0.8.2";
  private static final String chartMuseumVersionNew = "v0.12.0";
  private static final List<String> chartMuseumVersions = Arrays.asList(chartMuseumVersionOld, chartMuseumVersionNew);
  private static final Map<String, String> chartMuseumPaths = new HashMap<>();

  private static final String ocVersion = "v4.2.16";

  private static final Map<String, String> kubectlPaths = new HashMap<>();
  private static final Map<String, String> kustomizePaths = new HashMap<>();

  private static final String kustomizeVersionOld = "v3.5.4";
  private static final String kustomizeVersionNew = "v4.0.0";
  private static String kustomizePath = "kustomize";
  public static boolean isCustomKustomizePath;

  private static final List<String> kustomizeVersions = Arrays.asList(kustomizeVersionOld, kustomizeVersionNew);

  private static String goTemplateToolPath = "go-template";
  private static String harnessPywinrmToolPath = "harness-pywinrm";
  private static final Map<String, String> helmPaths = new HashMap<>();

  static {
    helmPaths.put(helm2Version, HELM.getBinaryName());
    helmPaths.put(helm3Version, HELM.getBinaryName());
    helmPaths.put(helm3VersionNew, HELM.getBinaryName());
    kubectlPaths.put(defaultKubectlVersion, KUBECTL.getBinaryName());
    kubectlPaths.put(newKubectlVersion, KUBECTL.getBinaryName());
    kustomizePaths.put(kustomizeVersionOld, KUSTOMIZE.getBinaryName());
    kustomizePaths.put(kustomizeVersionNew, KUSTOMIZE.getBinaryName());
    chartMuseumPaths.put(chartMuseumVersionOld, CHARTMUSEUM.getBinaryName());
    chartMuseumPaths.put(chartMuseumVersionNew, CHARTMUSEUM.getBinaryName());
  }

  private static String ocPath = "oc";

  private static final String terraformConfigInspectCurrentVersion = "v1.0";
  // This is not the version provided by Hashicorp because currently they do not maintain releases as such
  private static final String terraformConfigInspectLatestVersion = "v1.1";
  private static final List<String> terraformConfigInspectVersions =
      Arrays.asList(terraformConfigInspectCurrentVersion, terraformConfigInspectLatestVersion);

  private static final String defaultScmVersion = "98fc345b";

  public static String getTerraformConfigInspectPath(final String version) {
    return join("/", TERRAFORM_CONFIG_INSPECT.getBaseDir(), version, getOsPath(), "amd64",
        TERRAFORM_CONFIG_INSPECT.getBinaryName());
  }
  public static String getTerraformConfigInspectPath(final boolean useLatestVersion) {
    if (useLatestVersion) {
      return join("/", TERRAFORM_CONFIG_INSPECT.getBaseDir(), terraformConfigInspectLatestVersion, getOsPath(), "amd64",
          TERRAFORM_CONFIG_INSPECT.getBinaryName());
    } else {
      return join("/", TERRAFORM_CONFIG_INSPECT.getBaseDir(), terraformConfigInspectCurrentVersion, getOsPath(),
          "amd64", TERRAFORM_CONFIG_INSPECT.getBinaryName());
    }
  }

  public static String getScmPath() {
    return join("/", getScmFolderPath(), SCM.getBinaryName());
  }

  /**
   * @deprecated use {@link #getScmPath()} instead
   * @return scm binary name
   */
  @Deprecated
  public static String getScmBinary() {
    return SCM.getBinaryName();
  }

  /**
   * @deprecated use {@link #getScmPath()} instead
   * @return the path to the scm binary
   */
  @Deprecated
  public static String getScmFolderPath() {
    return join("/", SCM.getBaseDir(), getScmVersion(), getOsPath(), "amd64");
  }

  public static String getDefaultKubectlPath() {
    return kubectlPaths.get(defaultKubectlVersion);
  }

  public static String getNewKubectlPath() {
    return kubectlPaths.get(newKubectlVersion);
  }

  public static String getGoTemplateToolPath() {
    return goTemplateToolPath;
  }

  public static String getHarnessPywinrmToolPath() {
    return harnessPywinrmToolPath;
  }

  public static String getHelm2Path() {
    return helmPaths.get(helm2Version);
  }

  public static String getHelm3Path() {
    return helmPaths.get(helm3Version);
  }

  public static String getHelm380Path() {
    return helmPaths.get(helm3VersionNew);
  }

  public static String getChartMuseumPath(boolean useLastestVersion) {
    if (useLastestVersion) {
      return chartMuseumPaths.get(chartMuseumVersionNew);
    }
    return chartMuseumPaths.get(chartMuseumVersionOld);
  }

  public static String getOcPath() {
    return ocPath;
  }

  public static String getKustomizePath(boolean useLatestVersion) {
    if (isCustomKustomizePath) {
      return kustomizePath;
    }
    return useLatestVersion ? kustomizePaths.get(kustomizeVersionNew) : kustomizePaths.get(kustomizeVersionOld);
  }

  public static boolean installKubectl(DelegateConfiguration configuration) {
    boolean kubectlInstalled = true;
    for (String version : kubectlVersions) {
      kubectlInstalled = kubectlInstalled && installKubectl(configuration, version);
    }
    return kubectlInstalled;
  }

  public static boolean installKubectl(DelegateConfiguration configuration, String kubectlVersion) {
    try {
      if (isNotEmpty(configuration.getKubectlPath())) {
        String kubectlPathFromConfig = configuration.getKubectlPath();
        kubectlPaths.put(kubectlVersion, kubectlPathFromConfig);
        log.info("Found user configured kubectl at {}. Skipping Install.", kubectlPathFromConfig);
        return true;
      }

      if (SystemUtils.IS_OS_WINDOWS) {
        log.info("Skipping kubectl install on Windows");
        return true;
      }

      String kubectlDirectory = KUBECTL.getBaseDir() + kubectlVersion;

      if (validateToolExists(kubectlDirectory, KUBECTL)) {
        String kubectlPath = Paths.get(kubectlDirectory + "/kubectl").toAbsolutePath().normalize().toString();
        kubectlPaths.put(kubectlVersion, kubectlPath);
        log.info("kubectl version {} already installed", kubectlVersion);
        return true;
      }

      log.info("Installing kubectl");

      createDirectoryIfDoesNotExist(kubectlDirectory);

      String downloadUrl = getKubectlDownloadUrl(configuration, kubectlVersion);

      log.info("download Url is {}", downloadUrl);

      String script = "curl $MANAGER_PROXY_CURL -kLO " + downloadUrl + "\n"
          + "chmod +x ./kubectl\n"
          + "./kubectl version --short --client\n";

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(kubectlDirectory))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);
      ProcessResult result = processExecutor.execute();

      if (result.getExitValue() == 0) {
        String kubectlPath = Paths.get(kubectlDirectory + "/kubectl").toAbsolutePath().normalize().toString();
        kubectlPaths.put(kubectlVersion, kubectlPath);
        log.info(result.outputUTF8());
        if (validateToolExists(kubectlDirectory, KUBECTL)) {
          log.info("kubectl path: {}", kubectlPath);
          return true;
        } else {
          log.error("kubectl not validated after download: {}", kubectlPath);
          return false;
        }
      } else {
        log.error("kubectl install failed");
        log.error(result.outputUTF8());
        return false;
      }
    } catch (Exception e) {
      log.error("Error installing kubectl", e);
      return false;
    }
  }

  private static String getKubectlDownloadUrl(DelegateConfiguration delegateConfiguration, String version) {
    if (delegateConfiguration.isUseCdn()) {
      return join("/", delegateConfiguration.getCdnUrl(), String.format(KUBECTL.getCdnPath(), version, getOsPath()));
    }

    return getManagerBaseUrl(delegateConfiguration.getManagerUrl())
        + "storage/harness-download/kubernetes-release/release/" + version + "/bin/" + getOsPath() + "/amd64/kubectl";
  }

  public static boolean installGoTemplateTool(DelegateConfiguration configuration) {
    try {
      if (SystemUtils.IS_OS_WINDOWS) {
        log.info("Skipping go-template install on Windows");
        return true;
      }

      String goTemplateClientDirectory = GO_TEMPLATE.getBaseDir() + goTemplateClientVersion;

      if (validateToolExists(goTemplateClientDirectory, GO_TEMPLATE)) {
        goTemplateToolPath =
            Paths.get(goTemplateClientDirectory + "/go-template").toAbsolutePath().normalize().toString();
        log.info("go-template version {} already installed", goTemplateClientVersion);
        return true;
      }

      log.info("Installing go-template");

      createDirectoryIfDoesNotExist(goTemplateClientDirectory);

      String downloadUrl = getGoTemplateDownloadUrl(configuration);

      log.info("download Url is {}", downloadUrl);

      String script = "curl $MANAGER_PROXY_CURL -kLO " + downloadUrl + "\n"
          + "chmod +x ./go-template\n"
          + "./go-template -v\n";

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(goTemplateClientDirectory))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);
      ProcessResult result = processExecutor.execute();

      if (result.getExitValue() == 0) {
        goTemplateToolPath =
            Paths.get(goTemplateClientDirectory + "/go-template").toAbsolutePath().normalize().toString();
        log.info(result.outputUTF8());
        if (validateToolExists(goTemplateClientDirectory, GO_TEMPLATE)) {
          log.info("go-template path: {}", goTemplateToolPath);
          return true;
        } else {
          log.error("go-template not validated after download: {}", goTemplateToolPath);
          return false;
        }
      } else {
        log.error("go-template install failed");
        log.error(result.outputUTF8());
        return false;
      }
    } catch (Exception e) {
      log.error("Error installing go-template", e);
      return false;
    }
  }

  public static boolean installHarnessPywinrm(DelegateConfiguration configuration) {
    try {
      if (SystemUtils.IS_OS_WINDOWS) {
        log.info("Skipping harness-pywinrm install on Windows");
        return true;
      }

      String harnessPywinrmClientDirectory = HARNESS_PYWINRM.getBaseDir() + harnessPywinrmVersion;

      if (validateToolExists(harnessPywinrmClientDirectory, HARNESS_PYWINRM)) {
        harnessPywinrmToolPath =
            Paths.get(harnessPywinrmClientDirectory + "/harness-pywinrm").toAbsolutePath().normalize().toString();
        log.info("harness-pywinrm version {} already installed", harnessPywinrmVersion);
        return true;
      }

      log.info("Installing harness-pywinrm");

      createDirectoryIfDoesNotExist(harnessPywinrmClientDirectory);

      String downloadUrl = getHarnessPywinrmDownloadUrl(configuration);

      log.info("download Url is {}", downloadUrl);

      String script = "curl $MANAGER_PROXY_CURL -kLO " + downloadUrl + "\n"
          + "chmod +x ./harness-pywinrm\n";

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(harnessPywinrmClientDirectory))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);
      ProcessResult result = processExecutor.execute();

      if (result.getExitValue() == 0) {
        harnessPywinrmToolPath =
            Paths.get(harnessPywinrmClientDirectory + "/harness-pywinrm").toAbsolutePath().normalize().toString();
        log.info(format("harness-pywinrm version: %s", result.outputUTF8()));
        if (validateToolExists(harnessPywinrmClientDirectory, HARNESS_PYWINRM)) {
          log.info("harness-pywinrm path: {}", harnessPywinrmToolPath);
          return true;
        } else {
          log.error("harness-pywinrm not validated after download: {}", harnessPywinrmToolPath);
          return false;
        }
      } else {
        log.error("harness-pywinrm install failed\n" + result.outputUTF8());
        return false;
      }
    } catch (final Exception e) {
      log.error("Error installing harness-pywinrm", e);
      return false;
    }
  }

  public static void setupDefaultPaths(DelegateConfiguration delegateConfiguration) {
    if (isNotEmpty(delegateConfiguration.getKustomizePath())) {
      kustomizePath = delegateConfiguration.getKustomizePath();
      isCustomKustomizePath = true;
    }
    if (isNotEmpty(delegateConfiguration.getKubectlPath())) {
      kubectlPaths.put(defaultKubectlVersion, delegateConfiguration.getKubectlPath());
      kubectlPaths.put(newKubectlVersion, delegateConfiguration.getKubectlPath());
    }
    if (isNotEmpty(delegateConfiguration.getHelm3Path())) {
      helmPaths.put(helm3Version, delegateConfiguration.getHelm3Path());
      helmPaths.put(helm3VersionNew, delegateConfiguration.getHelm3Path());
    }
    if (isNotEmpty(delegateConfiguration.getHelmPath())) {
      helmPaths.put(helm2Version, delegateConfiguration.getHelmPath());
    }
    if (isNotEmpty(delegateConfiguration.getOcPath())) {
      ocPath = delegateConfiguration.getOcPath();
    }
  }

  private static String getGoTemplateDownloadUrl(DelegateConfiguration delegateConfiguration) {
    if (delegateConfiguration.isUseCdn()) {
      return join("/", delegateConfiguration.getCdnUrl(),
          String.format(GO_TEMPLATE.getCdnPath(), InstallUtils.goTemplateClientVersion, getOsPath()));
    }

    return getManagerBaseUrl(delegateConfiguration.getManagerUrl())
        + "storage/harness-download/snapshot-go-template/release/" + InstallUtils.goTemplateClientVersion + "/bin/"
        + getOsPath() + "/amd64/go-template";
  }

  private static String getHarnessPywinrmDownloadUrl(DelegateConfiguration delegateConfiguration) {
    if (delegateConfiguration.isUseCdn()) {
      return join("/", delegateConfiguration.getCdnUrl(),
          String.format(HARNESS_PYWINRM.getCdnPath(), InstallUtils.harnessPywinrmVersion, getOsPath()));
    }

    return getManagerBaseUrl(delegateConfiguration.getManagerUrl())
        + "storage/harness-download/snapshot-harness-pywinrm/release/" + InstallUtils.harnessPywinrmVersion + "/bin/"
        + getOsPath() + "/amd64/harness-pywinrm";
  }

  private static String getManagerBaseUrl(String managerUrl) {
    if (managerUrl.contains("localhost") || managerUrl.contains("127.0.0.1")) {
      return "https://app.harness.io/";
    }

    return getBaseUrl(managerUrl);
  }

  private static String getKustomizeDownloadUrl(DelegateConfiguration delegateConfiguration, String version) {
    if (delegateConfiguration.isUseCdn()) {
      return join("/", delegateConfiguration.getCdnUrl(), String.format(KUSTOMIZE.getCdnPath(), version, getOsPath()));
    }

    return getManagerBaseUrl(delegateConfiguration.getManagerUrl())
        + "storage/harness-download/harness-kustomize/release/" + version + "/bin/" + getOsPath() + "/amd64/kustomize";
  }

  private static String getHelmDownloadUrl(DelegateConfiguration delegateConfiguration, String version) {
    if (delegateConfiguration.isUseCdn()) {
      return join("/", delegateConfiguration.getCdnUrl(), String.format(HELM.getCdnPath(), version, getOsPath()));
    }

    return getManagerBaseUrl(delegateConfiguration.getManagerUrl()) + "storage/harness-download/harness-helm/release/"
        + version + "/bin/" + getOsPath() + "/amd64/helm";
  }

  private static boolean initHelmClient(String helmVersion) throws InterruptedException, TimeoutException, IOException {
    if (isHelmV2(helmVersion)) {
      log.info("Init helm client only");

      String helmDirectory = HELM.getBaseDir() + helmVersion;
      String script = "./helm init -c --skip-refresh \n";

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(helmDirectory))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);
      ProcessResult result = processExecutor.execute();
      if (result.getExitValue() == 0) {
        log.info("Successfully init helm client");
        return true;
      } else {
        log.error("Helm client init failed");
        log.error(result.outputUTF8());
        return false;
      }
    } else {
      log.info("Init helm not needed for helm v3");
      return true;
    }
  }

  static boolean isHelmV2(String helmVersion) {
    return helmVersion.toLowerCase().startsWith("v2");
  }

  static boolean isHelmV3(String helmVersion) {
    return helmVersion.toLowerCase().startsWith("v3");
  }

  public static boolean installHelm(DelegateConfiguration configuration) {
    boolean helmInstalled = true;
    for (String version : helmVersions) {
      helmInstalled = helmInstalled && installHelm(configuration, version);
    }
    return helmInstalled;
  }

  private static boolean installHelm(DelegateConfiguration configuration, String helmVersion) {
    try {
      if (delegateConfigHasHelmPath(configuration, helmVersion)) {
        return true;
      }

      if (SystemUtils.IS_OS_WINDOWS) {
        log.info("Skipping helm install on Windows");
        return true;
      }

      String helmDirectory = HELM.getBaseDir() + helmVersion;
      if (validateToolExists(helmDirectory, HELM)) {
        String helmPath = Paths.get(helmDirectory + "/helm").toAbsolutePath().normalize().toString();
        helmPaths.put(helmVersion, helmPath);
        log.info(format("helm version %s already installed", helmVersion));

        return initHelmClient(helmVersion);
      }

      log.info(format("Installing helm %s", helmVersion));
      createDirectoryIfDoesNotExist(helmDirectory);

      String downloadUrl = getHelmDownloadUrl(configuration, helmVersion);
      log.info("Download Url is " + downloadUrl);

      String versionCommand = getHelmVersionCommand(helmVersion);
      String initCommand = getHelmInitCommand(helmVersion);
      String script = "curl $MANAGER_PROXY_CURL -kLO " + downloadUrl + " \n"
          + "chmod +x ./helm \n" + versionCommand + initCommand;

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(helmDirectory))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);
      ProcessResult result = processExecutor.execute();

      if (result.getExitValue() == 0) {
        String helmPath = Paths.get(helmDirectory + "/helm").toAbsolutePath().normalize().toString();
        helmPaths.put(helmVersion, helmPath);
        log.info(result.outputUTF8());

        if (validateToolExists(helmDirectory, HELM)) {
          log.info("helm path: {}", helmPath);
          return true;
        } else {
          log.error("helm not validated after download: {}", helmPath);
          return false;
        }
      } else {
        log.error("helm install failed");
        log.error(result.outputUTF8());
        return false;
      }
    } catch (Exception e) {
      log.error("Error installing helm", e);
      return false;
    }
  }

  private static String getHelmInitCommand(String helmVersion) {
    return isHelmV2(helmVersion) ? "./helm init -c --skip-refresh \n" : StringUtils.EMPTY;
  }

  private static String getHelmVersionCommand(String helmVersion) {
    return isHelmV2(helmVersion) ? "./helm version -c \n" : "./helm version \n";
  }

  private static String getChartMuseumDownloadUrl(DelegateConfiguration delegateConfiguration, String version) {
    if (delegateConfiguration.isUseCdn()) {
      return join(
          "/", delegateConfiguration.getCdnUrl(), String.format(CHARTMUSEUM.getCdnPath(), version, getOsPath()));
    }

    return getManagerBaseUrl(delegateConfiguration.getManagerUrl())
        + "storage/harness-download/harness-chartmuseum/release/" + version + "/bin/" + getOsPath()
        + "/amd64/chartmuseum";
  }
  public static boolean installChartMuseum(DelegateConfiguration configuration) {
    boolean chartMuseumVersionsInstalled = true;
    for (String version : chartMuseumVersions) {
      chartMuseumVersionsInstalled = chartMuseumVersionsInstalled && installChartMuseum(configuration, version);
    }
    return chartMuseumVersionsInstalled;
  }
  public static boolean installChartMuseum(DelegateConfiguration configuration, String version) {
    try {
      if (SystemUtils.IS_OS_WINDOWS) {
        log.info("Skipping chart museum install on Windows");
        return true;
      }

      String chartMuseumDirectory = CHARTMUSEUM.getBaseDir() + version;
      String chartMuseumPath;
      if (validateToolExists(chartMuseumDirectory, CHARTMUSEUM)) {
        chartMuseumPath = Paths.get(chartMuseumDirectory + "/chartmuseum").toAbsolutePath().normalize().toString();
        chartMuseumPaths.put(version, chartMuseumPath);
        log.info("chartmuseum version {} already installed", version);
        return true;
      }

      log.info("Installing chartmuseum");
      createDirectoryIfDoesNotExist(chartMuseumDirectory);

      String downloadUrl = getChartMuseumDownloadUrl(configuration, version);
      log.info("Download Url is " + downloadUrl);

      String script = "curl $MANAGER_PROXY_CURL -kLO " + downloadUrl + "\n"
          + "chmod +x ./chartmuseum \n"
          + "./chartmuseum -v \n";

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(chartMuseumDirectory))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);

      ProcessResult result = processExecutor.execute();
      if (result.getExitValue() == 0) {
        chartMuseumPath = Paths.get(chartMuseumDirectory + "/chartmuseum").toAbsolutePath().normalize().toString();
        chartMuseumPaths.put(version, chartMuseumPath);
        log.info(result.outputUTF8());

        if (validateToolExists(chartMuseumDirectory, CHARTMUSEUM)) {
          log.info("chartmuseum path: {}", chartMuseumPath);
          return true;
        } else {
          log.error("chartmuseum not validated after download: {}", chartMuseumPath);
          return false;
        }
      } else {
        log.error("chart museum install failed");
        log.error(result.outputUTF8());
        return false;
      }

    } catch (Exception e) {
      log.error("Error installing chart museum", e);
      return false;
    }
  }

  public static boolean installTerraformConfigInspect(DelegateConfiguration configuration) {
    boolean terraformConfigInspectInstalled = true;
    for (String version : terraformConfigInspectVersions) {
      terraformConfigInspectInstalled =
          terraformConfigInspectInstalled && installTerraformConfigInspect(configuration, version);
    }
    return terraformConfigInspectInstalled;
  }

  public static boolean installTerraformConfigInspect(DelegateConfiguration configuration, String version) {
    try {
      if (SystemUtils.IS_OS_WINDOWS) {
        log.info(format("Skipping terraform-config-inspect version %s install on Windows", version));
        return true;
      }

      final String terraformConfigInspectVersionedDirectory =
          Paths.get(getTerraformConfigInspectPath(version)).getParent().toString();
      if (validateToolExists(terraformConfigInspectVersionedDirectory, TERRAFORM_CONFIG_INSPECT)) {
        log.info(format("terraform-config-inspect version %s already installed at {}", version),
            terraformConfigInspectVersionedDirectory);
        return true;
      }

      log.info(format("Installing terraform-config-inspect version %s", version));
      createDirectoryIfDoesNotExist(terraformConfigInspectVersionedDirectory);

      String downloadUrl = getTerraformConfigInspectDownloadUrl(configuration, version);
      log.info("Download Url is {}", downloadUrl);

      String script = "curl $MANAGER_PROXY_CURL -LO " + downloadUrl + "\n"
          + "chmod +x ./terraform-config-inspect";

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(terraformConfigInspectVersionedDirectory))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);
      ProcessResult result = processExecutor.execute();
      if (result.getExitValue() == 0) {
        String tfConfigInspectPath = Paths.get(getTerraformConfigInspectPath(version)).toAbsolutePath().toString();
        log.info(format("terraform config inspect version %s installed at {}", version), tfConfigInspectPath);
        return true;
      } else {
        log.error(format("Error installing Terraform Config Inspect version %s", version));
        return false;
      }

    } catch (Exception ex) {
      log.error(format("Error installing Terraform Config Inspect version %s", version), ex);
      return false;
    }
  }

  @VisibleForTesting
  static String getTerraformConfigInspectDownloadUrl(DelegateConfiguration delegateConfiguration, String version) {
    if (delegateConfiguration.isUseCdn()) {
      return join("/", delegateConfiguration.getCdnUrl(),
          String.format(TERRAFORM_CONFIG_INSPECT.getCdnPath(), version, getOsPath()));
    }
    return getManagerBaseUrl(delegateConfiguration.getManagerUrl())
        + "storage/harness-download/harness-terraform-config-inspect/" + version + "/" + getOsPath() + "/amd64/"
        + TERRAFORM_CONFIG_INSPECT.getBinaryName();
  }

  public static boolean installScm(DelegateConfiguration configuration) {
    try {
      if (SystemUtils.IS_OS_WINDOWS) {
        log.info("Skipping scm install on Windows");
        return true;
      }

      final String scmVersionedDirectory = Paths.get(getScmPath()).getParent().toString();
      if (validateToolExists(scmVersionedDirectory, SCM)) {
        log.info("scm already installed at {}", scmVersionedDirectory);
        return true;
      }

      log.info("Installing scm");
      createDirectoryIfDoesNotExist(scmVersionedDirectory);

      String downloadUrl = getScmDownloadUrl(configuration);
      log.info("Download Url is {}", downloadUrl);

      String script = "curl $MANAGER_PROXY_CURL -kLO " + downloadUrl + "\n"
          + "chmod +x ./scm";

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(scmVersionedDirectory))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);
      ProcessResult result = processExecutor.execute();
      if (result.getExitValue() == 0) {
        String scmPath = Paths.get(getScmPath()).toAbsolutePath().toString();
        log.info(result.outputUTF8());
        if (validateToolExists(scmVersionedDirectory, SCM)) {
          log.info("scm path: {}", scmPath);
          return true;
        } else {
          log.error("scm not validated after download: {}", scmPath);
          return false;
        }
      } else {
        log.error("scm install failed");
        log.error(result.outputUTF8());
        return false;
      }
    } catch (Exception e) {
      log.error("Error installing scm", e);
      return false;
    }
  }

  public static String getScmVersion() {
    String version = System.getenv().get("SCM_VERSION");
    if (StringUtils.isEmpty(version)) {
      version = defaultScmVersion;
      log.info("No version configured. Using default scm version {}", version);
    }
    return version;
  }

  @VisibleForTesting
  static String getScmDownloadUrl(DelegateConfiguration delegateConfiguration) {
    String scmVersion = getScmVersion();
    if (delegateConfiguration.isUseCdn()) {
      return join("/", delegateConfiguration.getCdnUrl(), String.format(SCM.getCdnPath(), scmVersion, getOsPath()));
    }
    return getManagerBaseUrl(delegateConfiguration.getManagerUrl()) + "storage/harness-download/harness-scm/release/"
        + scmVersion + "/bin/" + getOsPath() + "/amd64/" + SCM.getBinaryName();
  }

  public static boolean installOc(DelegateConfiguration configuration) {
    try {
      if (StringUtils.isNotEmpty(configuration.getOcPath())) {
        ocPath = configuration.getOcPath();
        log.info("Found user configured oc at {}. Skipping Install.", ocPath);
        return true;
      }

      if (SystemUtils.IS_OS_WINDOWS) {
        log.info("Skipping oc install on Windows");
        return true;
      }

      String version = System.getenv().get("OC_VERSION");
      if (StringUtils.isEmpty(version)) {
        version = ocVersion;
        log.info("No version configured. Using default oc version {}", version);
      }

      String ocDirectory = OC.getBaseDir() + version;
      if (validateToolExists(ocDirectory, OC)) {
        ocPath = Paths.get(ocDirectory, "oc").toAbsolutePath().normalize().toString();
        log.info("oc version {} already installed", version);
        return true;
      }

      log.info("Installing oc");
      createDirectoryIfDoesNotExist(ocDirectory);

      String downloadUrl = getOcDownloadUrl(configuration, version);
      log.info("download url is {}", downloadUrl);

      String script = "curl $MANAGER_PROXY_CURL -kLO " + downloadUrl + "\n"
          + "chmod +x ./oc\n"
          + "./oc version --client\n";

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(ocDirectory))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);
      ProcessResult result = processExecutor.execute();

      if (result.getExitValue() == 0) {
        ocPath = Paths.get(ocDirectory, "oc").toAbsolutePath().normalize().toString();
        log.info(result.outputUTF8());
        if (validateToolExists(ocDirectory, OC)) {
          log.info("oc path: {}", ocPath);
          return true;
        } else {
          log.error("oc not validated after download: {}", ocPath);
          return false;
        }
      } else {
        log.error("oc install failed");
        log.error(result.outputUTF8());
        return false;
      }
    } catch (Exception e) {
      log.error("Error installing oc", e);
      return false;
    }
  }

  private static String getOcDownloadUrl(DelegateConfiguration delegateConfiguration, String version) {
    if (delegateConfiguration.isUseCdn()) {
      return join("/", delegateConfiguration.getCdnUrl(), String.format(OC.getCdnPath(), version, getOsPath()));
    }
    return getManagerBaseUrl(delegateConfiguration.getManagerUrl()) + "storage/harness-download/harness-oc/release/"
        + version + "/bin/" + getOsPath() + "/amd64/oc";
  }

  public static boolean installKustomize(DelegateConfiguration configuration) {
    boolean kustomizeInstalled = true;
    for (String version : kustomizeVersions) {
      kustomizeInstalled = kustomizeInstalled && installKustomize(configuration, version);
    }
    return kustomizeInstalled;
  }

  public static boolean installKustomize(DelegateConfiguration configuration, String kustomizeVersion) {
    try {
      if (StringUtils.isNotEmpty(configuration.getKustomizePath())) {
        isCustomKustomizePath = true;
        kustomizePath = configuration.getKustomizePath();
        kustomizePaths.put(kustomizeVersion, kustomizePath);
        log.info("Found user configured kustomize at {}. Skipping Install.", kustomizePath);
        return true;
      }
      isCustomKustomizePath = false;
      if (SystemUtils.IS_OS_WINDOWS) {
        log.info("Skipping kustomize install on Windows");
        return true;
      }

      String kustomizeDir = Paths.get(KUSTOMIZE.getBaseDir(), kustomizeVersion).toAbsolutePath().normalize().toString();

      if (validateToolExists(kustomizeDir, KUSTOMIZE)) {
        kustomizePath = Paths.get(kustomizeDir, "kustomize").toAbsolutePath().normalize().toString();
        kustomizePaths.put(kustomizeVersion, kustomizePath);
        log.info("kustomize version {} already installed", kustomizeVersion);
        return true;
      }

      log.info("Installing kustomize");

      createDirectoryIfDoesNotExist(kustomizeDir);

      String downloadUrl = getKustomizeDownloadUrl(configuration, kustomizeVersion);

      log.info("download Url is {}", downloadUrl);

      String script = "curl $MANAGER_PROXY_CURL -kLO " + downloadUrl + "\n"
          + "chmod +x ./kustomize\n"
          + "./kustomize version --short\n";

      ProcessExecutor processExecutor = new ProcessExecutor()
                                            .timeout(10, TimeUnit.MINUTES)
                                            .directory(new File(kustomizeDir))
                                            .command("/bin/bash", "-c", script)
                                            .readOutput(true);
      ProcessResult result = processExecutor.execute();

      if (result.getExitValue() == 0) {
        kustomizePath = Paths.get(kustomizeDir, "kustomize").toAbsolutePath().normalize().toString();
        kustomizePaths.put(kustomizeVersion, kustomizePath);
        log.info(result.outputUTF8());
        if (validateToolExists(kustomizeDir, KUSTOMIZE)) {
          log.info("kustomize path: {}", kustomizePath);
          return true;
        } else {
          log.error("kustomize not validated after download: {}", kustomizePath);
          return false;
        }
      } else {
        log.error("kustomize version {} install failed", kustomizeVersion);
        log.error(result.outputUTF8());
        return false;
      }
    } catch (Exception e) {
      log.error("Error installing kustomize", e);
      return false;
    }
  }

  @VisibleForTesting
  static boolean delegateConfigHasHelmPath(DelegateConfiguration configuration, String helmVersion) {
    if (helm2Version.equals(helmVersion)) {
      if (isNotEmpty(configuration.getHelmPath())) {
        String helmPath = configuration.getHelmPath();
        helmPaths.put(helmVersion, helmPath);
        log.info("Found user configured helm2 at {}. Skipping Install.", helmPath);
        return true;
      }
    } else if (helm3Version.equals(helmVersion) || helm3VersionNew.equals(helmVersion)) {
      if (isNotEmpty(configuration.getHelm3Path())) {
        String helmPath = configuration.getHelm3Path();
        helmPaths.put(helmVersion, helmPath);
        log.info("Found user configured helm3 at {}. Skipping Install.", helmPath);
        return true;
      }
    }
    return false;
  }

  public static boolean validateCfCliExists() {
    return validateToolExists(".", CF);
  }

  private static boolean validateToolExists(final String toolDirectory, final ClientTool tool) {
    try {
      if (!Files.exists(Paths.get(toolDirectory, tool.getBinaryName()))) {
        return false;
      }

      return runCommand(toolDirectory, tool.getValidateCommand());
    } catch (final Exception e) {
      log.error("Error validating if tool {} exists using {}", tool.getBinaryName(), tool.getValidateCommand(), e);
      return false;
    }
  }

  private static boolean runCommand(final String toolDirectory, final String script)
      throws IOException, InterruptedException, TimeoutException {
    final ProcessExecutor processExecutor = new ProcessExecutor()
                                                .timeout(1, TimeUnit.MINUTES)
                                                .directory(new File(toolDirectory))
                                                .command("/bin/bash", "-c", script)
                                                .readOutput(true)
                                                .redirectOutput(new LogOutputStream() {
                                                  @Override
                                                  protected void processLine(final String line) {
                                                    log.info(line);
                                                  }
                                                })
                                                .redirectError(new LogOutputStream() {
                                                  @Override
                                                  protected void processLine(final String line) {
                                                    log.error(line);
                                                  }
                                                });
    final ProcessResult result = processExecutor.execute();

    if (result.getExitValue() == 0) {
      log.info(result.outputUTF8());
      return true;
    } else {
      log.error(result.outputUTF8());
      return false;
    }
  }

  @VisibleForTesting
  static String getOsPath() {
    if (SystemUtils.IS_OS_WINDOWS) {
      return "windows";
    }
    if (SystemUtils.IS_OS_MAC) {
      return "darwin";
    }
    return "linux";
  }
}
