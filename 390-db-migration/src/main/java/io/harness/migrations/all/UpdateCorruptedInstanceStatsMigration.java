/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.migrations.all;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.persistence.HQuery.excludeAuthority;

import static java.lang.String.format;
import static org.reflections.Reflections.log;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.FeatureName;
import io.harness.ff.FeatureFlagService;
import io.harness.migrations.Migration;
import io.harness.mongo.MongoPersistence;
import io.harness.persistence.HIterator;

import software.wings.beans.EntityType;
import software.wings.beans.InfrastructureMapping;
import software.wings.beans.InfrastructureMapping.InfrastructureMappingKeys;
import software.wings.beans.infrastructure.instance.Instance;
import software.wings.beans.infrastructure.instance.Instance.InstanceKeys;
import software.wings.beans.infrastructure.instance.stats.InstanceStatsSnapshot;
import software.wings.dl.WingsPersistence;
import software.wings.service.intfc.instance.stats.InstanceStatService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.query.Query;

@OwnedBy(HarnessTeam.CDP)
public class UpdateCorruptedInstanceStatsMigration implements Migration {
  @Inject private InstanceStatService instanceStatService;
  @Inject private MongoPersistence mongoPersistence;
  @Inject private FeatureFlagService featureFlagService;
  @Inject private WingsPersistence wingsPersistence;

  // time of 08.03.2022 00:00:00 GMT
  private static final Instant FROM = Instant.ofEpochMilli(1646524800000L);

  // time of 08.03.2022 00:00:00 GMT
  private static final long CREATED_AT_FROM_TIMESTAMP = 1646524800000L;
  // time of 23.03.2022 00:00:00 GMT
  private static final long CREATED_AT_TILL_TIMESTAMP = 1648166400000L;

  private final String DEBUG_LINE = "UPDATE_CORRUPTED_INSTANCE_STATS_MIGRATION: ";
  @Override
  public void migrate() {
    log.info("Running UpdateCorruptedInstanceStatsMigration");
    deleteDuplicateInstanceMigration();
    updateCorruptedInstanceStats();
    log.info("Completed UpdateCorruptedInstanceStatsMigration");
  }

  private void deleteDuplicateInstanceMigration() {
    try (HIterator<InfrastructureMapping> infraMappingIter =
             new HIterator<>(mongoPersistence.createQuery(InfrastructureMapping.class, excludeAuthority)
                                 .filter(InfrastructureMappingKeys.deploymentType, "HELM")
                                 .project("_id", true)
                                 .project(InfrastructureMappingKeys.accountId, true)
                                 .project(InfrastructureMappingKeys.appId, true)
                                 .disableValidation()
                                 .fetch())) {
      while (infraMappingIter.hasNext()) {
        InfrastructureMapping infraMapping = infraMappingIter.next();
        if (featureFlagService.isEnabled(FeatureName.KEEP_PT_AFTER_K8S_DOWNSCALE, infraMapping.getAccountId())) {
          continue;
        }

        log.info("Delete orphaned instances for infra mapping {} and account {}", infraMapping.getUuid(),
            infraMapping.getAccountId());
        BulkWriteOperation instanceWriteOperation =
            mongoPersistence.getCollection(Instance.class).initializeUnorderedBulkOperation();
        HashMap<String, Object> query = new HashMap<>();
        query.put(InstanceKeys.appId, infraMapping.getAppId());
        query.put(InstanceKeys.infraMappingId, infraMapping.getUuid());
        query.put(InstanceKeys.createdAt,
            new BasicDBObject(ImmutableMap.of("$gte", CREATED_AT_FROM_TIMESTAMP, "$lt", CREATED_AT_TILL_TIMESTAMP)));
        query.put(InstanceKeys.lastWorkflowExecutionId, null);
        query.put(InstanceKeys.lastDeployedById, "AUTO_SCALE");

        instanceWriteOperation.find(new BasicDBObject(query)).remove();

        log.info("Deleted result {} for infraMapping {}", instanceWriteOperation.execute(), infraMapping.getUuid());
      }
    }
  }

  private void updateCorruptedInstanceStats() {
    Query<InfrastructureMapping> accountIdsQuery = wingsPersistence.createQuery(InfrastructureMapping.class)
                                                       .filter("deploymentType", "HELM")
                                                       .project("accountId", true)
                                                       .project("_id", true);
    Set<String> affectedAccounts = new HashSet<>();
    try (HIterator<InfrastructureMapping> iterator = new HIterator<>(accountIdsQuery.fetch())) {
      for (InfrastructureMapping mapping : iterator) {
        affectedAccounts.add(mapping.getAccountId());
      }
    } catch (Exception ex) {
      log.error(StringUtils.join(DEBUG_LINE, "Error fetching affected accounts for migration"), ex);
    }

    Instant to = Instant.now();
    affectedAccounts.forEach(accountId -> {
      try {
        List<InstanceStatsSnapshot> instanceStats = instanceStatService.aggregate(accountId, FROM, to);
        for (InstanceStatsSnapshot statsSnapshot : instanceStats) {
          List<InstanceStatsSnapshot.AggregateCount> updatedAggregates = new ArrayList<>();
          int updatedTotal = 0;
          for (InstanceStatsSnapshot.AggregateCount aggregateCount : statsSnapshot.getAggregateCounts()) {
            if (aggregateCount.getEntityType().equals(EntityType.APPLICATION)) {
              long countPerApp =
                  getCorrectUniqueInstanceCount(accountId, aggregateCount.getId(), statsSnapshot.getTimestamp());
              if (countPerApp == -1) {
                log.error(StringUtils.join(DEBUG_LINE,
                    format("Error getting unique instance count for instanceStatId %s", statsSnapshot.getUuid())));
                updatedTotal = -1;
                continue;
              }
              updatedAggregates.add(new InstanceStatsSnapshot.AggregateCount(
                  EntityType.APPLICATION, aggregateCount.getName(), aggregateCount.getId(), (int) countPerApp));
              updatedTotal += countPerApp;
            }
          }

          if (isNotEmpty(updatedAggregates) && updatedTotal != -1) {
            try {
              wingsPersistence.updateField(
                  InstanceStatsSnapshot.class, statsSnapshot.getUuid(), "aggregateCounts", updatedAggregates);
              wingsPersistence.updateField(InstanceStatsSnapshot.class, statsSnapshot.getUuid(), "total", updatedTotal);
            } catch (Exception ex) {
              log.error(StringUtils.join(
                            DEBUG_LINE, format("Error updating corrupted instanceStatId %s ", statsSnapshot.getUuid())),
                  ex);
            }
          }
        }

      } catch (Exception ex) {
        log.error(StringUtils.join(
                      DEBUG_LINE, format("Error updating corrupted instance stats for accountId %s ", accountId)),
            ex);
      }

      log.info(StringUtils.join(
          DEBUG_LINE, format("Updated corrupted instance stats successfully for accountId %s ", accountId)));
    });
  }

  private long getCorrectUniqueInstanceCount(String accountId, String appId, Instant toTime) {
    if (StringUtils.isBlank(accountId) || StringUtils.isBlank(appId)) {
      return 0;
    }
    try {
      return getInstanceCount(accountId, appId, toTime);
    } catch (Exception ex) {
      log.error(StringUtils.join(DEBUG_LINE,
                    format("Error getting unique instance count for accountId %s, appId %s ", accountId, appId)),
          ex);
      return -1;
    }
  }

  private long getInstanceCount(String accountId, String appId, Instant toTime) {
    Query<Instance> containerFetchInstancesQuery = wingsPersistence.createQuery(Instance.class)
                                                       .filter("accountId", accountId)
                                                       .filter("appId", appId)
                                                       .field("createdAt")
                                                       .lessThanOrEq(toTime.toEpochMilli());
    containerFetchInstancesQuery.and(
        containerFetchInstancesQuery.or(containerFetchInstancesQuery.criteria("isDeleted").equal(false),
            containerFetchInstancesQuery.criteria("deletedAt").greaterThanOrEq(toTime.toEpochMilli())));

    return containerFetchInstancesQuery.count();
  }
}