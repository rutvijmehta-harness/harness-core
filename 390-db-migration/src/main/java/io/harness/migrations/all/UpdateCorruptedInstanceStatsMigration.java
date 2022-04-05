/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.migrations.all;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;

import static java.lang.String.format;
import static org.mongodb.morphia.aggregation.Accumulator.accumulator;
import static org.mongodb.morphia.aggregation.Group.grouping;
import static org.mongodb.morphia.aggregation.Group.id;
import static org.reflections.Reflections.log;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.migrations.Migration;
import io.harness.mongo.index.migrator.ContainerIdAggregate;
import io.harness.persistence.HIterator;

import software.wings.beans.EntityType;
import software.wings.beans.InfrastructureMapping;
import software.wings.beans.infrastructure.instance.Instance;
import software.wings.beans.infrastructure.instance.stats.InstanceStatsSnapshot;
import software.wings.dl.WingsPersistence;
import software.wings.service.intfc.instance.stats.InstanceStatService;

import com.google.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.query.Query;

@OwnedBy(HarnessTeam.CDP)
public class UpdateCorruptedInstanceStatsMigration implements Migration {
  @Inject private InstanceStatService instanceStatService;
  @Inject private WingsPersistence wingsPersistence;

  // time of 21.02.2022 00:00:00 IST
  public static final Instant FROM = Instant.ofEpochMilli(1645381800000L);
  private final String DEBUG_LINE = "UPDATE_CORRUPTED_INSTANCE_STATS_MIGRATION: ";

  @Override
  public void migrate() {
    log.info("Running UpdateCorruptedInstanceStatsMigration");
    Query<InfrastructureMapping> accountIdsQuery = wingsPersistence.createQuery(InfrastructureMapping.class)
                                                       .filter("deploymentType", "HELM")
                                                       .project("accountId", true)
                                                       .project("_id", true);
    Map<String, Set<String>> affectedAccounts = new HashMap<>();
    try (HIterator<InfrastructureMapping> iterator = new HIterator<>(accountIdsQuery.fetch())) {
      for (InfrastructureMapping mapping : iterator) {
        if (affectedAccounts.containsKey(mapping.getAccountId())) {
          affectedAccounts.get(mapping.getAccountId()).add(mapping.getUuid());
        } else {
          affectedAccounts.put(mapping.getAccountId(), new HashSet<>(Collections.singleton(mapping.getUuid())));
        }
      }
    } catch (Exception ex) {
      log.error(StringUtils.join(DEBUG_LINE, "Error fetching affected accounts for migration"));
    }

    Instant to = Instant.now();

    affectedAccounts.keySet().forEach(accountId -> {
      try {
        List<InstanceStatsSnapshot> instanceStats = instanceStatService.aggregate(accountId, FROM, to);
        for (InstanceStatsSnapshot statsSnapshot : instanceStats) {
          List<InstanceStatsSnapshot.AggregateCount> updatedAggregates = new ArrayList<>();
          int updatedTotal = 0;
          for (InstanceStatsSnapshot.AggregateCount aggregateCount : statsSnapshot.getAggregateCounts()) {
            if (aggregateCount.getEntityType().equals(EntityType.APPLICATION)) {
              int countPerApp =
                  getCorrectUniqueInstanceCount(accountId, aggregateCount.getId(), statsSnapshot.getTimestamp());
              if (countPerApp == -1) {
                log.error(StringUtils.join(DEBUG_LINE,
                    format("Error getting unique instance count for instanceStatId %s", statsSnapshot.getUuid())));
                updatedTotal = -1;
                continue;
              }
              updatedAggregates.add(new InstanceStatsSnapshot.AggregateCount(
                  EntityType.APPLICATION, aggregateCount.getName(), aggregateCount.getId(), countPerApp));
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
                  DEBUG_LINE, format("Error updating corrupted instanceStatId %s ", statsSnapshot.getUuid())));
            }
          }
        }

      } catch (Exception ex) {
        log.error(StringUtils.join(
            DEBUG_LINE, format("Error updating corrupted instance stats for accountId %s ", accountId)));
      }

      log.info(StringUtils.join(
          DEBUG_LINE, format("Updated corrupted instance stats successfully for accountId %s ", accountId)));
    });

    log.info("Completed UpdateCorruptedInstanceStatsMigration");
  }

  private int getCorrectUniqueInstanceCount(String accountId, String appId, Instant toTime) {
    if (StringUtils.isBlank(accountId) || StringUtils.isBlank(appId)) {
      return 0;
    }
    try {
      AtomicInteger containerCounter = getContainerTypeInstanceCount(accountId, appId, toTime);
      AtomicInteger nonContainerCounter = getNonContainerTypeInstanceCount(accountId, appId, toTime);
      return containerCounter.get() + nonContainerCounter.get();
    } catch (Exception ex) {
      log.error(StringUtils.join(
          DEBUG_LINE, format("Error getting unique instance count for accountId %s, appId %s ", accountId, appId)));
      return -1;
    }
  }

  private AtomicInteger getNonContainerTypeInstanceCount(String accountId, String appId, Instant toTime) {
    AtomicInteger counter = new AtomicInteger();
    Query<Instance> nonContainerFetchInstancesQuery = wingsPersistence.createQuery(Instance.class)
                                                          .filter("accountId", accountId)
                                                          .filter("appId", appId)
                                                          .filter("isDeleted", false)
                                                          .filter("containerInstanceKey", null)
                                                          .field("deletedAt")
                                                          .greaterThanOrEq(toTime.toEpochMilli())
                                                          .field("createdAt")
                                                          .lessThanOrEq(toTime.toEpochMilli())
                                                          .project("containerInstanceKey", true);

    wingsPersistence.getDatastore(Instance.class)
        .createAggregation(Instance.class)
        .match(nonContainerFetchInstancesQuery)
        .group(id(grouping("containerId", "containerInstanceKey.containerId"),
                   grouping("namespace", "containerInstanceKey.namespace")),
            grouping("count", accumulator("$sum", 1)))
        .aggregate(ContainerIdAggregate.class)
        .forEachRemaining(instance -> { counter.getAndAdd(instance.getCount()); });
    return counter;
  }

  private AtomicInteger getContainerTypeInstanceCount(String accountId, String appId, Instant toTime) {
    AtomicInteger counter = new AtomicInteger();
    Query<Instance> containerFetchInstancesQuery = wingsPersistence.createQuery(Instance.class)
                                                       .filter("accountId", accountId)
                                                       .filter("appId", appId)
                                                       .filter("isDeleted", false)
                                                       .filter("containerInstanceKey !=", null)
                                                       .field("deletedAt")
                                                       .greaterThanOrEq(toTime.toEpochMilli())
                                                       .field("createdAt")
                                                       .lessThanOrEq(toTime.toEpochMilli())
                                                       .project("containerInstanceKey", true);

    wingsPersistence.getDatastore(Instance.class)
        .createAggregation(Instance.class)
        .match(containerFetchInstancesQuery)
        .group(id(grouping("containerId", "containerInstanceKey.containerId"),
                   grouping("namespace", "containerInstanceKey.namespace")),
            grouping("count", accumulator("$sum", 1)))
        .aggregate(ContainerIdAggregate.class)
        .forEachRemaining(instance -> { counter.getAndIncrement(); });
    return counter;
  }
}