/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.delegate.beans.executioncapability;

import io.harness.annotations.dev.HarnessModule;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.annotations.dev.TargetModule;

import java.time.Duration;

@OwnedBy(HarnessTeam.DEL)
@TargetModule(HarnessModule._955_DELEGATE_BEANS)
public interface ExecutionCapability {
  enum EvaluationMode { MANAGER, AGENT }

  EvaluationMode evaluationMode();

  CapabilityType getCapabilityType();
  String fetchCapabilityBasis();

  /**
   * Should return the maximal period for which the existing successful check of the capability can be considered as
   * valid. Applicable to capabilities with Evaluation Mode AGENT.
   */
  Duration getMaxValidityPeriod();

  /**
   * Should return the period that should pass until the capability check should be validated again. Applicable to
   * capabilities with Evaluation Mode AGENT.
   */
  Duration getPeriodUntilNextValidation();

  default String getCapabilityToString() {
    return null;
  }
}
