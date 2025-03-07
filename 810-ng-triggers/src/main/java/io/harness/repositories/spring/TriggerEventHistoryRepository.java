/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.repositories.spring;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotation.HarnessRepo;
import io.harness.annotations.dev.OwnedBy;
import io.harness.ngtriggers.beans.entity.TriggerEventHistory;
import io.harness.repositories.custom.TriggerEventHistoryRepositoryCustom;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

@HarnessRepo
@OwnedBy(PIPELINE)
public interface TriggerEventHistoryRepository
    extends PagingAndSortingRepository<TriggerEventHistory, String>, TriggerEventHistoryRepositoryCustom {
  List<TriggerEventHistory> findFirst1ByAccountIdAndOrgIdentifierAndProjectIdentifierAndTriggerIdentifier(
      String accountId, String orgIdentifier, String projectIdentifier, String triggerIdentifier, Sort sort);

  List<TriggerEventHistory>
  findFirst1ByAccountIdAndOrgIdentifierAndProjectIdentifierAndTargetIdentifierAndTriggerIdentifier(String accountId,
      String orgIdentifier, String projectIdentifier, String targetIdentifier, String triggerIdentifier, Sort sort);

  TriggerEventHistory findByAccountIdAndEventCorrelationId(String accountId, String eventCorrelationId);
}
