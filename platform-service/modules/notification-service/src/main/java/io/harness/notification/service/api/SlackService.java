/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.notification.service.api;

import io.harness.Team;

import java.util.List;
import java.util.Map;

public interface SlackService extends ChannelService {
  boolean send(
      List<String> slackWebhookUrls, String templateId, Map<String, String> templateData, String notificationId);

  boolean send(List<String> slackWebhookUrls, String templateId, Map<String, String> templateData,
      String notificationId, Team team);
}
