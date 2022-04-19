/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.notification.service.api;

import io.harness.Team;
import io.harness.notification.entities.NotificationTemplate;
import io.harness.stream.BoundedInputStream;

import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;

public interface NotificationTemplateService {
  NotificationTemplate create(String identifier, Team team, BoundedInputStream inputStream, Boolean harnessManaged);

  NotificationTemplate save(@NotNull NotificationTemplate notificationTemplate);

  Optional<NotificationTemplate> update(
      String templateIdentifier, Team team, BoundedInputStream inputStream, Boolean harnessManaged);

  List<NotificationTemplate> list(Team team);

  Optional<NotificationTemplate> getByIdentifierAndTeam(String identifier, Team team);

  Optional<String> getTemplateAsString(String identifier, Team team);

  Optional<NotificationTemplate> getPredefinedTemplate(String identifier);

  boolean delete(String templateIdentifier, Team team);

  void dropPredefinedTemplates();
}
