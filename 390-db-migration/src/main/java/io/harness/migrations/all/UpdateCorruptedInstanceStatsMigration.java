/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.migrations.all;

import static io.harness.persistence.HQuery.excludeAuthority;
import static io.harness.threading.Morpheus.sleep;

import static java.lang.String.format;

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
import software.wings.beans.infrastructure.instance.stats.InstanceStatsSnapshot.AggregateCount;
import software.wings.beans.infrastructure.instance.stats.InstanceStatsSnapshot.InstanceStatsSnapshotKeys;
import software.wings.service.intfc.instance.stats.InstanceStatService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.mongodb.morphia.query.Query;

@Slf4j
@OwnedBy(HarnessTeam.CDP)
public class UpdateCorruptedInstanceStatsMigration implements Migration {
  @Inject private InstanceStatService instanceStatService;
  @Inject private MongoPersistence mongoPersistence;
  @Inject private FeatureFlagService featureFlagService;

  // time of 06.03.2022 00:00:00 GMT
  private static final long FROM = 1646524800000L;
  // time of 25.03.2022 00:00:00 GMT
  private static final long UNTIL = 1648166400000L;

  private static final int INFRA_INSTANCE_BATCH_SIZE = 50;
  private static final int INSTANCE_STATS_BATCH_SIZE = 500;

  private final String DEBUG_LINE = "UPDATE_CORRUPTED_INSTANCE_STATS_MIGRATION: ";
  @Override
  public void migrate() {
    log.info("Running UpdateCorruptedInstanceStatsMigration");
    Set<String> affectedAccounts = deleteDuplicateInstanceMigration();
    updateCorruptedInstanceStats(affectedAccounts);
    log.info("Completed UpdateCorruptedInstanceStatsMigration");
  }

  private Set<String> deleteDuplicateInstanceMigration() {
    Set<String> affectedAccounts = new HashSet<>();
    try (HIterator<InfrastructureMapping> infraMappingIter =
             new HIterator<>(mongoPersistence.createQuery(InfrastructureMapping.class, excludeAuthority)
                                 .filter(InfrastructureMappingKeys.deploymentType, "HELM")
                                 .project("_id", true)
                                 .project(InfrastructureMappingKeys.accountId, true)
                                 .project(InfrastructureMappingKeys.appId, true)
                                 .disableValidation()
                                 .fetch())) {
      int updated = 0;
      BulkWriteOperation instanceWriteOperation =
          mongoPersistence.getCollection(Instance.class).initializeUnorderedBulkOperation();
      while (infraMappingIter.hasNext()) {
        InfrastructureMapping infraMapping = infraMappingIter.next();
        affectedAccounts.add(infraMapping.getAccountId());
        if (featureFlagService.isEnabled(FeatureName.KEEP_PT_AFTER_K8S_DOWNSCALE, infraMapping.getAccountId())) {
          continue;
        }

        HashMap<String, Object> query = new HashMap<>();
        query.put(InstanceKeys.appId, infraMapping.getAppId());
        query.put(InstanceKeys.infraMappingId, infraMapping.getUuid());
        query.put(InstanceKeys.createdAt, new BasicDBObject(ImmutableMap.of("$gte", FROM, "$lt", UNTIL)));
        query.put(InstanceKeys.lastWorkflowExecutionId, null);
        query.put(InstanceKeys.lastDeployedById, "AUTO_SCALE");

        instanceWriteOperation.find(new BasicDBObject(query)).remove();
        updated++;

        if (updated >= INFRA_INSTANCE_BATCH_SIZE) {
          log.info("Delete corrupted instances for {} infra mappings: {}", updated, instanceWriteOperation.execute());
          sleep(Duration.ofMillis(100)); // give some relax time
          instanceWriteOperation = mongoPersistence.getCollection(Instance.class).initializeUnorderedBulkOperation();
          updated = 0;
        }
      }

      if (updated != 0) {
        log.info("Delete corrupted instances for {} infra mappings: {}", updated, instanceWriteOperation.execute());
      }
    }

    return affectedAccounts;
  }

  private void updateCorruptedInstanceStats(Set<String> affectedAccounts) {
    affectedAccounts.forEach(accountId -> {
      try {
        int updated = 0;
        BulkWriteOperation bulkWriteOperation =
            mongoPersistence.getCollection(InstanceStatsSnapshot.class).initializeUnorderedBulkOperation();

        List<InstanceStatsSnapshot> instanceStats =
            instanceStatService.aggregate(accountId, Instant.ofEpochMilli(FROM), Instant.ofEpochMilli(UNTIL));
        for (InstanceStatsSnapshot statsSnapshot : instanceStats) {
          InstanceStatsSnapshot updatedInstanceStats =
              getUpdatedInstanceStatsSnapshot(accountId, statsSnapshot.getTimestamp());

          if (updatedInstanceStats != null) {
            bulkWriteOperation.find(new BasicDBObject("_id", statsSnapshot.getUuid()))
                .updateOne(new BasicDBObject("$set",
                    new Document(ImmutableMap.of(InstanceStatsSnapshotKeys.aggregateCounts,
                        getAggregateCountDBList(updatedInstanceStats.getAggregateCounts()),
                        InstanceStatsSnapshotKeys.total, updatedInstanceStats.getTotal()))));

            updated++;

            if (updated >= INSTANCE_STATS_BATCH_SIZE) {
              log.info(
                  "Updated {} instance stats four account id {}: {}", updated, accountId, bulkWriteOperation.execute());
              bulkWriteOperation =
                  mongoPersistence.getCollection(InstanceStatsSnapshot.class).initializeUnorderedBulkOperation();
              updated = 0;
            }
          }
        }

        if (updated != 0) {
          log.info(
              "Updated {} instance stats four account id {}: {}", updated, accountId, bulkWriteOperation.execute());
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

  private InstanceStatsSnapshot getUpdatedInstanceStatsSnapshot(String accountId, Instant toTime) {
    if (StringUtils.isBlank(accountId)) {
      return null;
    }
    try {
      HIterator<Instance> instances = getInstances(accountId, toTime);
      return mapInstanceStatsSnapshot(instances, accountId, toTime);
    } catch (Exception ex) {
      log.error(
          StringUtils.join(DEBUG_LINE, format("Error getting unique instance count for accountId %s", accountId)), ex);
      return null;
    }
  }

  private HIterator<Instance> getInstances(String accountId, Instant toTime) {
    Query<Instance> containerFetchInstancesQuery = mongoPersistence.createQuery(Instance.class, excludeAuthority)
                                                       .filter("accountId", accountId)
                                                       .field("createdAt")
                                                       .lessThanOrEq(toTime.toEpochMilli());
    containerFetchInstancesQuery.and(
        containerFetchInstancesQuery.or(containerFetchInstancesQuery.criteria("isDeleted").equal(false),
            containerFetchInstancesQuery.criteria("deletedAt").greaterThanOrEq(toTime.toEpochMilli())));

    containerFetchInstancesQuery = containerFetchInstancesQuery.project("_id", true)
                                       .project(InstanceKeys.appId, true)
                                       .project(InstanceKeys.appName, true);

    return new HIterator<>(containerFetchInstancesQuery.fetch());
  }

  public InstanceStatsSnapshot mapInstanceStatsSnapshot(
      HIterator<Instance> instances, String accountId, Instant instant) {
    Collection<AggregateCount> appCounts = aggregateByApp(instances);
    List<AggregateCount> aggregateCounts = new ArrayList<>(appCounts);

    return new InstanceStatsSnapshot(instant, accountId, aggregateCounts);
  }

  private Collection<AggregateCount> aggregateByApp(HIterator<Instance> instances) {
    Map<String, AggregateCount> appCounts = new HashMap<>();

    while (instances.hasNext()) {
      Instance instance = instances.next();
      AggregateCount appCount = appCounts.computeIfAbsent(instance.getAppId(),
          appId -> new AggregateCount(EntityType.APPLICATION, instance.getAppName(), instance.getAppId(), 0));
      appCount.incrementCount(1);
    }

    return appCounts.values();
  }

  private BasicDBList getAggregateCountDBList(List<AggregateCount> aggregateCounts) {
    BasicDBList basicDBList = new BasicDBList();

    for (final AggregateCount aggregateCount : aggregateCounts) {
      basicDBList.add(new BasicDBObject(ImmutableMap.of("entityType", aggregateCount.getEntityType().name(), "name",
          aggregateCount.getName(), "id", aggregateCount.getId(), "count", aggregateCount.getCount())));
    }

    return basicDBList;
  }
}