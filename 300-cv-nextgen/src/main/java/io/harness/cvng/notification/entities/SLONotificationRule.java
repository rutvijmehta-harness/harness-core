/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cvng.notification.entities;

import io.harness.cvng.notification.beans.NotificationRuleType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@JsonTypeName("SLO")
@Data
@SuperBuilder
@NoArgsConstructor
@FieldNameConstants(innerTypeName = "SLONotificationRuleKeys")
@EqualsAndHashCode(callSuper = true)
public class SLONotificationRule extends NotificationRule {
  Integer errorBudgetRemainingPercentageThreshold;

  @Override
  public NotificationRuleType getType() {
    return NotificationRuleType.SLO;
  }
}
