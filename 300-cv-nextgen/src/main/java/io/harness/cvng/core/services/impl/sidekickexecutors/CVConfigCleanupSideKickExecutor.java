/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.core.services.impl.sidekickexecutors;

import io.harness.cvng.core.beans.sidekick.CVConfigCleanupSideKickData;
import io.harness.cvng.core.entities.DeletedCVConfig;
import io.harness.cvng.core.services.api.DeletedCVConfigService;
import io.harness.cvng.core.services.api.SideKickExecutor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Clock;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class CVConfigCleanupSideKickExecutor implements SideKickExecutor<CVConfigCleanupSideKickData> {
  @Inject private Clock clock;
  @Inject private DeletedCVConfigService deletedCVConfigService;

  @Override
  public void execute(CVConfigCleanupSideKickData sideKickInfo) {
    log.info("SidekickInfo {}", sideKickInfo);
    DeletedCVConfig deletedCVConfig = sideKickInfo.getDeletedCVConfig();
    if (Objects.nonNull(deletedCVConfig)) {
      log.info("Triggering cleanup for CVConfig {}", deletedCVConfig.getCvConfig().getUuid());
      deletedCVConfigService.triggerCleanup(deletedCVConfig);
      log.info("Cleanup complete for CVConfig {}", deletedCVConfig.getCvConfig().getUuid());
    }
  }

  @Override
  public RetryData shouldRetry(int lastRetryCount) {
    if (lastRetryCount < 5) {
      return RetryData.builder().shouldRetry(true).nextRetryTime(clock.instant().plusSeconds(300)).build();
    }
    return RetryData.builder().shouldRetry(false).build();
  }
}
