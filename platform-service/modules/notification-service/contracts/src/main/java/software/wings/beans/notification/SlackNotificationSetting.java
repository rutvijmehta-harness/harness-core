/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package software.wings.beans.notification;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;

import lombok.Value;

@Value
@OwnedBy(PL)
public class SlackNotificationSetting implements SlackNotificationConfiguration {
  private String name;
  private String outgoingWebhookUrl;

  public static SlackNotificationSetting emptyConfig() {
    return new SlackNotificationSetting("", "");
  }

  @Override
  public String toString() {
    return "SlackNotificationSetting{"
        + "name='" + name + '\'' + ", outgoingWebhookUrl='"
        + "<redacted-for-security>" + '\'' + '}';
  }
}
