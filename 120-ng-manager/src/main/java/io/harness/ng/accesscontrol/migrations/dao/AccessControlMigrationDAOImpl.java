/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ng.accesscontrol.migrations.dao;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.Scope;
import io.harness.ng.accesscontrol.migrations.models.AccessControlMigration;
import io.harness.ng.accesscontrol.migrations.repositories.AccessControlMigrationRepository;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
@OwnedBy(HarnessTeam.PL)
public class AccessControlMigrationDAOImpl implements AccessControlMigrationDAO {
  private final AccessControlMigrationRepository migrationRepository;

  @Override
  public AccessControlMigration save(AccessControlMigration accessControlMigration) {
    return migrationRepository.save(accessControlMigration);
  }

  @Override
  public boolean isMigrated(Scope scope) {
    return migrationRepository
        .findByAccountIdentifierAndOrgIdentifierAndProjectIdentifier(
            scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier())
        .isPresent();
  }
}
