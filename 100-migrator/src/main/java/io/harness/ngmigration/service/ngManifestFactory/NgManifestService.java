package io.harness.ngmigration.service.ngManifestFactory;

import io.harness.cdng.manifest.yaml.ManifestConfigWrapper;
import io.harness.ngmigration.beans.ManifestProvidedEntitySpec;
import io.harness.ngmigration.beans.NgEntityDetail;

import software.wings.beans.appmanifest.ApplicationManifest;
import software.wings.ngmigration.CgEntityId;

import java.util.Map;

public interface NgManifestService {
  ManifestConfigWrapper getManifestConfigWrapper(ApplicationManifest applicationManifest,
      Map<CgEntityId, NgEntityDetail> migratedEntities, ManifestProvidedEntitySpec entitySpec);
}
