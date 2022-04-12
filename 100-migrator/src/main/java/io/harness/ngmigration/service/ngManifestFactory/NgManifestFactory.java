package io.harness.ngmigration.service.ngManifestFactory;

import io.harness.cdng.manifest.yaml.ManifestConfigWrapper;
import io.harness.ngmigration.beans.ManifestProvidedEntitySpec;
import io.harness.ngmigration.beans.NgEntityDetail;

import software.wings.beans.appmanifest.ApplicationManifest;
import software.wings.ngmigration.CgEntityId;

import com.google.inject.Inject;
import java.util.Map;

public class NgManifestFactory {
  @Inject k8sManifestStoretypeRemote k8sManifestStoretypeRemote;
  @Inject valuesConfig valuesConfig;
  final String k8sManifestRemote = "K8S_MANIFEST_Remote";
  final String valuesRemote = "VALUES_Remote";

  public ManifestConfigWrapper getManifestConfigWrapper(ApplicationManifest applicationManifest,
      Map<CgEntityId, NgEntityDetail> migratedEntities, ManifestProvidedEntitySpec entitySpec) {
    String manifestKindAndStoreType =
        applicationManifest.getKind().name() + "_" + applicationManifest.getStoreType().name();
    switch (manifestKindAndStoreType) {
      case k8sManifestRemote:
        return k8sManifestStoretypeRemote.getManifestConfigWrapper(applicationManifest, migratedEntities, entitySpec);
      case valuesRemote:
        return valuesConfig.getManifestConfigWrapper(applicationManifest, migratedEntities, entitySpec);
      default:
        throw new IllegalStateException();
    }
  }
}
