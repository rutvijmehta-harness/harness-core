/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.migrations.all;

import io.harness.migrations.Migration;
import io.harness.persistence.HIterator;

import software.wings.beans.Account;
import software.wings.beans.Application;
import software.wings.beans.AwsInfrastructureMapping;
import software.wings.beans.InfrastructureMapping;
import software.wings.dl.WingsPersistence;
import software.wings.infra.AwsInstanceInfrastructure;
import software.wings.infra.InfrastructureDefinition;
import software.wings.service.impl.infrastructuredefinition.InfrastructureDefinitionHelper;

import com.google.inject.Inject;
import java.util.Collections;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateNameInAwsInfrastructureMappingMigration implements Migration {
  @Inject private WingsPersistence wingsPersistence;
  @Inject private InfrastructureDefinitionHelper infrastructureDefinitionHelper;

  private static final String DEBUG_LINE = "AWS_INFRA_STRUCTURE_NAME_MIGRATION:";

  @Override
  public void migrate() {
    log.info("{} Starting migration", DEBUG_LINE);
    try (HIterator<Account> accounts = new HIterator<>(wingsPersistence.createQuery(Account.class).fetch())) {
      while (accounts.hasNext()) {
        Account account = accounts.next();
        log.info("{} AccountId: {}", DEBUG_LINE, account.getUuid());
        try (HIterator<Application> applications =
                 new HIterator<>(wingsPersistence.createQuery(Application.class)
                                     .filter(Application.ApplicationKeys.accountId, account.getUuid())
                                     .fetch())) {
          while (applications.hasNext()) {
            Application application = applications.next();
            log.info("{} ApplicationId: {}", DEBUG_LINE, application.getUuid());
            try (HIterator<InfrastructureMapping> infraMappings = new HIterator<>(
                     wingsPersistence.createQuery(InfrastructureMapping.class)
                         .filter(InfrastructureMapping.InfrastructureMappingKeys.appId, application.getUuid())
                         .filter("className", "software.wings.beans.AwsInfrastructureMapping")
                         .fetch())) {
              while (infraMappings.hasNext()) {
                InfrastructureMapping infrastructureMapping = infraMappings.next();
                if (!(infrastructureMapping instanceof AwsInfrastructureMapping)) {
                  continue;
                }
                updateInfraMapping((AwsInfrastructureMapping) infrastructureMapping);
              }
            }
          }
        }
      }
    }
    log.info("{} Ended migration", DEBUG_LINE);
  }

  private void updateInfraMapping(@Nonnull AwsInfrastructureMapping infrastructureMapping) {
    try {
      AwsInstanceInfrastructure infrastructure =
          AwsInstanceInfrastructure.builder()
              .region(infrastructureMapping.getRegion())
              .awsInstanceFilter(infrastructureMapping.getAwsInstanceFilter())
              .autoScalingGroupName(infrastructureMapping.getAutoScalingGroupName())
              .build();

      String name = infrastructureDefinitionHelper.getNameFromInfraDefinition(
          InfrastructureDefinition.builder()
              .appId(infrastructureMapping.getAppId())
              .envId(infrastructureMapping.getEnvId())
              .uuid(infrastructureMapping.getInfrastructureDefinitionId())
              .infrastructure(infrastructure)
              .build(),
          infrastructureMapping.getServiceId());

      if (!name.equals(infrastructureMapping.getName())) {
        wingsPersistence.updateFields(AwsInfrastructureMapping.class, infrastructureMapping.getUuid(),
            Collections.singletonMap(InfrastructureMapping.InfrastructureMappingKeys.name, name));
        log.info("{} Updated name for infra mapping Id: {}", DEBUG_LINE, infrastructureMapping.getUuid());
      }

    } catch (Exception ex) {
      log.info("{} Failed to update name for infra mapping Id: {}", DEBUG_LINE, infrastructureMapping.getUuid());
    }
  }
}
