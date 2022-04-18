/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.audit.mapper;

import static io.harness.annotations.dev.HarnessTeam.PL;
import static io.harness.data.structure.EmptyPredicate.isEmpty;

import io.harness.annotations.dev.OwnedBy;
import io.harness.audit.beans.ResourceScopeDTO;
import io.harness.audit.entities.ResourceScope;
import io.harness.ng.core.common.beans.KeyValuePair;
import io.harness.ng.core.mapper.KeyValuePairMapper;

import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@OwnedBy(PL)
@UtilityClass
public class ResourceScopeMapper {
  public static ResourceScope fromDTO(ResourceScopeDTO dto) {
    List<KeyValuePair> labels = KeyValuePairMapper.convertToList(dto.getLabels());
    if (isEmpty(labels)) {
      labels = null;
    }
    return ResourceScope.builder()
        .accountIdentifier(dto.getAccountIdentifier())
        .orgIdentifier(dto.getOrgIdentifier())
        .projectIdentifier(dto.getProjectIdentifier())
        .labels(labels)
        .build();
  }

  public static ResourceScopeDTO toDTO(ResourceScope dbo) {
    Map<String, String> labels = KeyValuePairMapper.convertToMap(dbo.getLabels());
    if (isEmpty(labels)) {
      labels = null;
    }
    return ResourceScopeDTO.builder()
        .accountIdentifier(dbo.getAccountIdentifier())
        .orgIdentifier(dbo.getOrgIdentifier())
        .projectIdentifier(dbo.getProjectIdentifier())
        .labels(labels)
        .build();
  }
}
