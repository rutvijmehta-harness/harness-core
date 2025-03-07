/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.scim;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class RemoveOperation extends PatchOperation {
  @JsonProperty protected final JsonNode value;

  @JsonIgnore private ObjectMapper jsonObjectMapper = new ObjectMapper();

  public RemoveOperation(@JsonProperty(value = "path", required = true) String path,
      @JsonProperty(value = "value", required = true) final JsonNode value) {
    super(path);
    this.value = value;
  }

  @Override
  public <T> List<T> getValues(final Class<T> cls) throws JsonProcessingException {
    ArrayList<T> objects = new ArrayList<>(value.size());
    for (JsonNode node : value) {
      objects.add(jsonObjectMapper.treeToValue(node, cls));
    }
    return objects;
  }

  @Override
  public String getOpType() {
    return "Remove";
  }
}
