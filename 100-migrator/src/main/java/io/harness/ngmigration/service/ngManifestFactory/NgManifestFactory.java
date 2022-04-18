package io.harness.ngmigration.service.ngManifestFactory;

import software.wings.beans.appmanifest.AppManifestKind;
import software.wings.beans.appmanifest.ApplicationManifest;
import software.wings.beans.appmanifest.StoreType;

public class NgManifestFactory {
  public NgManifestService getNgManifestService(ApplicationManifest applicationManifest) {
    AppManifestKind appManifestKind = applicationManifest.getKind();
    StoreType storeType = applicationManifest.getStoreType();

    switch (appManifestKind) {
      case K8S_MANIFEST:
        switch (storeType) {
          case Remote:
            return new K8sManifestRemoteStoreService();
          default:
            throw new IllegalStateException();
        }
      case VALUES:
        switch (storeType) {
          case Remote:
            return new ValuesManifestRemoteStoreService();
          default:
            throw new IllegalStateException();
        }
      default:
        throw new IllegalStateException();
    }
  }
}
