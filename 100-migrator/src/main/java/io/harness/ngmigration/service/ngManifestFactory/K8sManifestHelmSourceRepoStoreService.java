package io.harness.ngmigration.service.ngManifestFactory;

import io.harness.cdng.manifest.ManifestConfigType;
import io.harness.cdng.manifest.yaml.ManifestConfig;
import io.harness.cdng.manifest.yaml.ManifestConfigWrapper;
import io.harness.cdng.manifest.yaml.kinds.K8sManifest;
import io.harness.cdng.manifest.yaml.storeConfig.StoreConfigType;
import io.harness.cdng.manifest.yaml.storeConfig.StoreConfigWrapper;
import io.harness.ngmigration.beans.ManifestProvidedEntitySpec;
import io.harness.ngmigration.beans.NgEntityDetail;
import io.harness.ngmigration.service.ManifestMigrationService;
import io.harness.ngmigration.service.MigratorUtility;
import io.harness.pms.yaml.ParameterField;

import software.wings.beans.GitFileConfig;
import software.wings.beans.appmanifest.ApplicationManifest;
import software.wings.ngmigration.CgEntityId;
import software.wings.ngmigration.NGMigrationEntityType;

import com.google.inject.Inject;
import java.util.Map;

public class K8sManifestHelmSourceRepoStoreService implements NgManifestService {
  @Inject ManifestMigrationService manifestMigrationService;

  @Override
  public ManifestConfigWrapper getManifestConfigWrapper(ApplicationManifest applicationManifest,
      Map<CgEntityId, NgEntityDetail> migratedEntities, ManifestProvidedEntitySpec entitySpec) {
    GitFileConfig gitFileConfig = applicationManifest.getGitFileConfig();
    NgEntityDetail connector = migratedEntities.get(
        CgEntityId.builder().id(gitFileConfig.getConnectorId()).type(NGMigrationEntityType.CONNECTOR).build());

    K8sManifest k8sManifest =
        K8sManifest.builder()
            .identifier(MigratorUtility.generateIdentifier(applicationManifest.getUuid()))
            .skipResourceVersioning(
                ParameterField.createValueField(applicationManifest.getSkipVersioningForAllK8sObjects()))
            .store(ParameterField.createValueField(
                StoreConfigWrapper.builder()
                    .type(StoreConfigType.GIT)
                    .spec(manifestMigrationService.getGitStore(gitFileConfig, entitySpec, connector.getIdentifier()))
                    .build()))
            .build();
    return ManifestConfigWrapper.builder()
        .manifest(ManifestConfig.builder()
                      .identifier(MigratorUtility.generateIdentifier(applicationManifest.getUuid()))
                      .type(ManifestConfigType.K8_MANIFEST)
                      .spec(k8sManifest)
                      .build())
        .build();
  }
}
